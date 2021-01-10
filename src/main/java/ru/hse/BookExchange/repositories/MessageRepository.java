package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.Message;

/**
 * Репозиторий сообщений пользователей
 */
@Repository
@Transactional
public interface MessageRepository extends DatedEntityRepository<Message> {

}


