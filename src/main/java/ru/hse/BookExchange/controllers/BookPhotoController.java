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
import ru.hse.BookExchange.exceptions.BookNotFoundException;
import ru.hse.BookExchange.exceptions.EntityNotFoundException;
import ru.hse.BookExchange.exceptions.NullIdException;
import ru.hse.BookExchange.models.Book;
import ru.hse.BookExchange.models.BookPhoto;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.BookPhotoRepository;
import ru.hse.BookExchange.repositories.BookRepository;

/**
 * Контроллер фотографий книг для обмена (book)
 */
@RestController
@RequestMapping("bookPhoto")
public class BookPhotoController extends
    CreatedDatedEntityController<BookPhoto> {

  // Репозиторий книг для обмена
  private final BookRepository bookRepository;

  BookPhotoController(BookPhotoRepository repository,
      BookRepository bookRepository) {
    super(repository);
    this.bookRepository = bookRepository;
  }

  /**
   * Возвращает список фотографий книг для обмена (book)
   *
   * @param skip           - сколько пропустить
   * @param limit          - сколько вернуть
   * @param latest         - отсортировать ли по дате
   * @param ids            - id массива фотографий (null, если не нужно)
   * @param authentication - данные авторизации
   * @return список фотографий  книг для обмена (book)
   */
  @Override
  @GetMapping
  public List<BookPhoto> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @RequestParam(required = false) List<Long> ids,
      Authentication authentication) {
    throw new UnsupportedOperationException(
        "Request rejected to avoid huge data transmission (get picture by id instead).");
  }

  /**
   * Добавляет фотографию книги в бд
   *
   * @param entity         - фотография книги
   * @param authentication - данные авторизации
   * @return сохраненная фотография книги
   */
  @Override
  @PostMapping
  public BookPhoto add(@RequestBody BookPhoto entity,
      Authentication authentication) {
    /*  DEBUG ONLY
    try {
      ImageIO.write(ImageIO.read(new ByteArrayInputStream(entity.getImage())),
          "jpg",
          new File("C:/Users/Shado/Desktop/TermWork/files/bookPhoto.jpg"));
    } catch (IOException e) {
      e.printStackTrace();
    }*/

    User creator = auth.isUser(authentication);
    entity.setCreator(creator);

    Long bookId = entity.getBookId();
    if (bookId == null) {
      throw new NullIdException("bookId");
    }

    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    book.setPhoto(entity);
    entity.setBook(book);
    var res = repository.saveAndFlush(entity);
    bookRepository.saveAndFlush(book);

    return res;
  }

  /**
   * Заменяет фотографию книги для обмена (book) в бд
   *
   * @param entity         - новая фотография книги для обмена
   * @param id             - id фотографии книги
   * @param authentication - данные аутентификации
   * @return сохраненная новая фотография книги для обмена
   */
  @Override
  @PutMapping("/{id}")
  public BookPhoto replace(@RequestBody BookPhoto entity,
      @PathVariable Long id,
      Authentication authentication) {
    /*  DEBUG ONLY
    try {
      ImageIO.write(ImageIO.read(new ByteArrayInputStream(entity.getImage())),
          "jpg",
          new File("C:/Users/Shado/Desktop/TermWork/files/bookPhoto.jpg"));
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    hasAccessToModify(id, authentication);
    BookPhoto oldPhoto = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("bookPhoto", id));
    Book book = bookRepository.findById(oldPhoto.getBookId())
        .orElseThrow(() -> new IllegalArgumentException(String.format(
            "Somehow original bookPhoto(id = %s) has null book field. (error in db)",
            id)));

    if (entity.getBookId() == null) {
      entity.setBook(book);
    } else if (!oldPhoto.getBookId().equals(entity.getBookId())) {
      throw new UnsupportedOperationException(
          "You can't change book connected to this photo.");
    }

    return super.replace(entity, id, authentication);
    // Проверка прав на редактирование выполнится в super.replace(...)
  }


}


