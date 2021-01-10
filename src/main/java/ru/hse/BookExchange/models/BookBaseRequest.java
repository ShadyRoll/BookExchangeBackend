package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.hse.BookExchange.models.abstractions.Request;

/**
 * Запрос на добавление книги (bookBase) в бд
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties(value = {"rating"})
@Entity(name = "BookBaseRequest")
@Table(name = "book_base_request")
public class BookBaseRequest extends BookBase implements Request {

  // Id запроса
  private @Id
  @Column(name = "book_base_request_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Статус запроса
  @Enumerated(EnumType.ORDINAL)
  RequestStatus status;

  // Создатель запроса
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User creator;

  // Жанры
  @Transient
  protected transient List<Long> genreIds = new ArrayList<>();

  private String genreStr;


  public BookBaseRequest() {
    status = RequestStatus.Pending;
  }

  public BookBaseRequest(BookBase bookBase) {
    super(bookBase.getAuthor(), bookBase.getLanguage(), bookBase.getTitle(),
        bookBase.getNumberOfPages(), bookBase.getPublishYear(),
        bookBase.getGenres(), bookBase.getDescription());
    setGenreIds(bookBase.getGenres().stream().map(Genre::getId).collect(
        Collectors.toList()));
    status = RequestStatus.Pending;
  }


  public BookBaseRequest(BookBase bookBase, User creator) {
    this(bookBase);
    this.creator = creator;
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
  @JsonIgnore
  @Override
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
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id
   *
   * @param id id
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Устанавливает id суперкласса(bookBase)
   *
   * @param id id суперкласса(bookBase)
   */
  public void setSuperId(Long id) {
    super.id = id;
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
   * @param status статус запроса
   */
  @Override
  public void setStatus(RequestStatus status) {
    this.status = status;
  }

  @Override
  public List<Long> getGenreIds() {
    if (genreIds != null && genreIds.size() > 0) {
      return genreIds;
    }

    if (genres != null && genres.size()>0) {
      return genres.stream().map(Genre::getId)
          .collect(Collectors.toList());
    }

    if (genreStr == null) {
      return new ArrayList<>();
    }

    return Arrays
        .stream(genreStr.substring(1, genreStr.length() - 1).split(", "))
        .map(str -> Long.parseLong(str.trim())).collect(
            Collectors.toList());
  }

  public void setGenreIds(List<Long> genreIds) {
    this.genreIds = genreIds;
    genreStr = Arrays.toString(genreIds.toArray());
  }
}
