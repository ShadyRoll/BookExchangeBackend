package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия уровня поношенности книги для обмена в бд
 */
public class ExteriorQualityNotFoundException extends EntityNotFoundException {

  public ExteriorQualityNotFoundException(Long id) {
    super("exterior quality", id);
  }
}
