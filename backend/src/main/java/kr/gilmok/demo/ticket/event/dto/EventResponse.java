package kr.gilmok.demo.ticket.event.dto;

import kr.gilmok.demo.ticket.event.entity.Event;
import kr.gilmok.demo.ticket.event.entity.EventStatus;

import java.time.LocalDateTime;

public record EventResponse(
        Long eventId,
        EventStatus status,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getStatus(),
                event.getName(),
                event.getDescription(),
                event.getCreatedAt()
        );
    }
}
