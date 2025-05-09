package cn.guetzjb.drawguess.repository;

import cn.guetzjb.drawguess.entity.GameRound;
import org.springframework.data.repository.ListCrudRepository;

public interface GameRoundRepository extends ListCrudRepository<GameRound, Long> {
}
