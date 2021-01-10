package ru.hse.BookExchange.controllers.abstractions;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.hse.BookExchange.models.abstractions.DatedEntity;
import ru.hse.BookExchange.models.abstractions.Request;
import ru.hse.BookExchange.models.abstractions.Request.RequestStatus;
import ru.hse.BookExchange.repositories.DatedEntityRepository;


/**
 * Контроллер для записей с владельцем и датой создания, являющихся запросом
 *
 * @param <T> - класс записи с датой создания и владельцем, являющаяся запросом
 */
public abstract class CreatedDatedRequestController<T extends DatedEntity
    & Request> extends CreatedDatedEntityController<T> {

  public CreatedDatedRequestController(DatedEntityRepository<T> repository) {
    super(repository);
  }

  /**
   * Заглушка (данный метод не должен вызывываться спрингом)
   */
  @Override
  @GetMapping("dontUseThisMapping")
  public List<T> all(int skip, int limit, boolean latest, List<Long> ids,
      Authentication authentication) {
    return super.all(skip, limit, latest, ids, authentication);
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
  protected List<T> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false, defaultValue = "false") boolean pendingOnly,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    List<T> res = super.all(skip, limit, latest, ids, authentication);
    if (pendingOnly) {
      res = res.stream()
          .filter(req -> req.getStatus().equals(RequestStatus.Pending))
          .collect(Collectors.toList());
    }
    return res;
  }

  /**
   * Одобряет запрос
   *
   * @param id             - id запроса
   * @param authentication - данные авторизации
   * @return одобренный запрос
   */
  protected abstract T accept(@PathVariable Long id,
      Authentication authentication);

  /**
   * Отклоняет запрос
   *
   * @param id             - id запроса
   * @param authentication - данные авторизации
   * @return отклоненный запрос
   */
  protected abstract T reject(@PathVariable Long id,
      Authentication authentication);
}


