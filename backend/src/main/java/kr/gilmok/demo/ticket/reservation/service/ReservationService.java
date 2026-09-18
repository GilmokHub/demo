package kr.gilmok.demo.ticket.reservation.service;

import io.micrometer.core.instrument.MeterRegistry;
import kr.gilmok.demo.ticket.event.entity.Event;
import kr.gilmok.demo.ticket.event.repository.EventRepository;
import kr.gilmok.demo.ticket.reservation.dto.ReservationCreateRequest;
import kr.gilmok.demo.ticket.reservation.dto.ReservationResponse;
import kr.gilmok.demo.ticket.reservation.dto.ReservationStatsResponse;
import kr.gilmok.demo.ticket.reservation.entity.Reservation;
import kr.gilmok.demo.ticket.reservation.entity.ReservationStatus;
import kr.gilmok.demo.ticket.reservation.entity.Seat;
import kr.gilmok.demo.ticket.reservation.exception.ReservationErrorCode;
import kr.gilmok.demo.ticket.reservation.repository.ReservationRepository;
import kr.gilmok.demo.ticket.reservation.repository.SeatLockRedisRepository;
import kr.gilmok.demo.ticket.reservation.repository.SeatRepository;
import kr.gilmok.demo.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final SeatLockRedisRepository seatLockRedisRepository;
    private final MeterRegistry meterRegistry;

    @Value("${reservation.seat-lock-ttl-seconds:300}")
    private int seatLockTtlSeconds;

    @Value("${reservation.max-quantity:4}")
    private int maxQuantity;

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Transactional
    public ReservationResponse createReservation(Long userId, String username, ReservationCreateRequest request) {
        // 수량 검증
        if (request.quantity() < 1 || request.quantity() > maxQuantity) {
            throw new CustomException(ReservationErrorCode.INVALID_QUANTITY);
        }

        // 대기열 통과(ADMITTABLE) 검증
        String eventIdStr = String.valueOf(request.eventId());

        // 이벤트 확인
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new CustomException(ReservationErrorCode.EVENT_NOT_FOUND));

        if (!event.isOpen()) {
            throw new CustomException(ReservationErrorCode.EVENT_NOT_OPEN);
        }

        // 좌석 확인
        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new CustomException(ReservationErrorCode.SEAT_NOT_FOUND));
        // 좌석이 해당 이벤트에 속하는지 검증
        if (!seat.getEvent().getId().equals(request.eventId())) {
            throw new CustomException(ReservationErrorCode.SEAT_NOT_BELONG_TO_EVENT);
        }

        // Redis 좌석 잠금 (원자적)
        boolean locked = seatLockRedisRepository.lock(
                request.eventId(), request.seatId(), userId,
                request.quantity(), seatLockTtlSeconds);
        if (!locked) {
            throw new CustomException(ReservationErrorCode.SEAT_LOCK_FAILED);
        }

        Reservation saved;
        try {
            // 예약 생성 (HOLDING 상태)
            Reservation reservation = Reservation.builder()
                    .event(event)
                    .seat(seat).userId(userId).username(username)
                    .quantity(request.quantity())
                    .build();
            saved = reservationRepository.save(reservation);
        } catch (Exception e) {
            // DB 저장 실패 시 Redis 잠금 해제
            seatLockRedisRepository.unlockAndRestore(request.eventId(), request.seatId(), userId);
            throw e;
        }

        log.info("Reservation created: code={}, eventId={}, seatId={}, userId={}, qty={}",
                saved.getReservationCode(), request.eventId(), request.seatId(), userId, request.quantity());

        return ReservationResponse.from(saved, seatLockTtlSeconds);
    }

    @Transactional
    public ReservationResponse confirmReservation(Long userId, String reservationCode) {

        Reservation reservation = reservationRepository.findByReservationCodeForUpdate(reservationCode)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUserId().equals(userId)) {
            throw new CustomException(ReservationErrorCode.UNAUTHORIZED);
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            throw new CustomException(ReservationErrorCode.ALREADY_CONFIRMED);
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ReservationErrorCode.ALREADY_CANCELLED);
        }

        if (reservation.getCreatedAt().plusSeconds(seatLockTtlSeconds)
                .isBefore(java.time.LocalDateTime.now())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_EXPIRED);
        }

        // MySQL 비관적 잠금으로 좌석 예약 확정
        Seat seat = seatRepository.findByIdForUpdate(reservation.getSeat().getId())
                .orElseThrow(() -> new CustomException(ReservationErrorCode.SEAT_NOT_FOUND));

        seat.reserve(reservation.getQuantity());
        reservation.confirm();

        // Redis 잠금 해제
        seatLockRedisRepository.unlockAndRestore(
                reservation.getEvent().getId(), seat.getId(), userId);
        // 확정 후 Redis 잔여석을 DB 기준으로 재동기화
        seatLockRedisRepository.initAvailable(
                reservation.getEvent().getId(), seat.getId(), seat.getAvailableCount());

        log.info("Reservation confirmed: code={}", reservationCode);

        meterRegistry.counter("reservation.success.total", "eventId", String.valueOf(reservation.getEvent().getId()))
                .increment();

        return ReservationResponse.from(reservation, seatLockTtlSeconds);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long userId, String reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCodeForUpdate(reservationCode)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUserId().equals(userId)) {
            throw new CustomException(ReservationErrorCode.UNAUTHORIZED);
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ReservationErrorCode.ALREADY_CANCELLED);
        }

        boolean wasConfirmed = reservation.getStatus() == ReservationStatus.CONFIRMED;
        reservation.cancel();

        if (wasConfirmed) {
            // 확정된 예약 취소: DB 좌석 복구
            Seat seat = seatRepository.findByIdForUpdate(reservation.getSeat().getId())
                    .orElseThrow(() -> new CustomException(ReservationErrorCode.SEAT_NOT_FOUND));
            seat.cancelReservation(reservation.getQuantity());
            seatLockRedisRepository.initAvailable(
                    reservation.getEvent().getId(), seat.getId(), seat.getAvailableCount());
        } else {
            // HOLDING 상태 취소: Redis 잠금 해제 + 잔여석 복구
            seatLockRedisRepository.unlockAndRestore(
                    reservation.getEvent().getId(),
                    reservation.getSeat().getId(),
                    userId);
        }

        log.info("Reservation cancelled: code={}", reservationCode);

        return ReservationResponse.from(reservation, seatLockTtlSeconds);
    }

    public ReservationResponse getReservation(Long userId, String reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUserId().equals(userId)) {
            throw new CustomException(ReservationErrorCode.UNAUTHORIZED);
        }

        return ReservationResponse.from(reservation, seatLockTtlSeconds);
    }

    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(r -> ReservationResponse.from(r, seatLockTtlSeconds))
                .toList();
    }

    public List<ReservationResponse> getReservationsByEvent(Long eventId) {
        return reservationRepository.findByEventIdOrderByCreatedAtDesc(eventId)
                .stream()
                .map(r -> ReservationResponse.from(r, seatLockTtlSeconds))
                .toList();
    }

    public ReservationStatsResponse getReservationStats(Long eventId) {
        return new ReservationStatsResponse(
                eventId,
                reservationRepository.countByEventIdAndStatus(eventId, ReservationStatus.HOLDING),
                reservationRepository.countByEventIdAndStatus(eventId, ReservationStatus.CONFIRMED),
                reservationRepository.countByEventIdAndStatus(eventId, ReservationStatus.CANCELLED),
                reservationRepository.sumQuantityByEventIdAndStatus(eventId, ReservationStatus.HOLDING),
                reservationRepository.sumQuantityByEventIdAndStatus(eventId, ReservationStatus.CONFIRMED),
                reservationRepository.sumQuantityByEventIdAndStatus(eventId, ReservationStatus.CANCELLED));
    }
}
