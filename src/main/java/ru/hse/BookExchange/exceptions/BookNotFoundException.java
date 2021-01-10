package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия книги для обмена (book) в бд
 */
public class BookNotFoundException extends EntityNotFoundException {

  public BookNotFoundException(Long id) {
    super("book", id);
  }
}
