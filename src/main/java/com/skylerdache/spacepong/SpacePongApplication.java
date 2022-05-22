package com.skylerdache.spacepong;

import com.skylerdache.spacepong.dto.PlayerDto;
import com.skylerdache.spacepong.services.PlayerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableScheduling
public class SpacePongApplication {
    private final PlayerService playerService;
    public SpacePongApplication(PlayerService playerService) {
        this.playerService = playerService;
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SpacePongApplication.class, args);

//        System.out.println("Let's inspect the beans provided by Spring Boot:");
//        String[] beanNames = ctx.getBeanDefinitionNames();
//        Arrays.sort(beanNames);
//        for (String beanName : beanNames) {
//            System.out.println(beanName);
//        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startUp() {
        PlayerDto user1 = new PlayerDto();
        user1.setUsername("user1");
        user1.setPassword("pass");
        user1.setColorHex("#AA4444");
        playerService.registerNewPlayer(user1);
        PlayerDto user2 = new PlayerDto();
        user2.setUsername("user2");
        user2.setPassword("pass");
        user2.setColorHex("#4444BB");
        playerService.registerNewPlayer(user2);
        PlayerDto user3 = new PlayerDto();
        user3.setUsername("user3");
        user3.setPassword("pass");
        user3.setColorHex("#44CC33");
        playerService.registerNewPlayer(user3);
    }
//    @Bean()
//    public Executor taskScheduler() {
//        return Executors.newScheduledThreadPool(5);
//    }
}
