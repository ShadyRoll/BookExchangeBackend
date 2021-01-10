package ru.hse.BookExchange.controllers;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.CreatedDatedRequestController;
import ru.hse.BookExchange.exceptions.ComplaintNotFoundException;
import ru.hse.BookExchange.exceptions.IllegalRequestStatusState;
import ru.hse.BookExchange.models.Complaint;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.models.abstractions.Request.RequestStatus;
import ru.hse.BookExchange.repositories.ComplaintRepository;

/**
 * Контроллер жалоб
 */
@RestController
@RequestMapping("complaint")
public class ComplaintController extends
    CreatedDatedRequestController<Complaint> {

  ComplaintController(ComplaintRepository repository) {
    super(repository);
  }

  /**
   * Возвращает список жалоб
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id жалоб (null, если не нужно)
   * @param authentication - данные авторизации
   * @return список жалоб
   */
  @Override
  @GetMapping
  protected List<Complaint> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false, defaultValue = "false") boolean pendingOnly,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    auth.isModerator(authentication);
    return super.all(skip, limit, latest, pendingOnly, ids, authentication);
  }

  /**
   * Добавляет жалобу в бд
   *
   * @param entity         - жалоба
   * @param authentication - данные авторизации
   * @return сохраненная жалоба
   */
  @Override
  @PostMapping
  public Complaint add(@RequestBody Complaint entity,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    entity.setCreator(user);
    return repository.save(entity);
  }

  /**
   * Одобряет жалобу
   *
   * @param id             - id жалобы
   * @param authentication - данные авторизации
   * @return одобренная жалоба
   */
  @Override
  @PatchMapping("/{id}/accept")
  protected Complaint accept(@PathVariable Long id,
      Authentication authentication) {
    auth.isModerator(authentication);
    Complaint complaint = repository.findById(id)
        .orElseThrow(() -> new ComplaintNotFoundException(id));

    if (complaint.getStatus().equals(RequestStatus.Accepted)) {
      throw new IllegalRequestStatusState("Complaint is already accepted!");
    }

    // Обновим статус запроса
    complaint.setStatus(RequestStatus.Accepted);
    return repository.save(complaint);
  }

  /**
   * Отклоняет жалобу
   *
   * @param id             - id жалобы
   * @param authentication - данные авторизации
   * @return отклоненная жалоба
   */
  @Override
  @PatchMapping("/{id}/reject")
  protected Complaint reject(@PathVariable Long id,
      Authentication authentication) {
    auth.isModerator(authentication);
    Complaint complaint = repository.findById(id)
        .orElseThrow(() -> new ComplaintNotFoundException(id));

    if (!complaint.getStatus().equals(RequestStatus.Pending)) {
      throw new IllegalRequestStatusState(
          "You can reject only pending complaint.");
    }

    // Обновим статус запроса
    complaint.setStatus(RequestStatus.Rejected);
    return repository.save(complaint);
  }

  // Этот метод вызывать не нужно
  @Override
  @PostMapping("/{id}/addComplaint")
  public Complaint addComplaint(Long id, Complaint complaint,
      Authentication authentication) {
    throw new UnsupportedOperationException(
        "You really want to add complaint to complaint?))");
  }
}


