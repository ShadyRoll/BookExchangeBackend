package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.Town;

/**
 * Репозиторий городов
 */
@Repository
@Transactional
public interface TownRepository extends DatedEntityRepository<Town> {

}


