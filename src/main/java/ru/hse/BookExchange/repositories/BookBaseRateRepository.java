package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.BookBaseRate;

/**
 * Репозиторий оценок книг
 */
@Repository
@Transactional
public interface BookBaseRateRepository extends
    DatedEntityRepository<BookBaseRate> {

}


