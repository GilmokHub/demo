package kr.gilmok.demo.event.dto;

import kr.gilmok.demo.event.entity.Event;
import kr.gilmok.demo.event.entity.EventStatus;

import java.time.LocalDateTime;

public record EventResponse(
        Long eventId,
        Long platformEventId,
        EventStatus status,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getPlatformEventId(),
                event.getStatus(),
                event.getName(),
                event.getDescription(),
                event.getCreatedAt()
        );
    }
}
