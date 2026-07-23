package cn.guetzjb.drawguess.repository;

import cn.guetzjb.drawguess.entity.GameRound;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface GameRoundRepository extends ListCrudRepository<GameRound, Long> {

    @Query("SELECT g FROM GameRound g WHERE g.imageUrl IS NOT NULL AND g.imageUrl <> '' ORDER BY g.id DESC LIMIT 12")
    List<GameRound> findTop12ValidImage();

    @Query("SELECT g FROM GameRound g WHERE g.imageUrl IS NOT NULL AND g.imageUrl <> '' AND g.user.id = :userId ORDER BY g.id DESC LIMIT 12")
    List<GameRound> findTop12ValidImageProfile(Long userId);

}
