package kr.gilmok.demo.ticket.user.service;

import kr.gilmok.demo.ticket.event.entity.Event;
import kr.gilmok.demo.ticket.event.repository.EventRepository;
import kr.gilmok.demo.ticket.reservation.repository.ReservationRepository;
import kr.gilmok.demo.ticket.user.dto.UserDashboardResponse;
import kr.gilmok.demo.ticket.user.dto.UserEventItemResponse;
import kr.gilmok.demo.ticket.user.dto.UserMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId, String username) {
        return UserMeResponse.of(userId, username);
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse getDashboard(Long userId) {
        long count = reservationRepository.countByUserId(userId);
        // queue 연동 전까지는 null. 연동 후 대기 중인 eventId 목록을 조회해 채운다.
        List<Long> waitingEventIds = null;
        return new UserDashboardResponse(count, waitingEventIds);
    }

    @Transactional(readOnly = true)
    public List<UserEventItemResponse> getMyEvents(Long userId) {
        List<Long> eventIds = reservationRepository.findDistinctEventIdsByUserIdOrderByCreatedAtDesc(userId);
        if (eventIds.isEmpty()) {
            return List.of();
        }
        List<Event> events = eventRepository.findAllById(eventIds);
        var eventById = events.stream().collect(Collectors.toMap(Event::getId, e -> e));
        return eventIds.stream()
                .map(id -> {
                    Event e = eventById.get(id);
                    return e != null ? new UserEventItemResponse(e.getId(), e.getName(), e.getStatus()) : null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
