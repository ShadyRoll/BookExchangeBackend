package ru.hse.BookExchange.exceptions;

/**
 * Ошибка недопустимого состояния запроса
 */
public class IllegalRequestStatusState extends IllegalStateException {

  public IllegalRequestStatusState(String s) {
    super(s);
  }
}
