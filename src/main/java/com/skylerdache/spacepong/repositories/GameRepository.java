package com.skylerdache.spacepong.repositories;

import com.skylerdache.spacepong.entities.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends CrudRepository<Game, Long> {}
