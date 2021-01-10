package ru.hse.BookExchange.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.DatedEntityController;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.exceptions.NullIdException;
import ru.hse.BookExchange.exceptions.UserNotFoundException;
import ru.hse.BookExchange.models.Dialog;
import ru.hse.BookExchange.models.Message;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.DialogRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Контроллер диалогов
 */
@RestController
@RequestMapping("dialog")
public class DialogController extends DatedEntityController<Dialog> {

  // Репозиторий пользователей
  private final UserRepository userRepository;
  // Контроллер сообщений
  private final MessageController messageController;

  DialogController(DialogRepository repository,
      MessageController messageController,
      UserRepository userRepository) {
    super(repository);
    this.userRepository = userRepository;
    this.messageController = messageController;
  }

  /**
   * Возвращает список диалогоы
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id диалогов (null, если не нужно)
   * @param authentication - данные авторизации
   * @return список диалогов
   */
  @Override
  @GetMapping()
  public List<Dialog> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    List<Dialog> res = super.all(skip, limit, latest, ids, authentication);

    if (user.getRole().hasModeratorPermits()) {
      return res;
    }

    // Пользователь может видеть только свои диалоги
    res = res.stream()
        .filter(dialog -> dialog.getParticipants().contains(user))
        .collect(Collectors.toList());

    return res;
  }

  /**
   * Возвращает диалог по его id
   *
   * @param id             id диалога
   * @param authentication данные аутентификации
   * @return диалог
   */
  @Override
  @GetMapping("/{id}")
  public Dialog getById(@PathVariable Long id,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    Dialog dialog = super.getById(id, authentication);
    if (user.getRole().hasModeratorPermits() || dialog.getParticipants()
        .contains(user)) {
      return dialog;
    }
    throw new ForbiddenException(
        "You can't access not your dialogs.");
  }

  /**
   * Добавляет диалог в бд
   *
   * @param dialog         - диалог
   * @param authentication - данные авторизации
   * @return сохраненный диалог
   */
  @Override
  @PostMapping
  public Dialog add(@RequestBody Dialog dialog,
      Authentication authentication) {
    auth.isUser(authentication);

    setDialogReferences(dialog, authentication);

    return repository.save(dialog);
  }

  /**
   * Заменяет диалог в бд
   *
   * @param newDialog      - новый диалог
   * @param id             - id диалога
   * @param authentication - данные аутентификации
   * @return сохраненный новый диалог
   */
  @Override
  @PutMapping("/{id}")
  public Dialog replace(@RequestBody Dialog newDialog,
      @PathVariable Long id, Authentication authentication) {
    User user = auth.isAdmin(authentication);
    setDialogReferences(newDialog, authentication);
    newDialog.setId(id);
    return repository.save(newDialog);
  }

  /**
   * Удаляет диалог из бд
   *
   * @param id             - id диалога
   * @param authentication - данные аутентификации
   */
  @Override
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, Authentication authentication) {
    auth.isAdmin(authentication);
    super.delete(id, authentication);
  }

  /**
   * Устанавливает зависимости диалога
   * @param dialog диалог
   * @param authentication данные аутентификации
   */
  private void setDialogReferences(Dialog dialog,
      Authentication authentication) {
    List<Long> participantIds = dialog.getParticipantIds();
    if (participantIds == null) {
      throw new NullIdException("participantIds");
    }
    if (participantIds.size() != 2) {
      throw new IllegalArgumentException(
          "There must be 2 participants in dialog! (you specified "
              + participantIds.size() + " users)");
    }
    if (participantIds.get(0).equals(participantIds.get(1))) {
      throw new IllegalArgumentException(
          "Both participants can't have same id! (both id's were "
              + participantIds.get(0) + ')');
    }
    List<User> participants = new ArrayList<>();
    for (Long participantId : participantIds) {
      User participant = userRepository.findById(participantId)
          .orElseThrow(() -> new UserNotFoundException(participantId));
      participants.add(participant);
    }

    dialog.setParticipants(participants);

    dialog = repository.save(dialog);

    Message initialMessage = dialog.getInitialMessage();
    if (initialMessage != null) {
      initialMessage = messageController.add(initialMessage, authentication);
      dialog.addMessage(initialMessage);
      repository.save(dialog);
    }
  }
}


