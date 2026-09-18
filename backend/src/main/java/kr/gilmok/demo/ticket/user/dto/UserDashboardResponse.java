package kr.gilmok.demo.ticket.user.dto;

import java.util.List;

public record UserDashboardResponse(
        long reservationCount,
        List<Long> waitingEventIds
) {
}
