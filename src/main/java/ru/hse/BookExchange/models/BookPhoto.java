package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.hse.BookExchange.models.abstractions.Picture;

/**
 * Фотография книги для передачи
 */
@SuppressWarnings("unused")
@Entity(name = "BookPhoto")
@Table(name = "book_photo")
public class BookPhoto extends Picture {

  // Id фотографии книги
  private @Id
  @Column(name = "book_photo_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Книга
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id")
  private Book book;

  // Поле для парсинга книги
  @Transient
  private transient Long bookId;

  public BookPhoto() {
  }

  public BookPhoto(byte[] pic, User creator, Book book) {
    super(pic, creator);
    this.book = book;
  }

  /**
   * Возвращает книгу
   *
   * @return книгу
   */
  @JsonIgnore
  public Long getBook() {
    return book.getId();
  }

  /**
   * Устанавливает книгу
   *
   * @param book книга
   */
  public void setBook(Book book) {
    this.book = book;
  }

  /**
   * Возвращает id фотографии книги
   *
   * @return id фотографии книги
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Устанавливает id фотографии книги
   *
   * @param id фотографии книги
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает id книги
   *
   * @return id книги
   */
  public Long getBookId() {
    if (bookId == null) {
      if (book == null) {
        return null;
      }
      return book.getId();
    }
    return bookId;
  }

  /**
   * Устанавливает id книги
   *
   * @param bookId id книги
   */
  public void setBookId(Long bookId) {
    this.bookId = bookId;
  }

}
