package kr.gilmok.demo.seat.entity;

import jakarta.persistence.*;
import kr.gilmok.demo.event.entity.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 10)
    private String section;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "reserved_count", nullable = false)
    private int reservedCount;

    @Column(nullable = false)
    private int price;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Seat(Event event, String section, int totalCount, int price) {
        this.event = event;
        this.section = section;
        this.totalCount = totalCount;
        this.reservedCount = 0;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getAvailableCount() {
        return totalCount - reservedCount;
    }

    public void reserve(int count) {
        if (reservedCount + count > totalCount) {
            throw new IllegalStateException("잔여석이 부족합니다.");
        }
        this.reservedCount += count;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelReservation(int count) {
        this.reservedCount = Math.max(0, this.reservedCount - count);
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String section, int totalCount, int price) {
        this.section = section;
        this.totalCount = totalCount;
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }
}
