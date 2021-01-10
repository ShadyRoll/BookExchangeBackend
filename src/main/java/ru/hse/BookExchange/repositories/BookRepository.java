package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.Book;

/**
 * Репозиторий фотографий книг для передачи (book)
 */
@Repository
@Transactional
public interface BookRepository extends DatedEntityRepository<Book> {

}


