package ru.hse.BookExchange.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.CreatedDatedEntityController;
import ru.hse.BookExchange.exceptions.EntityNotFoundException;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.exceptions.NullIdException;
import ru.hse.BookExchange.exceptions.UserNotFoundException;
import ru.hse.BookExchange.models.Dialog;
import ru.hse.BookExchange.models.Message;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.DialogRepository;
import ru.hse.BookExchange.repositories.MessageRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Контроллер сообщений
 */
@RestController
@RequestMapping("message")
public class MessageController extends CreatedDatedEntityController<Message> {

  // Репозиторий пользователей
  private final UserRepository userRepository;
  // Репозиторий диалогов
  private final DialogRepository dialogRepository;

  MessageController(MessageRepository repository,
      UserRepository userRepository, DialogRepository dialogRepository) {
    super(repository);
    this.userRepository = userRepository;
    this.dialogRepository = dialogRepository;
  }

  /**
   * Возвращает список сообщений
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id сообщений (null, если не нужно)
   * @param authentication - данные авторизации
   * @return список сообщений
   */
  @Override
  @GetMapping()
  public List<Message> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    List<Message> res = super.all(skip, limit, latest, ids, authentication);

    if (user.getRole().hasModeratorPermits()) {
      return res;
    }

    /* Пользователь может видеть только те сообщения, что были отправлены им
       или были адресованы ему */
    res = res.stream()
        .filter(message -> message.getCreator() == user
            || message.getReceiver() == user)
        .collect(Collectors.toList());

    return res;
  }

  /**
   * Возвращает сообщение по id
   *
   * @param id             id сообщения
   * @param authentication данные аутентификации
   * @return сообщение
   */
  @Override
  @GetMapping("/{id}")
  public Message getById(@PathVariable Long id,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    Message message = super.getById(id, authentication);
    if (message.getCreator() == user
        || message.getReceiver() == user) {
      return message;
    }
    throw new ForbiddenException(
        "You must be creator(sender) or receiver of this message to have "
            + "access to it.");
  }

  /**
   * Добавляет сообщение в бд
   *
   * @param message        - сообщение
   * @param authentication - данные авторизации
   * @return сохраненное сообщение
   */
  @Override
  @PostMapping
  public Message add(@RequestBody Message message,
      Authentication authentication) {
    User user = auth.isUser(authentication);

    setMessageRelations(message, user);

    return repository.save(message);
  }

  /**
   * Заменяет сообщение в бд
   *
   * @param newMessage     - новое сообщение
   * @param id             - id сообщения
   * @param authentication - данные аутентификации
   * @return сохраненное новое сообщение
   */
  @Override
  @PutMapping("/{id}")
  public Message replace(@RequestBody Message newMessage,
      @PathVariable Long id, Authentication authentication) {
    User user = hasAccessToModify(id, authentication);
    setMessageRelations(newMessage, user);
    newMessage.setId(id);
    return repository.save(newMessage);
  }

  /**
   * Устанавливает связи в бд для сообщения
   *
   * @param message сообщение
   * @param sender  пользователь, отправивший сообщение
   */
  public void setMessageRelations(Message message, User sender) {
    Long creatorId = message.getCreatorId();
    Long dialogId = message.getDialogId();
    Long receiverId = message.getReceiverId();

    // Если creatorId не указан, значит пользователь, отправляет сообщение от своего имени
    if (creatorId == null) {
      creatorId = sender.getId();
      message.setCreatorId(creatorId);
    } else if (!sender.getRole().hasAdminPermits() && !sender.getId()
        .equals(creatorId)) {
      /* Отправлять сообещние от чужого имени может только админ */
      throw new ForbiddenException(
          "You can't send message from other users to someone, while "
              + "you are not admin. (creatorId must much your id, or you can "
              + "leave it empty, it will be automatically set to your id)");
    }

    if (dialogId == null) {
      throw new NullIdException("dialogId");
    } else {
      Dialog dialog = dialogRepository.findById(dialogId)
          .orElseThrow(() -> new EntityNotFoundException("dialog", dialogId));
      message.setDialog(dialog);

      final Long finalCreatorId1 = creatorId;
      User creator = userRepository.findById(creatorId)
          .orElseThrow(() -> new UserNotFoundException(finalCreatorId1));
      // Временно убираем отправителя из участников
      List<User> participants = new ArrayList<>(dialog.getParticipants());
      participants.remove(creator);
      // Опасный код, но ошибки произойти не должно
      receiverId = participants.get(0).getId();
    }

    Long finalCreatorId = creatorId;
    User creator = userRepository.findById(creatorId)
        .orElseThrow(() -> new UserNotFoundException(finalCreatorId));
    message.setCreator(creator);

    if (receiverId.equals(creatorId)) {
      throw new IllegalArgumentException("creatorId equals to receiverId!");
    }

    final Long finalReceiverId = receiverId;
    User receiver = userRepository.findById(receiverId)
        .orElseThrow(() -> new UserNotFoundException(finalReceiverId));
    message.setReceiver(receiver);

//    Dialog dialog = dialogRepository.findAll().stream().filter(
//        diag -> diag.getParticipants().containsAll(List.of(sender, receiver)))
//        .findFirst().orElseThrow(() -> new IllegalArgumentException(
//            "There is no dialog between sender(id = " + sender.getId()
//                + ") and receiver(id = " + receiverId + ")"));
  }
}


