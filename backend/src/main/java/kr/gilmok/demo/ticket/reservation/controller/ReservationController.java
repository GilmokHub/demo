package kr.gilmok.demo.ticket.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.gilmok.demo.ticket.reservation.dto.ReservationCreateRequest;
import kr.gilmok.demo.ticket.reservation.dto.ReservationResponse;
import kr.gilmok.demo.ticket.reservation.service.ReservationService;
import kr.gilmok.demo.global.dto.ApiResponse;
import kr.gilmok.demo.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "사용자 예약 API")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "예약 생성 (좌석 선점)", description = "대기열 입장 토큰(GilmokGuard에서 검증)을 기반으로 좌석 예약을 생성합니다.")
    public ApiResponse<ReservationResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ReservationCreateRequest request) {

        // GilmokAdmissionGuard가 사전에 토큰을 검증했다고 가정합니다.
        ReservationResponse res = reservationService.createReservation(principal.user().id(), principal.getUsername(), request);
        return ApiResponse.success(res);
    }

    @PostMapping("/{code}/confirm")
    @Operation(summary = "예약 확정", description = "결제 완료 후 예약을 확정합니다.")
    public ApiResponse<ReservationResponse> confirm(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String code) {

        // 예약 확정 (GilmokAdmissionGuard 검증 또는 내부 처리)
        ReservationResponse res = reservationService.confirmReservation(principal.user().id(), code);
        return ApiResponse.success(res);
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "예약 취소", description = "예약 코드를 기준으로 예약을 취소합니다.")
    public ApiResponse<ReservationResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String code) {
        return ApiResponse.success(reservationService.cancelReservation(principal.user().id(), code));
    }

    @GetMapping("/{code}")
    @Operation(summary = "예약 단건 조회", description = "예약 코드를 기준으로 예약 상세를 조회합니다.")
    public ApiResponse<ReservationResponse> getReservation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String code) {
        return ApiResponse.success(reservationService.getReservation(principal.user().id(), code));
    }

    @GetMapping("/my")
    @Operation(summary = "내 예약 목록 조회", description = "로그인한 사용자의 예약 목록을 조회합니다.")
    public ApiResponse<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.success(reservationService.getMyReservations(principal.user().id()));
    }
}
