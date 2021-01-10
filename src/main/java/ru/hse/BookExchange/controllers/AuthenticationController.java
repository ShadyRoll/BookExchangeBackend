package ru.hse.BookExchange.controllers;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static ru.hse.BookExchange.security.SecurityConstants.EXPIRATION_TIME;
import static ru.hse.BookExchange.security.SecurityConstants.SECRET;

import com.auth0.jwt.JWT;
import java.util.Date;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.exceptions.TownNotFoundException;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.models.User.Role;
import ru.hse.BookExchange.repositories.TownRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Контроллер для авторизации и регистрации пользователей
 */
@RestController
@RequestMapping
public class AuthenticationController {

  // Репозиторий пользователей
  private final UserRepository repository;
  // Репозиторий городов
  private final TownRepository townRepository;
  // Кодировщик паролей
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  AuthenticationController(UserRepository repository,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      TownRepository townRepository) {
    this.repository = repository;
    this.townRepository = townRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  /**
   * Регистрация пользователя
   *
   * @param user - пользователь
   * @return зарегистрированный пользователь
   */
  @PostMapping("/signup")
  ResponseEntity<User> signUp(@RequestBody User user) {
    if (repository.findByUsername(user.getUsername()) != null) {
      throw new IllegalArgumentException("This username is already taken.");
    }

    if (user.getRole() != Role.None && user.getRole() != Role.User) {
      throw new ForbiddenException("You cannot set yourself a role.");
    }

    if (user.getTownId() != null) {
      user.setTown(townRepository.findById(user.getTownId())
          .orElseThrow(() -> new TownNotFoundException(
              user.getTownId())));
    }

    // Устанавливаем роль = юзер
    user.setRole(Role.User);

    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    User savedUser = repository.save(user);

    String token = JWT.create()
        .withSubject(savedUser.getUsername())
        .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .sign(HMAC512(SECRET.getBytes()));

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    return new ResponseEntity<>(savedUser, headers, HttpStatus.OK);
  }

  /*
    GetMapping: /login
    Login implementation is already made by spring security
    Example:
    * Get localhost/login
    * Json body: {"username":"kek", "password":"1234"}
   */

  /**
   * Возвращает кодировщик паролей
   *
   * @return кодировщик паролей
   */
  public BCryptPasswordEncoder getPasswordEncoder() {
    return bCryptPasswordEncoder;
  }

  /**
   * Проверяет, является ли пользователь модератором
   *
   * @param authentication - иноформация авторизации
   * @return пользователя
   * @throws ForbiddenException - если не является
   */
  public User isModerator(Authentication authentication)
      throws ForbiddenException {
    User user = repository.findByUsername(authentication.getName());
    if (user == null || !user.getRole().hasModeratorPermits()) {
      throw new ForbiddenException(
          "You must have moderator permits to make this request");
    }
    return user;
  }


  /**
   * Проверяет, является ли пользователь админом
   *
   * @param authentication - иноформация авторизации
   * @return пользователя
   * @throws ForbiddenException - если не является
   */
  public User isAdmin(Authentication authentication) throws ForbiddenException {
    User user = repository.findByUsername(authentication.getName());
    if (user == null || !user.getRole().hasAdminPermits()) {
      throw new ForbiddenException(
          "You must have admin permits to make this request");
    }
    return user;
  }


  /**
   * Проверяет, имеет ли пользователь права пользователя
   *
   * @param authentication - иноформация авторизации
   * @return пользователя
   * @throws ForbiddenException - если не является
   */
  public User isUser(Authentication authentication) throws ForbiddenException {
    User user = repository.findByUsername(authentication.getName());
    if (user == null || !user.getRole().hasUserPermits()) {
      throw new ForbiddenException(
          "You must have user permits to make this request");
    }
    return user;
  }
}


