package ru.hse.BookExchange.controllers;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.CreatedDatedRequestController;
import ru.hse.BookExchange.exceptions.BookExchangeRequestNotFoundException;
import ru.hse.BookExchange.exceptions.BookNotFoundException;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.exceptions.IllegalRequestStatusState;
import ru.hse.BookExchange.exceptions.UserNotFoundException;
import ru.hse.BookExchange.models.Book;
import ru.hse.BookExchange.models.Book.PublicityStatus;
import ru.hse.BookExchange.models.BookExchangeRequest;
import ru.hse.BookExchange.models.Dialog;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.models.abstractions.Request.RequestStatus;
import ru.hse.BookExchange.repositories.BookExchangeRequestRepository;
import ru.hse.BookExchange.repositories.BookRepository;
import ru.hse.BookExchange.repositories.DialogRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Контроллер запросов на передачу книги
 */
@RestController
@RequestMapping("book/request")
public class BookExchangeRequestController extends
    CreatedDatedRequestController<BookExchangeRequest> {

  // Репозиторий пользователей
  private final UserRepository userRepository;
  // Репозиторий книг для обмена
  private final BookRepository bookRepository;
  // Репозиторий диалогов
  private final DialogRepository dialogRepository;

  BookExchangeRequestController(BookExchangeRequestRepository repository,
      UserRepository userRepository, BookRepository bookRepository,
      DialogRepository dialogRepository) {
    super(repository);
    this.userRepository = userRepository;
    this.bookRepository = bookRepository;
    this.dialogRepository = dialogRepository;
  }

  /**
   * Добавляет запрос на передачу книги в бд
   *
   * @param request        - запрос на передачу книги
   * @param authentication - данные авторизации
   * @return сохраненный запрос на передачу книги
   */
  @Override
  @PostMapping()
  public BookExchangeRequest add(@RequestBody BookExchangeRequest request,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    request.setCreator(user);

    Long userFromId = request.getUserFromId();
    Long userToId = request.getUserToId();
    Long exchangingBookId = request.getExchangingBookId();

    User userTo;
    if (userToId == null) {
      userToId = user.getId();
      userTo = user;
    } else {
      final Long finalUserToId = userToId;
      userTo = userRepository.findById(userToId).orElseThrow(()
          -> new UserNotFoundException(finalUserToId));
    }

    if (userFromId.equals(userToId)) {
      throw new IllegalArgumentException(
          "userFrom and userTo cannot be equal. "
              + "(maybe you are trying to exchange book with yourself)");
    }

    User userFrom = userRepository.findById(userFromId).orElseThrow(()
        -> new UserNotFoundException(userFromId));

    if (userTo != user && !user.getRole().hasModeratorPermits()) {
      throw new ForbiddenException("You must have moderator permits to "
          + "create request directed not to you!");
    }

    if (!userFrom.getExchangeListIds().contains(exchangingBookId)) {
      throw new IllegalArgumentException(
          userFrom.getName() + " does not have that book (id = "
              + exchangingBookId
              + ") in exchange list.");
    }

    Book exchangingBook = bookRepository.findById(exchangingBookId)
        .orElseThrow(() -> new BookNotFoundException(exchangingBookId));

    request.setUserFrom(userFrom);
    request.setUserTo(userTo);
    request.setExchangingBook(exchangingBook);

    repository.save(request);

    Dialog dialog = new Dialog(List.of(userFrom, userTo));
    dialog.setExchangeRequest(request);
    dialogRepository.save(dialog);
    request.setDialog(dialog);

    return repository.save(request);
  }

  /**
   * Одобряет запрос на передачу книги (book)
   *
   * @param id             - id запроса
   * @param authentication - данные авторизации
   * @return одобренный запрос
   */
  @Override
  @PatchMapping("/{id}/accept")
  protected BookExchangeRequest accept(@PathVariable Long id,
      Authentication authentication) {
    User user = auth.isUser(authentication);

    BookExchangeRequest request = repository.findById(id)
        .orElseThrow(() -> new BookExchangeRequestNotFoundException(id));

    if (user != request.getUserFrom() && !user.getRole()
        .hasModeratorPermits()) {
      throw new ForbiddenException("You must have moderator permits to "
          + "accept request directed not from to you!");
    }

    if (request.getStatus().equals(RequestStatus.Accepted)) {
      throw new IllegalRequestStatusState("Request is already accepted!");
    }

    // Передадим книгу другому пользователю
    Book exchangingBook = request.getExchangingBook();
    User userFrom = request.getUserFrom();
    User userTo = request.getUserTo();
    userFrom.removeBookFromExchangeList(exchangingBook);
    userTo.addBookToExchangeList(exchangingBook);
    exchangingBook.setOwner(userTo);

    // Сделаем книгу приватной
    exchangingBook.setPublicityStatus(PublicityStatus.Private);

    // Обновим этого запроса
    request.setStatus(RequestStatus.Accepted);

    bookRepository.save(exchangingBook);
    userRepository.save(userFrom);
    userRepository.save(userTo);
    var res = repository.save(request);

    // Отклоняем все прочие запросы на эту книгу
    for (var exchangeRequest : exchangingBook.getExchangeRequests()) {
      if (exchangeRequest.getStatus() == RequestStatus.Pending) {
        exchangeRequest.setStatus(RequestStatus.Rejected);
        repository.save(exchangeRequest);
      }
    }

    return res;
  }

  /**
   * Отклоняет запрос на передачу книги (book)
   *
   * @param id             - id запроса
   * @param authentication - данные авторизации
   * @return отклоненный запрос
   */
  @Override
  @PatchMapping("/{id}/reject")
  protected BookExchangeRequest reject(@PathVariable Long id,
      Authentication authentication) {
    User user = auth.isUser(authentication);

    BookExchangeRequest request = repository.findById(id)
        .orElseThrow(() -> new BookExchangeRequestNotFoundException(id));

    if (user != request.getUserFrom() && !user.getRole()
        .hasModeratorPermits()) {
      throw new ForbiddenException("You must have moderator permits to "
          + "reject request directed not from you!");
    }

    if (!request.getStatus().equals(RequestStatus.Pending)) {
      throw new IllegalRequestStatusState(
          "Request is already in " + request.getStatus()
              + " state! You can only reject pending requests.");
    }

    // Обновим статус запроса
    request.setStatus(RequestStatus.Rejected);

    return repository.save(request);
  }
}


