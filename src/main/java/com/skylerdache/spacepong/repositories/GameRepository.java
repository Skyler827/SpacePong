package com.skylerdache.spacepong.repositories;//        System.out.println("Let's inspect the beans provided by Spring Boot:");


import com.skylerdache.spacepong.entities.Game;
import com.skylerdache.spacepong.entities.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends CrudRepository<Game, Long> {
    public List<Game> findGamesByPlayer1(Player p);
    public List<Game> findGamesByPlayer2(Player p);
}
