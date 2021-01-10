package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.hse.BookExchange.models.abstractions.Created;
import ru.hse.BookExchange.models.abstractions.DatedEntity;

/**
 * Оценка (отзыв) книги (bookBase)
 */
@SuppressWarnings("unused")
@Entity(name = "BookBaseRate")
@Table(name = "book_base_rate")
public class BookBaseRate extends DatedEntity implements Created {

  // Id оценки
  private @Id
  @Column(name = "book_base_rate_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Оцененная книга
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rated_book_base_id")
  private BookBase ratedBookBase;

  // Создатель оценки
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id")
  private User creator;

  // Значение оценки
  @Column(name = "rate")
  private Float rate;

  // Комментарий к оценке
  private String body;

  // Поля для парсинга
  @Transient
  private transient Long creatorId, ratedBookBaseId;

  public BookBaseRate() {
  }

  public BookBaseRate(User creator, BookBase ratedBaseBase, Float rate) {
    this();
    this.creator = creator;
    this.ratedBookBase = ratedBaseBase;
    checkRateValue(rate);
    this.rate = rate;
    ratedBaseBase.addRate(this);
  }

  public BookBaseRate(User creator, BookBase ratedBaseBase, Float rate,
      String body) {
    this(creator, ratedBaseBase, rate);
    this.body = body;
  }

  /**
   * Возвращает id оцененной книги
   *
   * @return id оцененной книги
   */
  public Long getRatedBookBaseId() {
    if (ratedBookBaseId == null) {
      if (ratedBookBase == null) {
        return null;
      }
      return ratedBookBase.getId();
    }
    return ratedBookBaseId;
  }

  /**
   * Устанавливает id оцененной книги
   *
   * @param baseId id оцененной книги
   */
  public void setRatedBookBaseId(Long baseId) {
    this.ratedBookBaseId = baseId;
  }

  /**
   * Возвращает id создателя оценки
   *
   * @return id создателя оценки
   */
  public Long getCreatorId() {
    if (creatorId == null) {
      if (creator == null) {
        return null;
      }
      return creator.getId();
    }
    return creatorId;
  }

  /**
   * Устанавливает id создателя оценки
   *
   * @param creatorId id создателя оценки
   */
  public void setCreatorId(Long creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * Возвращает значение оценки
   *
   * @return значение оценки
   */
  public Float getRate() {
    return rate;
  }

  /**
   * Устанавливает значение оценки
   *
   * @param rate значение оценки
   */
  public void setRate(Float rate) {
    checkRateValue(rate);
    this.rate = rate;
  }

  /**
   * Проверяет корректность значения оценки (она должна быть от 1 до 5
   * включительно)
   *
   * @param rate значение оценки
   * @throws IllegalArgumentException при недопустимом значении оценки
   */
  private void checkRateValue(Float rate) throws IllegalArgumentException {
    if (rate < 1 || rate > 5) {
      throw new IllegalArgumentException("Rate must be in range [1;5]!");
    }
  }

  /**
   * Возвращает id оценки
   *
   * @return id оценки
   */
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id оценки
   *
   * @param id id оценки
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает оцененную книгу
   *
   * @return оцененную книгу
   */
  @JsonIgnore
  public BookBase getRatedBookBase() {
    return ratedBookBase;
  }

  /**
   * Устанавливает оцененную книгу
   *
   * @param base оцененную книгу
   */
  public void setRatedBookBase(BookBase base) {
    this.ratedBookBase = base;
  }

  /**
   * Возвращает создателя оценки
   *
   * @return создателя оценки
   */
  @JsonIgnore
  public User getCreator() {
    return creator;
  }

  /**
   * Устанавливает создателя оценки
   *
   * @param creator создателя оценки
   */
  public void setCreator(User creator) {
    this.creator = creator;
  }

  /**
   * Возвращает комментарий оценки
   *
   * @return комментарий оценки
   */
  public String getBody() {
    return body;
  }

  /**
   * Устанавливает комментарий оценки
   *
   * @param body комментарий оценки
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Возвращает строку с информацией об оценке
   *
   * @return строку с информацией об оценке
   */
  @Override
  public String toString() {
    return "BookBaseRate{" +
        "id=" + id +
        ", rate=" + rate +
        ", body=" + body +
        ", creatorId=" + creatorId +
        ", ratedBookBaseId=" + ratedBookBaseId +
        '}';
  }
}
