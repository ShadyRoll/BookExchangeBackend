package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.User;

/**
 * Репозиторий пользователей
 */
@Repository
@Transactional
public interface UserRepository extends DatedEntityRepository<User> {

  User findByUsername(String username);
}


