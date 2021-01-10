package ru.hse.BookExchange.controllers;

import java.util.List;
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
import ru.hse.BookExchange.exceptions.BookBaseNotFoundException;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.exceptions.NullIdException;
import ru.hse.BookExchange.exceptions.UserNotFoundException;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.Town;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.models.User.Role;
import ru.hse.BookExchange.repositories.BookBaseRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Контроллер пользователей
 */
@RestController
@RequestMapping("user")
public class UserController extends DatedEntityController<User> {

  // Репозиторий книг (bookBase)
  BookBaseRepository bookBaseRepository;

  TownController townController;

  UserController(UserRepository repository,
      BookBaseRepository bookBaseRepository,
      TownController townController) {
    super(repository);
    this.bookBaseRepository = bookBaseRepository;
    this.townController = townController;
  }

  /**
   * Возвращает пользователя по данным аутентификации
   *
   * @param authentication данные аутентификации
   * @return пользователь
   */
  @GetMapping("/me")
  protected User getCurrentUserByToken(Authentication authentication) {
    return auth.isUser(authentication);
  }


  /**
   * Возвращает список пользователей
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id массива пользователей (null, если не нужно)
   * @param authentication - данные авторизации
   * @return список пользователей
   */
  @Override
  @GetMapping()
  public List<User> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    User requestingUser = auth.isUser(authentication);
    List<User> res = super.all(skip, limit, latest, ids, authentication);
    if (requestingUser.getRole().hasModeratorPermits()) {
      return res;
    }
    // Удаляем все приватные книги
    res.forEach(User::switchExchangeListToPublic);
    return res;
  }

  /**
   * Возвращает пользователя по id
   *
   * @param id             id пользователя
   * @param authentication данные аутентификации
   * @return пользователя
   */
  @Override
  @GetMapping("/{id}")
  public User getById(@PathVariable Long id, Authentication authentication) {
    User requestingUser = auth.isUser(authentication);
    User res = super.getById(id, authentication);
    /* Если запрос делает не модератор и не владелец аккаунта,
       убираем приватные книги */
    if (!requestingUser.getRole().hasModeratorPermits()
        && requestingUser != res) {
      res.switchExchangeListToPublic();
    }
    return res;
    //надо протестить приватность книг, а так все вроде готово
  }

  /**
   * Добавляет книгу в избранные
   *
   * @param baseId         id книги (bookBase)
   * @param authentication данные аутентификации
   */
  @PostMapping("/wishlist")
  protected void addBookBaseToWishList(@RequestParam Long baseId,
      Authentication authentication) {
    checkBookBaseExist(baseId);
    User user = auth.isUser(authentication);

    BookBase bookBase = bookBaseRepository.getOne(baseId);
    user.addToWishList(bookBase);
    bookBase.addWisher(user);

    bookBaseRepository.save(bookBase);
    repository.save(user);
  }

  /**
   * Удаляет книгу из избранных
   *
   * @param baseId         id пользователя
   * @param authentication данные аутентификации
   */
  @DeleteMapping("/wishlist")
  protected void removeBookBaseFromWishList(@RequestParam Long baseId,
      Authentication authentication) {
    checkBookBaseExist(baseId);
    User user = auth.isUser(authentication);

    BookBase bookBase = bookBaseRepository.getOne(baseId);
    user.removeFromWishList(bookBase);
    bookBase.removeWisher(user);

    bookBaseRepository.save(bookBase);
    repository.save(user);
  }

  /**
   * Блокирует пользователя
   *
   * @param id             id пользователя
   * @param authentication данные аутентификации
   */
  @PostMapping("/{id}/block")
  protected void blockUser(@PathVariable Long id,
      Authentication authentication) {
    User moderator = auth.isModerator(authentication);
    User user = repository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    user.setBlocked(true);

    repository.save(user);
  }

  /**
   * Заменяет пользователя в бд
   *
   * @param newUser        - новый пользователь
   * @param id             - id пользователя
   * @param authentication - данные аутентификации
   * @return сохраненный новый пользователь
   */
  @Override
  @PutMapping("/{id}")
  public User replace(@RequestBody User newUser, @PathVariable Long id,
      Authentication authentication) {
    User user = repository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    if (newUser.getPassword() != null) {
      newUser
          .setPassword(auth.getPasswordEncoder().encode(newUser.getPassword()));
    } else {
      newUser.setPassword(user.getPassword());
    }

    if (newUser.getTownId() != null) {
      Town town = townController.getById(newUser.getTownId(), authentication);
      newUser.setTown(town);
    }

    newUser.setWishList(user.getWishList());
    newUser.setExchangeList(user.getExchangeList());
    newUser.setAvatar(user.getAvatar());
    newUser.setBlocked(user.isBlocked());
    newUser.setComplaintsList(user.getComplaints());
    if (newUser.getUsername() == null) {
      newUser.setUsername(user.getUsername());
    }

    if (newUser.getRole() != Role.None && newUser.getRole() != user.getRole()) {
      if (newUser.getUsername() == null) {
        newUser.setUsername(user.getUsername());
      } else if (!user.getUsername().equals(newUser.getUsername()) &&
          ((UserRepository) repository).findByUsername(newUser.getUsername())
              != null) {
        throw new IllegalArgumentException("This username is already taken.");
      }
    }

    // Не админ может поменять только себя
    if (auth.isUser(authentication) == user) {
      if (newUser.getRole() != Role.None && user.getRole() != newUser
          .getRole()) {
        throw new ForbiddenException("You cannot change your role!");
      }
      // Устанавливаем старую роль
      newUser.setRole(user.getRole());
      return super.replace(newUser, id, authentication);
    }

    // ДАЛЕЕ ЛОГИКА ТОЛЬКО ПРИ АДМИНСКОМ ТОКИНЕ

    // Админ может поменять другого пользователя, в том числе его роль
    auth.isAdmin(authentication);
    // Но не другого админа
    if (user.getRole() == Role.Admin) {
      throw new ForbiddenException(
          "This user is admin, you can't modify his or her account.");
    }
    if (newUser.getRole() == Role.None) {
      newUser.setRole(user.getRole());
    }

    return super.replace(newUser, id, authentication);
  }

  /**
   * Разблокирует пользователя
   *
   * @param id             id пользователя
   * @param authentication данные аутентификации
   */
  @PostMapping("/{id}/unblock")
  protected void unblockUser(@PathVariable Long id,
      Authentication authentication) {
    User moderator = auth.isModerator(authentication);
    User user = repository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    user.setBlocked(false);

    repository.save(user);
  }


  /**
   * Проверяет существование книги (bookBase)
   *
   * @param baseId id книги
   * @throws NullIdException           если id было null
   * @throws BookBaseNotFoundException если книги не существует
   */
  private void checkBookBaseExist(Long baseId)
      throws NullIdException, BookBaseNotFoundException {
    if (baseId == null) {
      throw new NullIdException("baseId");
    }
    if (!bookBaseRepository.existsById(baseId)) {
      throw new BookBaseNotFoundException(baseId);
    }
  }
}


