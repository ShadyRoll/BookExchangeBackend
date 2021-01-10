package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия жанра в бд
 */
public class GenreNotFoundException extends EntityNotFoundException {

  public GenreNotFoundException(Long id) {
    super("genre", id);
  }
}
