package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия id (было null)
 */
public class NullIdException extends RuntimeException {

  public NullIdException(String field) {
    super(String.format("Missing %s field! (%s can't be null)", field, field));
  }
}
