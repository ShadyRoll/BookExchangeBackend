package ru.hse.BookExchange.exceptions;

/**
 * Ошибка отсутствия жалобы в бд
 */
public class ComplaintNotFoundException extends EntityNotFoundException {

  public ComplaintNotFoundException(Long id) {
    super("complaint", id);
  }
}
