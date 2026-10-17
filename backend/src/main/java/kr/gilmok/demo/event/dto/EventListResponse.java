package kr.gilmok.demo.event.dto;

import kr.gilmok.demo.event.entity.Event;
import kr.gilmok.demo.event.entity.EventStatus;

import java.time.LocalDateTime;

public record EventListResponse(
        Long eventId,
        Long platformEventId,
        String name,
        String description,
        EventStatus status,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
    public static EventListResponse from(Event event) {
        return new EventListResponse(
                event.getId(),
                event.getPlatformEventId(),
                event.getName(),
                event.getDescription(),
                event.getStatus(),
                event.getStartsAt(),
                event.getEndsAt()
        );
    }
}
