package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.hse.BookExchange.models.abstractions.Picture;

/**
 * Обложка книги (фотография bookBase)
 */
@SuppressWarnings("unused")
@Entity(name = "BookBasePhoto")
@Table(name = "book_base_photo")
public class BookBasePhoto extends Picture {

  // Id обложки
  private @Id
  @Column(name = "book_base_photo_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Книга
  @OneToOne(fetch = FetchType.EAGER, mappedBy = "photo")
  private BookBase bookBase;

  // Поле для парсинга id книги
  @Transient
  private transient Long bookBaseId;

  public BookBasePhoto() {
  }

  public BookBasePhoto(byte[] pic, User creator, BookBase bookBase) {
    super(pic, creator);
    this.bookBase = bookBase;
  }

  /**
   * Возвращает книгу
   *
   * @return книга
   */
  @JsonIgnore
  public Long getBookBase() {
    return bookBase.getId();
  }

  /**
   * Устанавливает книгу
   *
   * @param bookBase книга
   */
  public void setBookBase(BookBase bookBase) {
    this.bookBase = bookBase;
  }

  /**
   * Возвращает id обложки
   *
   * @return id обложки
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Устанавливает id
   *
   * @param id id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает id книги
   *
   * @return id книги
   */
  public Long getBookBaseId() {
    if (bookBaseId == null) {
      if (bookBase == null) {
        return null;
      }
      return bookBase.getId();
    }
    return bookBaseId;
  }

  /**
   * Устанавливает id книги
   *
   * @param bookBaseId id книги
   */
  public void setBookBaseId(Long bookBaseId) {
    this.bookBaseId = bookBaseId;
  }
}
