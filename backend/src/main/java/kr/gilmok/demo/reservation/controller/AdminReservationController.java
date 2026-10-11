package kr.gilmok.demo.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.gilmok.demo.global.dto.ApiResponse;
import kr.gilmok.demo.reservation.dto.ReservationResponse;
import kr.gilmok.demo.reservation.dto.ReservationStatsResponse;
import kr.gilmok.demo.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/events/{eventId}")
@RequiredArgsConstructor
@Tag(name = "Admin Reservation", description = "관리자 예약 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class AdminReservationController {

    private final ReservationService reservationService;

    // --- 예약 현황 ---

    @GetMapping("/reservations")
    @Operation(summary = "이벤트 예약 목록 조회", description = "eventId에 대한 예약 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 없음")
    })
    public ApiResponse<List<ReservationResponse>> getReservations(@PathVariable Long eventId) {
        return ApiResponse.success(reservationService.getReservationsByEvent(eventId));
    }

    @GetMapping("/reservations/stats")
    @Operation(summary = "이벤트 예약 통계 조회", description = "eventId에 대한 예약 현황 통계를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 없음")
    })
    public ApiResponse<ReservationStatsResponse> getStats(@PathVariable Long eventId) {
        return ApiResponse.success(reservationService.getReservationStats(eventId));
    }
}
