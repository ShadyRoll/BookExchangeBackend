package ru.hse.BookExchange.controllers.abstractions;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.hse.BookExchange.exceptions.EntityNotFoundException;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.models.abstractions.Created;
import ru.hse.BookExchange.models.abstractions.DatedEntity;
import ru.hse.BookExchange.repositories.DatedEntityRepository;


/**
 * Контроллер для записей с владельцем и датой создания
 *
 * @param <T> - класс записи с датой создания и владельцем (создателем)
 */
public abstract class CreatedDatedEntityController<T extends DatedEntity & Created>
    extends DatedEntityController<T> {


  public CreatedDatedEntityController(DatedEntityRepository<T> repository) {
    super(repository);
  }

  /**
   * Проверяет, есть ли у пользователя доступ к записи
   *
   * @param id             - id записи
   * @param authentication - данные аутентификации
   * @return пользователя
   * @throws ForbiddenException - если доступа нет
   */
  protected User hasAccessToModify(@PathVariable Long id,
      Authentication authentication) throws ForbiddenException {
    // Это условие должно быть отловлено ранее
    if (authentication == null) {
      throw new ForbiddenException(
          "You have to be authorized to make this request!");
    }
    User user = auth.isUser(authentication);
    T entity = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("entity", id));
    if (!(entity.getCreator() == user) && !user.getRole()
        .hasAdminPermits()) {
      throw new ForbiddenException(
          "You can modify only your entity (or you must have admin permits)");
    }
    return user;
  }


  /**
   * Удаляет запись из бд
   *
   * @param id             - id записи
   * @param authentication - данные аутентификации
   */
  @Override
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, Authentication authentication) {
    if (repository.findById(id).isEmpty()) {
      throw new EntityNotFoundException("entity", id);
    }
    hasAccessToModify(id, authentication);

    repository.deleteById(id);
  }

  /**
   * Заменяет запись в бд
   *
   * @param newEntity      - новая запись
   * @param id             - id записи
   * @param authentication - данные аутентификации
   * @return сохраненная новая запись
   */
  @Override
  @PutMapping("/{id}")
  public T replace(@RequestBody T newEntity, @PathVariable Long id,
      Authentication authentication) {
    hasAccessToModify(id, authentication);
    //User user = hasAccessToModify(id, authentication);
    //newEntity.setCreator(user);
    //newEntity.setId(id);
    return super
        .replace(newEntity, id, authentication); //repository.save(newEntity);
  }
}


