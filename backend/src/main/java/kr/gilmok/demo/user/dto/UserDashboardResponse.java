package kr.gilmok.demo.user.dto;

import java.util.List;

public record UserDashboardResponse(
        long reservationCount,
        List<Long> waitingEventIds
) {
}
