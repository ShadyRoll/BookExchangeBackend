package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.Genre;

/**
 * Репозиторий жанров
 */
@Repository
@Transactional
public interface GenreRepository extends DatedEntityRepository<Genre> {

}


