package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия запроса на передачу книги в бд
 */
public class BookExchangeRequestNotFoundException extends
    EntityNotFoundException {

  public BookExchangeRequestNotFoundException(Long id) {
    super("book exchange request", id);
  }
}

