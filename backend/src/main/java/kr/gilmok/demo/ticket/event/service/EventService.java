package kr.gilmok.demo.ticket.event.service;

import kr.gilmok.demo.ticket.event.dto.EventCreateRequest;
import kr.gilmok.demo.ticket.event.dto.EventListResponse;
import kr.gilmok.demo.ticket.event.dto.EventResponse;
import kr.gilmok.demo.ticket.event.entity.Event;
import kr.gilmok.demo.ticket.event.entity.EventStatus;
import kr.gilmok.demo.ticket.event.repository.EventRepository;
import kr.gilmok.demo.ticket.event.exception.EventErrorCode;
import kr.gilmok.demo.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventResponse createEvent(EventCreateRequest dto) {
        Event event = Event.builder()
                .name(dto.name())
                .description(dto.description())
                .startsAt(dto.startsAt())
                .endsAt(dto.endsAt())
                .build();

        Event saved = eventRepository.save(event);
        return EventResponse.from(saved);
    }

    @Transactional
    public EventResponse openEvent(Long eventId) {
        Event event = findEventById(eventId);
        event.open(); // 엔티티 내부 비즈니스 메서드 호출
        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse closeEvent(Long eventId) {
        Event event = findEventById(eventId);
        event.close();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                }
            });
        } else {
        }
        return EventResponse.from(event);
    }

    public List<EventListResponse> getOpenEvents() {
        return eventRepository.findByStatusOrderByStartsAtDesc(EventStatus.OPEN)
                .stream()
                .map(EventListResponse::from)
                .toList();
    }

    public EventResponse getEvent(Long eventId) {
        Event event = findEventById(eventId);
        return EventResponse.from(event);
    }

    public List<EventResponse> getEvents() {
        return eventRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(EventResponse::from)
                .toList();
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));
    }
}
