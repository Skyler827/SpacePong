package com.skylerdache.spacepong.repositories;

import com.skylerdache.spacepong.entities.HumanPlayer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HumanPlayerRepository extends CrudRepository<HumanPlayer, Long> {
    public HumanPlayer getHumanPlayerByUsername(String username);
}
