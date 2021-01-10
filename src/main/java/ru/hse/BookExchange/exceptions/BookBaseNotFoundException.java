package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия книги (bookBase) в бд
 */
public class BookBaseNotFoundException extends EntityNotFoundException {

  public BookBaseNotFoundException(Long id) {
    super("bookBase", id);
  }
}

