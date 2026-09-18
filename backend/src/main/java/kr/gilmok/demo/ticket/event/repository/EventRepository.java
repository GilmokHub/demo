package kr.gilmok.demo.ticket.event.repository;

import kr.gilmok.demo.ticket.event.entity.Event;
import kr.gilmok.demo.ticket.event.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByCreatedAtDesc();

    List<Event> findByStatusOrderByStartsAtDesc(EventStatus status);
}
