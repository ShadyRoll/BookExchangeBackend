package ru.hse.BookExchange.controllers.abstractions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.hse.BookExchange.controllers.AuthenticationController;
import ru.hse.BookExchange.exceptions.EntityNotFoundException;
import ru.hse.BookExchange.models.Complaint;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.models.abstractions.DatedEntity;
import ru.hse.BookExchange.repositories.ComplaintRepository;
import ru.hse.BookExchange.repositories.DatedEntityRepository;

/**
 * Контроллер для записей с датой создания
 *
 * @param <T> - класс записи с датой создания
 */
public abstract class DatedEntityController<T extends DatedEntity> {

  /**
   * Репозиторий записей с датой создания
   */
  protected final DatedEntityRepository<T> repository;

  @Autowired
  protected AuthenticationController auth;
  @Autowired
  protected ComplaintRepository complaintRepository;


  public DatedEntityController(DatedEntityRepository<T> repository) {
    this.repository = repository;
  }


  /**
   * Возвращает список записей в бд
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id записей в бд (null, если не нужно)
   * @param authentication - данные авторизации
   * @return список записей в бд
   */
  @GetMapping()
  public List<T> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    if (ids != null) {
      List<T> res = new ArrayList<>(ids.size());//repository.findAllById(ids);
      // Чтобы гарантировать ту же последовательность id приходится собирать вручную
      for (int i = 0; i < ids.size(); i++) {
        final int finalI = i;
        res.add(repository.findById(ids.get(i))
            .orElseThrow(() -> new EntityNotFoundException("entity",
                ids.get(finalI))));
      }

      return res;
    }

    checkSkipAndLimit(skip, limit);
    var stream = repository.findAll().stream();
    if (latest) {
      stream = stream.sorted(Collections.reverseOrder());
    }
    return stream.skip(skip).limit(limit).collect(
        Collectors.toList());
  }

  @GetMapping("/{id}")
  public T getById(@PathVariable Long id, Authentication authentication) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("entity", id));
  }

  @PostMapping("/{id}/addComplaint")
  public Complaint addComplaint(@PathVariable Long id,
      @RequestBody Complaint complaint, Authentication authentication) {
    T entity = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("entity", id));
    complaint.setConnectedEntity(entity);

    User user = auth.isUser(authentication);
    complaint.setCreator(user);

    entity.getComplaints().add(complaint);
    repository.save(entity);
    return complaintRepository.save(complaint);
  }

  /**
   * Добавляет запись в бд
   *
   * @param entity         - запись
   * @param authentication - данные авторизации
   * @return сохраненная запись
   */
  @PostMapping()
  public T add(@RequestBody T entity,
      Authentication authentication) {
    auth.isUser(authentication);
    return repository.save(entity);
  }

  /**
   * Заменяет запись в бд
   *
   * @param newEntity      - новая запись
   * @param id             - id записи
   * @param authentication - данные аутентификации
   * @return сохраненная новая запись
   */
  @PutMapping("/{id}")
  public T replace(@RequestBody T newEntity, @PathVariable Long id,
      Authentication authentication) {
    //auth.isModerator(authentication);

    DatedEntity cur = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("entity", id));
    var fields = newEntity.getClass().getDeclaredFields();
    migrateFields(newEntity, cur, fields);

    if (newEntity.getClass().getSuperclass().getPackageName()
        .equals("ru.hse.BookExchange.models")) {
      fields = newEntity.getClass().getSuperclass().getDeclaredFields();
      migrateFields(newEntity, cur, fields);
    }

    return repository.findById(id)
        .map(entity -> {
          entity = newEntity;
          entity.setId(id);
          return repository.save(entity);
        })
        .orElseGet(() -> {
          newEntity.setId(id);
          return repository.save(newEntity);
        });
  }

  protected void migrateFields(DatedEntity newEntity, DatedEntity cur,
      Field[] fields) {
    try {
      for (var field : fields) {
        field.setAccessible(true);
        if (field.get(newEntity) == null || (
            field.get(newEntity) instanceof Collection
                && ((Collection<?>) field.get(newEntity)).size() == 0)) {
          field.set(newEntity, field.get(cur));
        }
      }
    } catch (IllegalAccessException ex) {
      System.out.println("IllegalAccessException");
    }
  }

  /**
   * Удаляет запись из бд
   *
   * @param id             - id записи
   * @param authentication - данные аутентификации
   */
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, Authentication authentication) {
    auth.isAdmin(authentication);
    if (repository.findById(id).isEmpty()) {
      throw new EntityNotFoundException("entity", id);
    }
    repository.deleteById(id);
  }

  public static void checkSkipAndLimit(int skip, int limit) {
    if (skip < 0 || limit < 0) {
      throw new IllegalArgumentException(
          "skip and limit arguments must cannot be negative!");
    }
  }
}


