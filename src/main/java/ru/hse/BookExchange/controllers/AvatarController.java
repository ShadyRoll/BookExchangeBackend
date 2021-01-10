package ru.hse.BookExchange.controllers;

import java.util.List;
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
import ru.hse.BookExchange.exceptions.UserNotFoundException;
import ru.hse.BookExchange.models.Avatar;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.AvatarRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Контроллер для аватаров
 */
@RestController
@RequestMapping("avatar")
public class AvatarController extends CreatedDatedEntityController<Avatar> {

  private final UserRepository userRepository;

  AvatarController(AvatarRepository repository, UserRepository userRepository) {
    super(repository);
    this.userRepository = userRepository;
  }

  /**
   * Возвращает список аватаров
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id массива аватаров (null, если не нужно)
   * @param authentication - жанные авторизации
   * @return список аватаров
   */
  @Override
  @GetMapping
  public List<Avatar> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    throw new UnsupportedOperationException(
        "Request rejected to avoid huge data transmission (get picture by id instead).");
  }

  /**
   * Добавляет аватар в бд
   *
   * @param avatar         - аватар
   * @param authentication - данные авторизации
   * @return сохраненный аватар
   */
  @Override
  @PostMapping
  public Avatar add(@RequestBody Avatar avatar,
      Authentication authentication) {
    // ! Загрузить аватар можно только себе! (avatarOwnerId указавать нельзя)

    /*  DEBUG ONLY
    try {
      ImageIO.write(ImageIO.read(new ByteArrayInputStream(entity.getImage())),
          "jpg",
          new File("C:/Users/Shado/Desktop/TermWork/files/avatar.jpg"));
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    User user;
    if (avatar.getOwnerId() != null) {
      user = userRepository.findById(avatar.getOwnerId())
          .orElseThrow(() -> new UserNotFoundException(avatar.getOwnerId()));
    } else {
      user = auth.isUser(authentication);
    }

    // Удаляем старую автарку пользователя, если она была
    if (user.getAvatar() != null) {
      repository.delete(user.getAvatar());
    }

    // Сохраняем новую аватарку
    avatar.setCreator(auth.isUser(authentication));
    avatar.setOwner(user);
    return repository.save(avatar);
  }

  /**
   * Заменяет аватар пользователя в бд
   *
   * @param entity         - новый аватар
   * @param id             - id автара
   * @param authentication - данные аутентификации
   * @return сохраненный новый аватар
   */
  @Override
  @PutMapping("/{id}")
  public Avatar replace(@RequestBody Avatar entity, @PathVariable Long id,
      Authentication authentication) {
    // ! Изменить владельца аватара нельзя! (avatarOwnerId указавать нельзя)

    /*  DEBUG ONLY
    try {
      ImageIO.write(ImageIO.read(new ByteArrayInputStream(entity.getImage())),
          "jpg",
          new File("C:/Users/Shado/Desktop/TermWork/files/avatar.jpg"));
    } catch (IOException e) {
      e.printStackTrace();
    }*/

    User owner = hasAccessToModify(id, authentication);
    entity.setOwner(owner);

    return super.replace(entity, id, authentication);
    // Проверка прав на редактирование выполнится в super.replace(...)
  }


}


