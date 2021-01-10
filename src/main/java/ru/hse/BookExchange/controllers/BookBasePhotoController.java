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
import ru.hse.BookExchange.exceptions.BookBaseNotFoundException;
import ru.hse.BookExchange.exceptions.EntityNotFoundException;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.exceptions.NullIdException;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.BookBasePhoto;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.BookBasePhotoRepository;
import ru.hse.BookExchange.repositories.BookBaseRepository;

/**
 * Контроллер для обложек книг (bookBase)
 */
@RestController
@RequestMapping("bookBasePhoto")
public class BookBasePhotoController extends
    CreatedDatedEntityController<BookBasePhoto> {

  // Репозиторий книг
  private final BookBaseRepository bookBaseRepository;

  BookBasePhotoController(BookBasePhotoRepository repository,
      BookBaseRepository bookRepository) {
    super(repository);
    this.bookBaseRepository = bookRepository;
  }

  /**
   * Возвращает список обложек книг (bookBase)
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id массива книг (null, если не нужно)
   * @param authentication - данные авторизации
   * @return список обложек книг (bookBase)
   */
  @Override
  @GetMapping
  public List<BookBasePhoto> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    throw new UnsupportedOperationException(
        "Request rejected to avoid huge data transmission (get picture by id instead).");
  }

  /**
   * Добавляет обложку книги (bookBase) в бд
   *
   * @param entity         - обложка книги
   * @param authentication - данные авторизации
   * @return сохраненная обложка книги
   */
  @Override
  @PostMapping
  public BookBasePhoto add(@RequestBody BookBasePhoto entity,
      Authentication authentication) {

    /*  DEBUG ONLY
    try {
      ImageIO.write(ImageIO.read(new ByteArrayInputStream(entity.getImage())),
          "jpg",
          new File("C:/Users/Shado/Desktop/TermWork/files/bookBasePhoto.jpg"));
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    if (authentication == null) {
      throw new ForbiddenException("Sign in to post book base photo!");
    }
    User creator = auth.isUser(authentication);
    entity.setCreator(creator);

    Long bookBaseId = entity.getBookBaseId();
    if (bookBaseId == null) {
      throw new NullIdException("bookBaseId");
    }

    BookBase bookBase = bookBaseRepository.findById(bookBaseId)
        .orElseThrow(() -> new BookBaseNotFoundException(bookBaseId));

    bookBase.setPhoto(entity);
    entity.setBookBase(bookBase);

    var res = repository.saveAndFlush(entity);
    bookBaseRepository.saveAndFlush(bookBase);
    return res;
  }

  /**
   * Заменяет обложку книги (bookBase) в бд
   *
   * @param entity         - новая обложка книги (bookBase)
   * @param id             - id обложки книги
   * @param authentication - данные аутентификации
   * @return сохраненная новая обложка книги (bookBase)
   */
  @Override
  @PutMapping("/{id}")
  public BookBasePhoto replace(@RequestBody BookBasePhoto entity,
      @PathVariable Long id,
      Authentication authentication) {

    /*  DEBUG ONLY
    try {
      ImageIO.write(ImageIO.read(new ByteArrayInputStream(entity.getImage())),
          "jpg",
          new File("C:/Users/Shado/Desktop/TermWork/files/bookBasePhoto.jpg"));
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    if (authentication == null) {
      throw new ForbiddenException("Sign in to edit book base photos!");
    }
    hasAccessToModify(id, authentication);
    BookBasePhoto oldPhoto = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("bookBasePhoto", id));
    BookBase bookBase = bookBaseRepository.findById(oldPhoto.getBookBaseId())
        .orElseThrow(() -> new IllegalArgumentException(String.format(
            "Somehow original bookBasePhoto(id = %s) has null book field. (error in db)",
            id)));

    if (entity.getBookBaseId() == null) {
      entity.setBookBase(bookBase);
    } else if (!oldPhoto.getBookBaseId().equals(entity.getBookBaseId())) {
      throw new UnsupportedOperationException(
          "You can't change book base connected to this photo.");
    }

    return super.replace(entity, id, authentication);
    // Проверка прав на редактирование выполнится в super.replace(...)
  }


}


