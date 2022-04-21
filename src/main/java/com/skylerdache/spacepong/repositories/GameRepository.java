package com.skylerdache.spacepong.repositories;//        System.out.println("Let's inspect the beans provided by Spring Boot:");


import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.entities.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends CrudRepository<GameEntity, Long> {
    public List<GameEntity> findGamesByPlayer1(Player p);
    public List<GameEntity> findGamesByPlayer2(Player p);
}
