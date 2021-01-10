package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия города в бд
 */
public class TownNotFoundException extends EntityNotFoundException {

  public TownNotFoundException(Long id) {
    super("town", id);
  }
}
