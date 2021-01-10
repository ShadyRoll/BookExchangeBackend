package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.BookPhoto;

/**
 * Репозиторий фотографий книг для передачи (book)
 */
@Repository
@Transactional
public interface BookPhotoRepository extends DatedEntityRepository<BookPhoto> {

}


