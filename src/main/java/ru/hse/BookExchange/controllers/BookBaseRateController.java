package ru.hse.BookExchange.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.DatedEntityController;
import ru.hse.BookExchange.exceptions.BookBaseNotFoundException;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.exceptions.NullIdException;
import ru.hse.BookExchange.exceptions.UserNotFoundException;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.BookBaseRate;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.BookBaseRateRepository;
import ru.hse.BookExchange.repositories.BookBaseRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Контроллер для оценок книг
 */
@RestController
@RequestMapping("bookBase/rate")
public class BookBaseRateController extends
    DatedEntityController<BookBaseRate> {

  // Репозиторий книг
  private final BookBaseRepository bookBaseRepository;
  // Репозиторий пользователей
  private final UserRepository userRepository;

  BookBaseRateController(BookBaseRateRepository repository,
      UserRepository userRepository,
      BookBaseRepository bookBaseRepository) {
    super(repository);
    this.bookBaseRepository = bookBaseRepository;
    this.userRepository = userRepository;
  }

  /**
   * Добавляет оценку книги (bookBase) в бд
   *
   * @param rate           - оценка книги
   * @param authentication - данные авторизации
   * @return сохраненная оценка книги
   */
  @Override
  @PostMapping
  public BookBaseRate add(@RequestBody BookBaseRate rate,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    Long ratedBookBaseId = rate.getRatedBookBaseId();
    if (ratedBookBaseId == null) {
      throw new NullIdException("ratedBookBaseId");
    }
    BookBase ratedBookBase = bookBaseRepository
        .findById(ratedBookBaseId).orElseThrow(
            () -> new BookBaseNotFoundException(ratedBookBaseId));
    rate.setRatedBookBase(ratedBookBase);

    Long creatorId = rate.getCreatorId();
    if (creatorId == null) {
      rate.setCreator(user);
    } else if (!creatorId.equals(user.getId()) && !user.getRole()
        .hasAdminPermits()) {
      throw new ForbiddenException(
          "You can't post rates from other users, while you are not admin!");
    } else {
      rate.setCreator(userRepository.findById(creatorId)
          .orElseThrow(() -> new UserNotFoundException(creatorId)));
    }

    ratedBookBase.addRate(rate);

    bookBaseRepository.save(ratedBookBase);
    repository.save(rate);

    // Подменяем значение
    rate.setRate(rate.getRatedBookBase().getRating());
    return rate;
  }

}


