package cn.guetzjb.drawguess.repository;

import cn.guetzjb.drawguess.entity.StartGame;
import org.springframework.data.repository.ListCrudRepository;

public interface StartGameRepository extends ListCrudRepository<StartGame, Long> {

    StartGame findFirstByRoomNameOrderByIdDesc(String roomName);

}
