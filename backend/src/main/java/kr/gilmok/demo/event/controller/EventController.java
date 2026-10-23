package kr.gilmok.demo.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.gilmok.demo.event.dto.EventListResponse;
import kr.gilmok.demo.event.service.EventService;
import kr.gilmok.demo.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "이벤트 조회 API")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "오픈 이벤트 목록 조회", description = "현재 오픈 상태인 이벤트 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ApiResponse<List<EventListResponse>> getOpenEvents() {
        return ApiResponse.success(eventService.getOpenEvents());
    }
}
