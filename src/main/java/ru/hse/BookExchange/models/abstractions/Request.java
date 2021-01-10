package ru.hse.BookExchange.models.abstractions;

/**
 * Интерфейс запроса
 */
public interface Request extends Created {

  /**
   * Возвращает статус запроса
   *
   * @return статус запроса
   */
  RequestStatus getStatus();

  /**
   * Устанавливает статус запроса
   *
   * @param status статус запроса
   */
  void setStatus(RequestStatus status);

  /**
   * Статусы запросов
   */
  enum RequestStatus {
    Pending, Accepted, Rejected
  }

}
