package com.skylerdache.spacepong.repositories;

import com.skylerdache.spacepong.entities.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> {
    public Player findPlayerByUsername(String username);
}