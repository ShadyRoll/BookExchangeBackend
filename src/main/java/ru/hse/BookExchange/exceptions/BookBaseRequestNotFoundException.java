package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия запроса на добавление книги (bookBase) в бд
 */
public class BookBaseRequestNotFoundException extends EntityNotFoundException {

  public BookBaseRequestNotFoundException(Long id) {
    super("bookBase request", id);
  }
}

