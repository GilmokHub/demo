package kr.gilmok.demo.reservation.service;

import kr.gilmok.demo.event.entity.Event;
import kr.gilmok.demo.event.entity.EventStatus;
import kr.gilmok.demo.event.repository.EventRepository;
import kr.gilmok.demo.reservation.dto.ReservationCreateRequest;
import kr.gilmok.demo.reservation.dto.ReservationResponse;
import kr.gilmok.demo.reservation.entity.Reservation;
import kr.gilmok.demo.reservation.repository.ReservationRepository;
import kr.gilmok.demo.reservation.repository.SeatLockRedisRepository;
import kr.gilmok.demo.seat.entity.Seat;
import kr.gilmok.demo.seat.repository.SeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationTotalPrice 단위 테스트 (TDD)")
class ReservationTotalPriceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SeatLockRedisRepository seatLockRedisRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("사용자가 좌석 예매를 완료할 때(조건), 좌석 단가와 예매 수량을 곱한 총 결제 금액(total_price) 스냅샷이 저장되어야 한다(기대 결과)")
    void createReservation_Success_PersistsTotalPriceSnapshot() {
        // Given
        ReflectionTestUtils.setField(reservationService, "seatLockTtlSeconds", 300);
        ReflectionTestUtils.setField(reservationService, "maxQuantity", 4);

        Long userId = 100L;
        String username = "tester";
        Long eventId = 1L;
        Long seatId = 10L;
        int unitPrice = 50000;
        int quantity = 3;
        int expectedTotalPrice = unitPrice * quantity; // 150,000원

        Event event = Event.builder()
                .name("싸이 흠뻑쇼 2026")
                .description("여름 콘서트")
                .startsAt(LocalDateTime.now().minusDays(1))
                .endsAt(LocalDateTime.now().plusDays(7))
                .build();
        event.open();
        ReflectionTestUtils.setField(event, "id", eventId);

        Seat seat = Seat.builder()
                .event(event)
                .section("VIP")
                .totalCount(100)
                .price(unitPrice)
                .build();
        ReflectionTestUtils.setField(seat, "id", seatId);

        ReservationCreateRequest request = new ReservationCreateRequest(eventId, seatId, quantity);

        given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
        given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));
        given(seatLockRedisRepository.lock(eq(eventId), eq(seatId), eq(userId), eq(quantity), eq(300)))
                .willReturn(true);

        given(reservationRepository.save(any(Reservation.class))).willAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });

        // When
        ReservationResponse response = reservationService.createReservation(userId, username, request);

        // Then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());

        Reservation savedReservation = captor.getValue();
        assertThat(savedReservation.getTotalPrice())
                .as("Reservation 엔티티에 계산된 총 결제 금액(total_price) 스냅샷이 null이 아니어야 합니다.")
                .isNotNull()
                .isEqualTo(expectedTotalPrice);

        assertThat(response.totalPrice()).isEqualTo(expectedTotalPrice);
    }
}
