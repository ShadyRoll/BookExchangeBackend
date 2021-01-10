package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия пользователя в бд
 */
public class UserNotFoundException extends EntityNotFoundException {

  public UserNotFoundException(Long id) {
    super("user", id);
  }
}
