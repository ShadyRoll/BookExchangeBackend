package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.BookBaseRequest;

/**
 * Репозиторий запросов на добавление книг (bookBase)
 */
@Repository
@Transactional
public interface BookBaseRequestRepository extends
    DatedEntityRepository<BookBaseRequest> {

}


