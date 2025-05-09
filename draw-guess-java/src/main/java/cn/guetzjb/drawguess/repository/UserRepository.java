package cn.guetzjb.drawguess.repository;

import cn.guetzjb.drawguess.entity.User;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface UserRepository extends ListPagingAndSortingRepository<User, Long>, ListCrudRepository<User, Long> {

    User findByUsername(String username);

    Long countByUsername(String username);

}
