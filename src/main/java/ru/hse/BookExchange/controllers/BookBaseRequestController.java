package ru.hse.BookExchange.controllers;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.CreatedDatedRequestController;
import ru.hse.BookExchange.exceptions.BookBaseRequestNotFoundException;
import ru.hse.BookExchange.exceptions.EntityNotFoundException;
import ru.hse.BookExchange.exceptions.GenreNotFoundException;
import ru.hse.BookExchange.exceptions.IllegalRequestStatusState;
import ru.hse.BookExchange.models.Book;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.BookBaseRequest;
import ru.hse.BookExchange.models.Genre;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.models.abstractions.Request.RequestStatus;
import ru.hse.BookExchange.repositories.BookBaseRepository;
import ru.hse.BookExchange.repositories.BookBaseRequestRepository;
import ru.hse.BookExchange.repositories.BookRepository;
import ru.hse.BookExchange.repositories.GenreRepository;

/**
 * Контроллер для запросов на добавление книг (bookBase)
 */
@RestController
@RequestMapping("bookBase/request")
public class BookBaseRequestController extends
    CreatedDatedRequestController<BookBaseRequest> {

  // Репозиторий книг (bookBase)
  private final BookBaseRepository bookBaseRepository;
  // Репозиторий жанров
  private final GenreRepository genreRepository;
  // Контроллер книг (bookBase)
  private final BookBaseController bookBaseController;

  private final GenreController genreController;
  // Репозиторий книг для передачи
  private final BookRepository bookRepository;

  BookBaseRequestController(BookBaseRequestRepository repository,
      BookBaseRepository bookBaseRepository, GenreRepository genreRepository,
      BookBaseController bookBaseController,
      BookRepository bookRepository,
      GenreController genreController) {
    super(repository);
    this.bookBaseRepository = bookBaseRepository;
    this.genreRepository = genreRepository;
    this.bookBaseController = bookBaseController;
    this.bookRepository = bookRepository;
    this.genreController = genreController;
  }

  /**
   * Добавляет запрос на добавление книги (bookBase) в бд
   *
   * @param entity         - запрос на добавление книги
   * @param authentication - данные авторизации
   * @return сохраненный запрос на добавление книги
   */
  @Override
  @PostMapping
  public BookBaseRequest add(@RequestBody BookBaseRequest entity,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    entity.setCreator(user);

    List<Long> genreIds = entity.getGenreIds();
    entity.setGenres(genreRepository.findAllById(genreIds));
    if (entity.getGenres().size() != entity.getGenreIds().size()) {
      for (Long genreId : entity.getGenreIds()) {
        if (!genreRepository.existsById(genreId)) {
          throw new GenreNotFoundException(genreId);
        }
      }
    }

    return repository.save(entity);
  }

  @Override
  @PutMapping("/{id}")
  public BookBaseRequest replace(@RequestBody BookBaseRequest newRequest,
      @PathVariable Long id,
      Authentication authentication) {
    BookBaseRequest oldRequest = getById(id, authentication);
    oldRequest.setSuperId(oldRequest.getId());
    if (newRequest.getStatus() == RequestStatus.Pending) {
      newRequest.setStatus(oldRequest.getStatus());
    }
    if (newRequest.getGenreIds().size()>0) {
      for (Genre genre:oldRequest.getGenres()) {
        genre.getBookBases().remove(oldRequest);
        genreRepository.save(genre);
      }
      oldRequest.getGenres().clear();

      for (Long genreId : newRequest.getGenreIds()) {
        Genre genre = genreController.getById(genreId, authentication);
        newRequest.getGenres().add(genre);
        oldRequest.getGenres().add(genre);
        genreRepository.save(genre);
      }
      oldRequest.setGenreIds(newRequest.getGenreIds());
      repository.save(oldRequest);
    }

    var res= super.replace(newRequest, id, authentication);
    return res;
  }

  /**
   * Одобряет запрос на добавление книги (bookBase)
   *
   * @param id             - id запроса
   * @param authentication - данные авторизации
   * @return одобренный запрос
   */
  @Override
  @PatchMapping("/{id}/accept")
  public BookBaseRequest accept(@PathVariable Long id,
      Authentication authentication) {
    auth.isModerator(authentication);
    BookBaseRequest request = repository.findById(id)
        .orElseThrow(() -> new BookBaseRequestNotFoundException(id));

    if (request.getStatus().equals(RequestStatus.Accepted)) {
      throw new IllegalRequestStatusState("Request is already accepted!");
    }

    // Добавим утвержденную книгу в бд
    BookBase bookBase = new BookBase(request);
    bookBaseRepository.save(bookBase);

    for (Book book : request.getBooks()) {
      book.setBase(bookBase);
      bookRepository.save(book);
    }

    List<Genre> genres = new ArrayList<>();
    for (Long genreId : request.getGenreIds()) {
      Genre genre = genreRepository.findById(genreId)
          .orElseThrow(() -> new GenreNotFoundException(genreId));
      genres.add(genre);
      genre.addBookBase(bookBase);
    }

    for (Genre genre : bookBase.getGenres()) {
      genre.getBookBases().remove(bookBase);
    }

    bookBase.setGenres(genres);
    bookBaseRepository.save(bookBase);

    // Обновим статус запроса
    request.setStatus(RequestStatus.Accepted);
    return repository.save(request);
  }

  /**
   * Отклоняет запрос на добавление книги (bookBase)
   *
   * @param id             - id запроса
   * @param authentication - данные авторизации
   * @return отклоненный запрос
   */
  @Override
  @PatchMapping("/{id}/reject")
  public BookBaseRequest reject(@PathVariable Long id,
      Authentication authentication) {
    auth.isModerator(authentication);
    BookBaseRequest request = repository.findById(id)
        .orElseThrow(() -> new BookBaseRequestNotFoundException(id));

    if (!request.getStatus().equals(RequestStatus.Pending)) {
      throw new IllegalRequestStatusState(
          "Request is already in " + request.getStatus()
              + " state! You can only reject pending requests.");
    }

    // Обновим статус запроса
    request.setStatus(RequestStatus.Rejected);
    return repository.save(request);
  }

  /**
   * Удаляет запрос на добавление книги (bookBase) из бд
   *
   * @param id             - id запроса на добавление книги (bookBase)
   * @param authentication - данные аутентификации
   */
  @Override
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, Authentication authentication) {
    BookBaseRequest request = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("bookBaseRequest", id));
    request.setSuperId(request.getId());

    // Используем удаление bookBase, оно также разбрерется со всеми зависимотсями
    bookBaseController.delete(id, authentication);

    //super.delete(id, authentication);
  }
}


