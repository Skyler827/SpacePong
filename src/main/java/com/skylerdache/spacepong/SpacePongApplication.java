package com.skylerdache.spacepong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.dto.PlayerDto;
import com.skylerdache.spacepong.services.PlayerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

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
        ObjectMapper m = new ObjectMapper();
        GameStateDto gsdto1 = new GameStateDto(
            true,
            0,
            0,
            Instant.now().toString(),
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
        );
        try {
            System.out.println(m.writeValueAsString(gsdto1));
        } catch (JsonProcessingException e) {
            System.out.println("Json processing Exception");
        }
        try {
            String gsJsonString = "{\"paused\":false,\"p1Score\":0,\"p2Score\":0,\"p1PaddleX\":0.0,\"p1PaddleY\":0.0,\"p1PaddleZ\":0.0,\"p1PaddleVx\":0.0,\"p1PaddleVy\":0.0,\"p1PaddleVz\":0.0,\"p2PaddleX\":0.0,\"p2PaddleY\":0.0,\"p2PaddleZ\":0.0,\"p2PaddleVx\":0.0,\"p2PaddleVy\":0.0,\"p2PaddleVz\":0.0,\"ballX\":0.0,\"ballY\":0.0,\"ballZ\":0.0,\"ballVx\":0.0,\"ballVy\":0.0,\"ballVz\":0.0}";
            InputStream gsFileStream = new ByteArrayInputStream(gsJsonString.getBytes());
            GameStateDto gsdto2 = m.readValue(gsFileStream, GameStateDto.class);
            System.out.println(gsdto2.ballVz());
            JSONObject jsonObj = new JSONObject();

        } catch (IOException e) {
            System.out.println("IO Exception");
        }
        PlayerControlMessage pcm = new PlayerControlMessage();
        System.out.println(pcm.getJson());
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
    @Bean()
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(5);
    }
}
