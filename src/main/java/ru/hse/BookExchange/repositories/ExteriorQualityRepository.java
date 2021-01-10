package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.ExteriorQuality;

/**
 * Репозиторий уровня поношенности книг
 */
@Repository
@Transactional
public interface ExteriorQualityRepository extends
    DatedEntityRepository<ExteriorQuality> {

}


