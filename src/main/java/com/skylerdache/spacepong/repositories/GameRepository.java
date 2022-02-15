package com.skylerdache.spacepong.repositories;//        System.out.println("Let's inspect the beans provided by Spring Boot:");


import com.skylerdache.spacepong.entities.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends CrudRepository<Game, Long> {}
