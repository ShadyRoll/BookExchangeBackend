package ru.hse.BookExchange.models.abstractions;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.hse.BookExchange.models.User;

/**
 * Интерфейс записи, обладающей пользоватлем-создателем
 */
public interface Created {

  /**
   * Возвращает id создателя
   * @return id создателя
   */
  Long getCreatorId();

  /**
   * Возвращает создателя
   * @return создателя
   */
  @JsonIgnore
  User getCreator();

  /**
   * Устанавливат создателя
   * @param creator создатель
   */
  void setCreator(User creator);
}
