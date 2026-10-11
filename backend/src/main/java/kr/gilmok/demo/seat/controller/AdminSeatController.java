package kr.gilmok.demo.seat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.gilmok.demo.global.dto.ApiResponse;
import kr.gilmok.demo.seat.dto.SeatCreateRequest;
import kr.gilmok.demo.seat.dto.SeatResponse;
import kr.gilmok.demo.seat.dto.SeatUpdateRequest;
import kr.gilmok.demo.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/events/{eventId}/seats")
@RequiredArgsConstructor
@Tag(name = "Admin Seat", description = "관리자 좌석 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class AdminSeatController {

    private final SeatService seatService;

    @PostMapping
    @Operation(summary = "좌석 생성", description = "eventId에 좌석을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 없음")
    })
    public ApiResponse<SeatResponse> createSeat(
            @PathVariable Long eventId,
            @Valid @RequestBody SeatCreateRequest request) {
        return ApiResponse.success(seatService.createSeat(eventId, request));
    }

    @PutMapping("/{seatId}")
    @Operation(summary = "좌석 수정", description = "eventId의 특정 좌석 정보를 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 또는 좌석 없음")
    })
    public ApiResponse<SeatResponse> updateSeat(
            @PathVariable Long eventId,
            @PathVariable Long seatId,
            @Valid @RequestBody SeatUpdateRequest request) {
        return ApiResponse.success(seatService.updateSeat(eventId, seatId, request));
    }

    @DeleteMapping("/{seatId}")
    @Operation(summary = "좌석 삭제", description = "eventId의 특정 좌석을 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 또는 좌석 없음")
    })
    public ApiResponse<Void> deleteSeat(
            @PathVariable Long eventId,
            @PathVariable Long seatId) {
        seatService.deleteSeat(eventId, seatId);
        return ApiResponse.success(null);
    }

    @PostMapping("/init-redis")
    @Operation(summary = "좌석 재고 Redis 초기화", description = "해당 이벤트의 Redis 좌석 재고를 초기화합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "초기화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 없음")
    })
    public ApiResponse<Void> initRedis(@PathVariable Long eventId) {
        seatService.initRedisAvailable(eventId);
        return ApiResponse.success(null);
    }
}
