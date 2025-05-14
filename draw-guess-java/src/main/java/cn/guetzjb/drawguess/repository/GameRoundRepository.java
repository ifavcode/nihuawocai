package cn.guetzjb.drawguess.repository;

import cn.guetzjb.drawguess.entity.GameRound;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface GameRoundRepository extends ListCrudRepository<GameRound, Long> {

    List<GameRound> findTop12ByImageUrlNotNullOrderByIdDesc();

}
