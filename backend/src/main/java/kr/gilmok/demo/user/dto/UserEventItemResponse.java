package kr.gilmok.demo.user.dto;

import kr.gilmok.demo.event.entity.EventStatus;

public record UserEventItemResponse(
        Long eventId,
        String eventName,
        EventStatus status
) {
}
