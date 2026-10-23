package kr.gilmok.demo.seat.dto;

import kr.gilmok.demo.seat.entity.Seat;

public record SeatResponse(
        Long seatId,
        Long eventId,
        String section,
        int totalCount,
        int availableCount,
        int price
) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getEvent().getId(),
                seat.getSection(),
                seat.getTotalCount(),
                seat.getAvailableCount(),
                seat.getPrice()
        );
    }

    public static SeatResponse of(Seat seat, int redisAvailable) {
        return new SeatResponse(
                seat.getId(),
                seat.getEvent().getId(),
                seat.getSection(),
                seat.getTotalCount(),
                redisAvailable,
                seat.getPrice()
        );
    }
}
