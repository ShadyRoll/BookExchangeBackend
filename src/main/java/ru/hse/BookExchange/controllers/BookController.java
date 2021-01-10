package ru.hse.BookExchange.controllers;

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
import ru.hse.BookExchange.controllers.abstractions.CreatedDatedEntityController;
import ru.hse.BookExchange.exceptions.BookBaseNotFoundException;
import ru.hse.BookExchange.exceptions.ExteriorQualityNotFoundException;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.exceptions.NullIdException;
import ru.hse.BookExchange.exceptions.TownNotFoundException;
import ru.hse.BookExchange.exceptions.UserNotFoundException;
import ru.hse.BookExchange.models.Book;
import ru.hse.BookExchange.models.Book.PublicityStatus;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.ExteriorQuality;
import ru.hse.BookExchange.models.Town;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.BookBaseRepository;
import ru.hse.BookExchange.repositories.BookExchangeRequestRepository;
import ru.hse.BookExchange.repositories.BookRepository;
import ru.hse.BookExchange.repositories.ExteriorQualityRepository;
import ru.hse.BookExchange.repositories.TownRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Контроллер книг для обмена (book)
 */
@RestController
@RequestMapping("book")
public class BookController extends CreatedDatedEntityController<Book> {

  // Репозиторий книг (bookBase)
  BookBaseRepository bookBaseRepository;
  // Репозиторий пользователя
  UserRepository userRepository;
  // Репозиторий уровня изношенности книги
  ExteriorQualityRepository exteriorQualityRepository;
  // Репозиторий городов
  TownRepository townRepository;
  // Репозиторий запросов на передачу книги
  BookExchangeRequestRepository bookExchangeRequestRepository;

  BookController(BookRepository repository,
      BookBaseRepository bookBaseRepository, UserRepository userRepository,
      ExteriorQualityRepository exteriorQualityRepository,
      BookExchangeRequestRepository bookExchangeRequestRepository,
      TownRepository townRepository) {
    super(repository);
    this.bookBaseRepository = bookBaseRepository;
    this.userRepository = userRepository;
    this.exteriorQualityRepository = exteriorQualityRepository;
    this.townRepository = townRepository;
    this.bookExchangeRequestRepository = bookExchangeRequestRepository;
  }

  /**
   * Возвращает список книг для обмена (book)
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id массива книг (null, если не нужно)
   * @param authentication - данные авторизации
   * @return список книг для обмена (book)
   */
  @Override
  @GetMapping()
  public List<Book> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    User user = auth.isUser(authentication);
    List<Book> res = super.all(skip, limit, latest, ids, authentication);

    if (user.getRole().hasModeratorPermits()) {
      return res;
    }
    
    if (ids == null) {
      // Видеть можно только книги открытые для обмена книги (а также свои закрытые)
      res = res.stream()
          .filter(book -> book.getPublicityStatus() == PublicityStatus.Public ||
              book.getOwnerId().equals(user.getId()))
          .collect(Collectors.toList());
    }

    return res;
  }

  /**
   * Получает пользователя по id
   *
   * @param id             id пользователя
   * @param authentication данные аутентификации
   * @return пользователя
   */
  @Override
  @GetMapping("/{id}")
  public Book getById(@PathVariable Long id, Authentication authentication) {
    return super.getById(id, authentication);
//    User user = auth.isUser(authentication);
//    Book book = super.getById(id, authentication);
//    if (book.getPublicityStatus() == PublicityStatus.Public
//        || book.getOwner() == user || user.getRole().hasModeratorPermits()) {
//      return book;
//    }
//    throw new ForbiddenException(
//        "This book is private (you must own this book or be a moderator).");
  }


  /**
   * Добавляет книгу для обмена в бд
   *
   * @param book           - книга
   * @param authentication - данные авторизации
   * @return сохраненная книга
   */
  @Override
  @PostMapping
  public Book add(@RequestBody Book book, Authentication authentication) {
    User user = auth.isUser(authentication);

    if (book.getTownId() == null) {
      book.setTownId(user.getTownId());
    }

    Long ownerId = book.getOwnerId();
    // Если ownerId не указан, значит пользователь, вероятно, хочет добавить книгу себе
    if (ownerId == null) {
      book.setOwnerId(user.getId());
    } else if (!user.getRole().hasAdminPermits() && !user.getId()
        .equals(ownerId)) {
      /* Иначе убедимся, что пользователь добавляет книгу именно себе,
         или что пользователь = админ */
      throw new ForbiddenException(
          "You can't add books to other users, while you are not admin. " +
              "(ownerId must much your id, or you can leave it empty, it " + ""
              + "will be automatically set to your id)");
    }

    setBookRelations(book);
    book.setCreator(user);

    return repository.save(book);
  }

  /**
   * Заменяет книгу (book) в бд
   *
   * @param newBook        - новая книга (book)
   * @param id             - id обложки книги
   * @param authentication - данные аутентификации
   * @return сохраненная новая книга (book)
   */
  @Override
  @PutMapping("/{id}")
  public Book replace(@RequestBody Book newBook,
      @PathVariable Long id, Authentication authentication) {
    hasAccessToModify(id, authentication);
    //Book oldBook = getById(id, authentication);

    //newBook.setCreator(oldBook.getCreator());
    //newBook.setPhoto(oldBook.getPhoto());
    //newBook.setExchangeRequests(oldBook.getExchangeRequests());
    //newBook.setId(id);

    setBookRelations(newBook);
    return super.replace(newBook, id, authentication);
    //return repository.save(newBook);
  }

  /**
   * Удаляет книгу (book) для обмена из бд
   *
   * @param id             - id книги (book) для обмена
   * @param authentication - данные аутентификации
   */
  @Override
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, Authentication authentication) {
    hasAccessToModify(id, authentication);
    Book book = getById(id, authentication);

    bookExchangeRequestRepository
        .deleteAll(bookExchangeRequestRepository.findAll().stream()
            .filter(r -> r.getExchangingBook().equals(book))
            .collect(Collectors.toList()));
    super.delete(id, authentication);
  }

  /**
   * Устанавливает зависимости книги для обмена
   *
   * @param book книга
   */
  private void setBookRelations(Book book) {
    Long exteriorQualityId = book.getExteriorQualityId();
    Long baseId = book.getBaseId();
    Long ownerId = book.getOwnerId();
    Long townId = book.getTownId();

    if (book.getBase() != null) {
      baseId = book.getBase().getId();
    } else if (baseId == null) {
      throw new NullIdException("baseId");
    }

    if (book.getOwner() != null) {
      ownerId = book.getOwner().getId();
    } else if (ownerId == null) {
      throw new NullIdException("creatorId");
    }

    if (book.getExteriorQuality() != null) {
      exteriorQualityId = book.getExteriorQuality().getId();
    } else if (exteriorQualityId == null) {
      throw new NullIdException("exteriorQualityId");
    }

    if (book.getTown() != null) {
      townId = book.getTown().getId();
    } else if (townId == null) {
      throw new NullIdException("townId");
    }

    if (!bookBaseRepository.existsById(baseId)) {
      throw new BookBaseNotFoundException(baseId);
    }
    if (!userRepository.existsById(ownerId)) {
      throw new UserNotFoundException(ownerId);
    }
    if (!exteriorQualityRepository.existsById(exteriorQualityId)) {
      throw new ExteriorQualityNotFoundException(exteriorQualityId);
    }
    if (!townRepository.existsById(townId)) {
      throw new TownNotFoundException(townId);
    }

    BookBase base = bookBaseRepository.getOne(baseId);
    book.setBase(base);
    User owner = userRepository.getOne(ownerId);
    book.setOwner(owner);
    Town town = townRepository.getOne(townId);
    book.setTown(town);
    ExteriorQuality exteriorQuality = exteriorQualityRepository
        .getOne(exteriorQualityId);
    book.setExteriorQuality(exteriorQuality);
  }

  @Override
  protected User hasAccessToModify(Long id, Authentication authentication)
      throws ForbiddenException {
    User user = auth.isUser(authentication);
    Book book = getById(id, authentication);
    if (!(book.getOwner() == user) && !user.getRole()
        .hasAdminPermits()) {
      throw new ForbiddenException(
          "You can modify only your book (or you must have admin permits)");
    }
    return user;
  }
}


