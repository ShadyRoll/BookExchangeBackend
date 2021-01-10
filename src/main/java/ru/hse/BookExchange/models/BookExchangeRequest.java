package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.hse.BookExchange.models.abstractions.DatedEntity;
import ru.hse.BookExchange.models.abstractions.Request;

/**
 * Запрос на передачу книги
 */
@SuppressWarnings("unused")
@Entity(name = "BookExchangeRequest")
@Table(name = "book_exchange_request")
public class BookExchangeRequest extends DatedEntity implements Request {

  // Id запроса
  private @Id
  @Column(name = "book_exchange_request_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Статус запроса
  @Enumerated(EnumType.ORDINAL)
  RequestStatus status;

  // Создатель запроса
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id")
  private User creator;

  // Пользователь, который должен передать книгу
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_from_id")
  private User userFrom;

  // Пользователь, который должен получить книгу
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_to_id")
  private User userTo;

  // Передаваемая книга
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exchanging_book_id")
  private Book exchangingBook;

  @OneToOne(orphanRemoval = true)
  private Dialog dialog;

  // Поля для парсинга
  @Transient
  private transient Long userFromId, userToId, exchangingBookId;

  public BookExchangeRequest() {
    status = RequestStatus.Pending;
  }

  public BookExchangeRequest(User from, User to, Book exchangingBook,
      Dialog dialog) {
    this();
    this.exchangingBook = exchangingBook;
    userFrom = from;
    userFrom.addOutcomingBookExchangeRequest(this);
    userTo = to;
    userTo.addIncomingBookExchangeRequest(this);
    creator = userTo;
    this.dialog = dialog;
  }

  /**
   * Возвращает id создателя запроса
   *
   * @return id создателя запроса
   */
  @Override
  public Long getCreatorId() {
    return creator.getId();
  }

  /**
   * Возвращает создателя запроса
   *
   * @return создателя запроса
   */
  @Override
  @JsonIgnore
  public User getCreator() {
    return creator;
  }

  /**
   * Устанавливает создателя запроса
   *
   * @param creator создатель запроса
   */
  @Override
  public void setCreator(User creator) {
    this.creator = creator;
  }

  /**
   * Возвращает id запроса
   *
   * @return id запроса
   */
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id запроса
   *
   * @param id id запроса
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает статус запроса
   *
   * @return статус запроса
   */
  @Override
  public RequestStatus getStatus() {
    return status;
  }

  /**
   * Устанавливает статус запроса
   *
   * @param status статутс запроса
   */
  @Override
  public void setStatus(RequestStatus status) {
    this.status = status;
  }

  /**
   * Возвращает пользователя, который должен передать книгу
   *
   * @return пользователя, который должен передать книгу
   */
  @JsonIgnore
  public User getUserFrom() {
    return userFrom;
  }

  /**
   * Устанавливает пользователя, который должен передать книгу
   *
   * @param userFrom пользователя, который должен передать книгу
   */
  public void setUserFrom(User userFrom) {
    if (this.userFrom != null) {
      throw new IllegalArgumentException(
          "You cannot change users in exchange! (make another request)");
    }
    userFrom.addOutcomingBookExchangeRequest(this);
    this.userFrom = userFrom;
  }

  /**
   * Возвращает id пользователя, который должен передать книгу
   *
   * @return id пользователя, который должен передать книгу
   */
  public Long getUserFromId() {
    if (userFromId == null) {
      if (userFrom == null) {
        return null;
      }
      return userFrom.getId();
    }
    return userFromId;
  }

  /**
   * Возвращает id передаваемой книги
   *
   * @return id передаваемой книги
   */
  public Long getExchangingBookId() {
    if (exchangingBookId == null) {
      if (exchangingBook == null) {
        return null;
      }
      return exchangingBook.getId();
    }
    return exchangingBookId;
  }

  /**
   * Возвращает пользователя, который получит книгу, после передачи
   *
   * @return пользователя, который получит книгу, после передачи
   */
  @JsonIgnore
  public User getUserTo() {
    return userTo;
  }

  /**
   * Устанавливает пользователя, который получит книгу, после передачи
   *
   * @param userTo пользователь, который получит книгу, после передачи
   */
  public void setUserTo(User userTo) {
    if (this.userTo != null) {
      throw new IllegalArgumentException(
          "You cannot change users in exchange! (make another request)");
    }
    userTo.addIncomingBookExchangeRequest(this);
    this.userTo = userTo;
  }

  /**
   * Возвращает id пользователя, который получит книгу, после передачи
   *
   * @return id пользователя, который получит книгу, после передачи
   */
  public Long getUserToId() {
    if (userToId == null) {
      if (userTo == null) {
        return null;
      }
      return userTo.getId();
    }
    return userToId;
  }

  /**
   * Возвращает передаваемую книгу
   *
   * @return передаваемую книгу
   */
  @JsonIgnore
  public Book getExchangingBook() {
    return exchangingBook;
  }

  /**
   * Устанавливает передаваемую книгу
   *
   * @param exchangingBook передаваемая книга
   */
  public void setExchangingBook(Book exchangingBook) {
    this.exchangingBook = exchangingBook;
  }

  @JsonIgnore
  public Dialog getDialog() {
    return dialog;
  }

  public void setDialog(Dialog dialog) {
    this.dialog = dialog;
  }

  public Long getDialogId() {
    if (dialog == null) {
      return null;
    }
    return dialog.getId();
  }

}
