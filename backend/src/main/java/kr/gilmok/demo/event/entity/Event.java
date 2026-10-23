package kr.gilmok.demo.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "platform_event_id", length = 50)
    private String platformEventId;

    @Builder
    public Event(String name, String description, LocalDateTime startsAt,
                 LocalDateTime endsAt, String platformEventId) {
        this.name = name;
        this.description = description;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.platformEventId = platformEventId;
        this.status = EventStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return this.status == EventStatus.OPEN;
    }

    public void open() {
        this.status = EventStatus.OPEN;
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = EventStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePlatformEventId(String platformEventId) {
        this.platformEventId = platformEventId;
        this.updatedAt = LocalDateTime.now();
    }
}