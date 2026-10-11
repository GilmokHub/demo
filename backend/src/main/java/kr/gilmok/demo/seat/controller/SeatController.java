package kr.gilmok.demo.seat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.gilmok.demo.seat.dto.SeatResponse;
import kr.gilmok.demo.seat.service.SeatService;
import kr.gilmok.demo.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Seat", description = "좌석 조회 API")
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/events/{eventId}/seats")
    @Operation(summary = "좌석 목록 조회", description = "eventId에 해당하는 좌석 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 없음")
    })
    public ApiResponse<List<SeatResponse>> getSeats(@PathVariable Long eventId) {
        return ApiResponse.success(seatService.getSeatsByEvent(eventId));
    }
}
