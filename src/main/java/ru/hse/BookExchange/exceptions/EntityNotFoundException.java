package ru.hse.BookExchange.exceptions;

import java.util.Arrays;
import java.util.List;

/**
 * Ошибка отсутствия записи в бд
 */
public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(String entityName, Long id) {
    super("Could not find " + entityName + " with id = " + id);
  }

  public EntityNotFoundException(String entitiesName, List<Long> ids) {
    super("Could not find " + entitiesName + " with ids = " +
        Arrays.toString(ids.toArray()));
  }
}
