package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.BookBasePhoto;

/**
 * Репозиторий книг (bookBase)
 */
@Repository
@Transactional
public interface BookBasePhotoRepository extends
    DatedEntityRepository<BookBasePhoto> {

}


