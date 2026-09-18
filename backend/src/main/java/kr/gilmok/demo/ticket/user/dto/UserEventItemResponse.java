package kr.gilmok.demo.ticket.user.dto;

import kr.gilmok.demo.ticket.event.entity.EventStatus;

public record UserEventItemResponse(
        Long eventId,
        String eventName,
        EventStatus status
) {
}
