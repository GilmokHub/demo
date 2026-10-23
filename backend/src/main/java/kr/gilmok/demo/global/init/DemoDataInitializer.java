package kr.gilmok.demo.global.init;

import kr.gilmok.demo.event.entity.Event;
import kr.gilmok.demo.event.entity.EventStatus;
import kr.gilmok.demo.event.repository.EventRepository;
import kr.gilmok.demo.seat.entity.Seat;
import kr.gilmok.demo.seat.repository.SeatRepository;
import kr.gilmok.demo.seat.service.SeatService;
import kr.gilmok.demo.user.entity.User;
import kr.gilmok.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final SeatService seatService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            User admin = User.createAdmin("admin", passwordEncoder.encode("admin1234!"));
            User user1 = User.createNewUser("user1", passwordEncoder.encode("user1234!"));
            userRepository.saveAll(List.of(admin, user1));
            log.info("[DemoDataInitializer] 기본 테스트 계정(admin, user1) 시드 생성 완료");
        }

        if (eventRepository.count() == 0) {
            Event event = Event.builder()
                    .name("아이유 콘서트 2026")
                    .description("2026 아이유 월드투어 서울 단독 콘서트")
                    .startsAt(LocalDateTime.now().minusDays(1))
                    .endsAt(LocalDateTime.now().plusDays(30))
                    .platformEventId("DEMO-CONCERT-2026")
                    .build();
            event.open();
            Event savedEvent = eventRepository.save(event);

            Seat vip = Seat.builder().event(savedEvent).section("VIP").totalCount(100).price(150000).build();
            Seat r = Seat.builder().event(savedEvent).section("R").totalCount(200).price(120000).build();
            Seat s = Seat.builder().event(savedEvent).section("S").totalCount(300).price(90000).build();
            seatRepository.saveAll(List.of(vip, r, s));

            seatService.initRedisAvailable(savedEvent.getId());
            log.info("[DemoDataInitializer] 기본 공연(id={}) 및 좌석 구역(VIP/R/S) 시드 생성 완료", savedEvent.getId());
        } else {
            eventRepository.findByStatusOrderByStartsAtDesc(EventStatus.OPEN)
                    .forEach(e -> seatService.initRedisAvailable(e.getId()));
        }
    }
}
