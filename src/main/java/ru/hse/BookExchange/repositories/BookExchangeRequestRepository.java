package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.BookExchangeRequest;

/**
 * Репозиторий запросов на  пользователей
 */
@Repository
@Transactional
public interface BookExchangeRequestRepository extends
    DatedEntityRepository<BookExchangeRequest> {

}


