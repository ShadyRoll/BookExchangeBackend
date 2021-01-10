package ru.hse.BookExchange.controllers;

import java.util.ArrayList;
import java.util.Collections;
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
import ru.hse.BookExchange.exceptions.BookBaseNotFoundException;
import ru.hse.BookExchange.exceptions.GenreNotFoundException;
import ru.hse.BookExchange.exceptions.NullIdException;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.BookBasePhoto;
import ru.hse.BookExchange.models.Genre;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.BookBasePhotoRepository;
import ru.hse.BookExchange.repositories.BookBaseRepository;
import ru.hse.BookExchange.repositories.BookRepository;
import ru.hse.BookExchange.repositories.GenreRepository;
import ru.hse.BookExchange.services.BookBaseService;

/**
 * Контроллер для книг (bookBase)
 */
@RestController
@RequestMapping("bookBase")
public class BookBaseController extends DatedEntityController<BookBase> {

  // Репозиторий книг (bookBase)
  private final BookBaseRepository repository;
  // Репозиторий книг для обмена (book)
  private final BookRepository bookRepository;
  // Котроллер книг для обмена (book)
  private final BookController bookController;
  // Репозиторий жанров
  private final GenreRepository genreRepository;

  private final BookBasePhotoController bookBasePhotoController;

  private final BookBaseService bookBaseService;

  BookBasePhotoRepository bookBasePhotoRepository;

  BookBaseController(BookBaseRepository repository,
      BookRepository bookRepository, BookController bookController,
      GenreRepository genreRepository,
      BookBasePhotoController bookBasePhotoController,
      BookBaseService bookBaseService,
      BookBasePhotoRepository bookBasePhotoRepository) {
    super(repository);
    this.repository = repository;
    this.bookRepository = bookRepository;
    this.bookController = bookController;
    this.genreRepository = genreRepository;
    this.bookBasePhotoController = bookBasePhotoController;
    this.bookBaseService = bookBaseService;
    this.bookBasePhotoRepository = bookBasePhotoRepository;
  }

  @Override
  @GetMapping("neverFindThis")
  public List<BookBase> all(int skip, int limit, boolean latest,
      List<Long> ids, Authentication authentication) {
    throw new UnsupportedOperationException("How did you find this link?");
  }

  /**
   * Ищет книгу по автору
   *
   * @param skip      - сколько пропустить
   * @param limit     - сколько вернуть
   * @param searchStr - поисковая строка
   * @return список найденных книг, отсортированных по релевантности
   */
  @GetMapping("/search/byAuthor")
  protected List<BookBase> searchByAuthor(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "") String searchStr,
      Authentication authentication) {
    DatedEntityController.checkSkipAndLimit(skip, limit);
    if (searchStr.isBlank()) {
      throw new NullIdException("searchStr (request parameter)");
    }

    var res = repository.searchByAuthor(searchStr, limit, skip);
    User user = auth.isUser(authentication);
    // Прячем приватные книги
    res.forEach((b) -> bookBaseService.hidePrivateBooks(b, user));
    return res;
  }

  /**
   * Ищет книгу по заголовку
   *
   * @param skip      - сколько пропустить
   * @param limit     - сколько вернуть
   * @param searchStr - поисковая строка
   * @return список найденных книг, отсортированных по релевантности
   */
  @GetMapping("/search/byTitle")
  protected List<BookBase> searchByTitle(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "") String searchStr,
      Authentication authentication) {
    DatedEntityController.checkSkipAndLimit(skip, limit);
    if (searchStr.isBlank()) {
      throw new NullIdException("searchStr (request parameter)");
    }

    var res = repository.searchByTitle(searchStr, limit, skip);
    User user = auth.isUser(authentication);
    // Прячем приватные книги
    res.forEach((b) -> bookBaseService.hidePrivateBooks(b, user));
    return res;
  }

  /**
   * Ищет книгу по заголовку и автору
   *
   * @param skip      - сколько пропустить
   * @param limit     - сколько вернуть
   * @param searchStr - поисковая строка
   * @return список найденных книг, отсортированных по релевантности
   */
  @GetMapping("/search")
  protected List<BookBase> search(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "") String searchStr,
      Authentication authentication) {
    DatedEntityController.checkSkipAndLimit(skip, limit);
    if (searchStr.isBlank()) {
      throw new NullIdException("searchStr (request parameter)");
    }

    var res = repository.searchByText(searchStr, limit, skip);
    User user = auth.isUser(authentication);
    // Прячем приватные книги
    res.forEach((b) -> bookBaseService.hidePrivateBooks(b, user));
    return res;
  }


  /**
   * Возвращает список книг (bookBase)
   *
   * @param skip           сколько пропустить
   * @param limit          сколько вернуть
   * @param sortBy         тип сортировки
   * @param ascending      порядок сортировки
   * @param ids            id массива книг (null, если не нужно)
   * @param latest         отсортировать ли по дате
   * @param recommended    вернуть ли рекомендованные книги
   * @param authentication данные авторизации
   * @return список книг
   */
  @GetMapping()
  public List<BookBase> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "none") String sortBy,
      @RequestParam(required = false, defaultValue = "true") boolean ascending,
      @RequestParam(required = false) List<Long> ids,

      @Deprecated
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @Deprecated
      @RequestParam(required = false, defaultValue = "false") boolean recommended,
      Authentication authentication) {
    return bookBaseService
        .all(skip, limit, sortBy, ascending, ids, latest, recommended,
            authentication);
  }

  /**
   * Удаляет книгу (bookBase) из бд
   *
   * @param id             - id книги (bookBase)
   * @param authentication - данные аутентификации
   */
  @Override
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, Authentication authentication) {
    auth.isModerator(authentication);
    BookBase bookBase = repository.findById(id)
        .orElseThrow(() -> new BookBaseNotFoundException(id));
    for (Long bookId : bookBase.getBookIds()) {
      bookController.delete(bookId, authentication);
    }
    List<Genre> genresWithBase = genreRepository.findAll().stream()
        .filter(genre -> genre.getBookBases().contains(bookBase))
        .collect(Collectors.toList());
    for (Genre genre : genresWithBase) {
      genre.getBookBases().remove(bookBase);
      genreRepository.save(genre);
    }

    super.delete(id, authentication);
  }

  @PostMapping()
  public BookBase add(@RequestBody BookBase bookBase,
      Authentication authentication) {
    List<Genre> genres = new ArrayList<>();
    for (Long genreId : bookBase.getGenreIds()) {
      genres.add(genreRepository.findById(genreId)
          .orElseThrow(() -> new GenreNotFoundException(genreId)));
      bookBase.getGenres().add(genres.get(genres.size() - 1));
    }

    var res = super.add(bookBase, authentication);
    for (Genre genre : genres) {
      genre.getBookBases().add(bookBase);
      genreRepository.save(genre);
    }

    if (bookBase.getPhotoTrans() != null) {
      BookBasePhoto photo = bookBasePhotoController
          .add(new BookBasePhoto(bookBase.getPhotoTrans(),
              auth.isUser(authentication), res), authentication);
      bookBasePhotoRepository.save(photo);
    }

    return res;
  }

  @Override
  @GetMapping("/{id}")
  public BookBase getById(@PathVariable Long id,
      Authentication authentication) {
    BookBase booKBase = repository.findById(id)
        .orElseThrow(() -> new BookBaseNotFoundException(id));
    User user = auth.isUser(authentication);
    // Прячем приватные книги
    bookBaseService.hidePrivateBooks(booKBase, user);
    return booKBase;
  }

  /**
   * Ищет книги по набору жанорв
   *
   * @param genreIds - id жанров
   * @param skip     - сколько пропустить
   * @param limit    - сколько вернуть
   * @param latest   - остортиовать ли по дате
   * @return список книг со всеми переданными жанрами
   */
  @GetMapping("/byGenres")
  protected List<BookBase> bookBasesWithGenres(
      @RequestParam(name = "genres") List<Long> genreIds,
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      Authentication authentication) {
    DatedEntityController.checkSkipAndLimit(skip, limit);
    var stream = repository.findByGenre(genreIds).stream();

    if (latest) {
      stream = stream.sorted(Collections.reverseOrder());
    }
    var res = stream.skip(skip).limit(limit).collect(
        Collectors.toList());
    User user = auth.isUser(authentication);
    // Прячем приватные книги
    res.forEach((b) -> bookBaseService.hidePrivateBooks(b, user));
    return res;
  }

  /**
   * Заменяет книгу
   *
   * @param newBookBase    - новая книга
   * @param id             - id книги
   * @param authentication - данные аутентификации
   * @return сохраненная книга (bookBase)
   */
  @Override
  @PutMapping("/{id}")
  public BookBase replace(@RequestBody BookBase newBookBase,
      @PathVariable Long id,
      Authentication authentication) {
    BookBase bookBase = getById(id, authentication);
    List<Long> genreIds = newBookBase.getGenreIds();
    if (genreIds != null) {
      for (Genre genre : bookBase.getGenres()) {
        genre.getBookBases().remove(bookBase);
      }
    }

    newBookBase = super.replace(newBookBase, id, authentication);

    if (genreIds != null) {
      newBookBase.getGenres().clear();
      for (Long genreId : genreIds) {
        Genre genre = genreRepository.findById(genreId)
            .orElseThrow(() -> new GenreNotFoundException(genreId));
        genre.getBookBases().add(newBookBase);
        newBookBase.getGenres().add(genre);
        genreRepository.save(genre);
      }
    }

    return repository.save(newBookBase);
  }


  /**
   * Типы сортировки
   */
  public enum SortType {
    none, date, rate, recommended
  }
}


