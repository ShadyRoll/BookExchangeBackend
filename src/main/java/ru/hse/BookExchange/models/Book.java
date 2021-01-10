package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.hse.BookExchange.models.abstractions.Created;
import ru.hse.BookExchange.models.abstractions.DatedEntity;

/**
 * Книга для обмена
 */
@SuppressWarnings("unused")
@Entity(name = "Book")
@Table(name = "book")
public class Book extends DatedEntity implements Created {

  // Id книги для обмена
  private @Id
  @Column(name = "book_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  /**
   * Уровень поношенности книги
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exterior_quality_id")
  private ExteriorQuality exteriorQuality;

  /**
   * Создатель книги для обмена (добавивший ее в бд)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id")
  private User creator;

  /**
   * Текущий владелец книги
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private User owner;

  /**
   * Город, в котором находится книга
   */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "town_id")
  private Town town;

  /**
   * Фотография книги
   */
  @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
  private BookPhoto photo;

  /**
   * Книга из списка книг (bookBase) сервиса, которой является этот экземпляра
   * для обмена
   */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "book_base_id")
  private BookBase base;

  /**
   * Список запросов на обмен с этой книгой
   */
  @OneToMany(mappedBy = "exchangingBook", orphanRemoval = true)
  private List<BookExchangeRequest> exchangeRequests = new ArrayList<>();

  /**
   * Статус публичности книги для обмена
   */
  @Column(name = "publicity_status")
  private PublicityStatus publicityStatus;

  // Поля для парсинга
  @Transient
  private transient Long creatorId, baseId, ownerId, townId, exteriorQualityId;

  public Book() {
    publicityStatus = PublicityStatus.Public;
  }

  public Book(ExteriorQuality exteriorQuality, User creator, BookBase base,
      Town town) {
    this();
    this.exteriorQuality = exteriorQuality;
    // TODO: Разделить creator и owner, это могут быть разные люди
    this.creator = creator;
    this.owner = creator;
    this.town = town;
    this.base = base;
    base.addBook(this);
    town.addBook(this);
  }

  /**
   * Возвращает id книги-прототика (bookBase)
   *
   * @return id книги-прототика (bookBase)
   */
  public Long getBaseId() {
    if (baseId == null) {
      if (base == null) {
        return null;
      }
      return base.getId();
    }
    return baseId;
  }

  /**
   * Устанавливает id книги-прототипа (bookBase)
   *
   * @param baseId id книги-прототипа (bookBase)
   */
  public void setBaseId(Long baseId) {
    this.baseId = baseId;
  }

  /**
   * Возвращает id создателя
   *
   * @return id создателя
   */
  @Override
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
   * Устанавливает id создателя книги
   *
   * @param creatorId id создателя книги
   */
  public void setCreatorId(Long creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * Возвращает id владельца
   *
   * @return id владельца
   */
  public Long getOwnerId() {
    if (ownerId == null) {
      if (owner == null) {
        return null;
      }
      return owner.getId();
    }
    return ownerId;
  }

  /**
   * Устанавливает id владельца книги
   *
   * @param ownerId id владельца книги
   */
  public void setOwnerId(Long ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * Возвращает id города
   *
   * @return id города
   */
  public Long getTownId() {
    if (townId == null) {
      if (town == null) {
        return null;
      }
      return town.getId();
    }
    return townId;
  }

  /**
   * Устанавливает id города для передачи книги
   *
   * @param townId id города для передачи книги
   */
  public void setTownId(Long townId) {
    this.townId = townId;
  }

  /**
   * Возвращает id уровня поношенности
   *
   * @return id уровня поношенности
   */
  public Long getExteriorQualityId() {
    if (exteriorQualityId == null) {
      if (exteriorQuality == null) {
        return null;
      }
      return exteriorQuality.getId();
    }
    return exteriorQualityId;
  }

  /**
   * Устанавливает id уровня публичности
   *
   * @param exteriorQualityId id уровня публичности
   */
  public void setExteriorQualityId(Long exteriorQualityId) {
    this.exteriorQualityId = exteriorQualityId;
  }

  /**
   * Возвращает уровень публичности
   *
   * @return уровень публичности
   */
  public PublicityStatus getPublicityStatus() {
    return publicityStatus;
  }

  /**
   * Устанавливает уровень публичности
   *
   * @param publicityStatus уровень публичности
   */
  public void setPublicityStatus(PublicityStatus publicityStatus) {
    this.publicityStatus = publicityStatus;
  }

  /**
   * Возвращает id
   *
   * @return id
   */
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id книги
   *
   * @param id id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает уровень поношенности книги
   *
   * @return уровень поношенности книги
   */
  @JsonIgnore
  public ExteriorQuality getExteriorQuality() {
    return exteriorQuality;
  }

  /**
   * Устанавливает уровень поношенности книги
   *
   * @param exteriorQuality уровень поношенности книги
   */
  public void setExteriorQuality(ExteriorQuality exteriorQuality) {
    this.exteriorQuality = exteriorQuality;
  }

  /**
   * Возвращает книгу-прототип
   *
   * @return книгу-прототип
   */
  @JsonIgnore
  public BookBase getBase() {
    return base;
  }

  /**
   * Устанавливает книгу-прототип
   *
   * @param base книгу-прототип
   */
  public void setBase(BookBase base) {
    this.base = base;
  }

  /**
   * Возвращает создателя
   *
   * @return создателя
   */
  @JsonIgnore
  public User getCreator() {
    return creator;
  }

  /**
   * Устанавливает создателя
   *
   * @param creator создателя
   */
  public void setCreator(User creator) {
    this.creator = creator;
  }

  /**
   * Возвращает владельца
   *
   * @return владельца
   */
  @JsonIgnore
  public User getOwner() {
    return owner;
  }

  /**
   * Устанавливает владельца
   *
   * @param owner владельца
   */
  public void setOwner(User owner) {
    this.owner = owner;
  }

  /**
   * Возвращает город для передачи книги
   *
   * @return город
   */
  @JsonIgnore
  public Town getTown() {
    return town;
  }

  /**
   * Устанавливает город для передачи книги
   *
   * @param town город для передачи книги
   */
  public void setTown(Town town) {
    this.town = town;
  }

  /**
   * Возвращает фотографию книги
   *
   * @return фотографию книги
   */
  @JsonIgnore
  public BookPhoto getPhoto() {
    return photo;
  }

  /**
   * Устанавливает фото книги
   *
   * @param photo фото книги
   */
  public void setPhoto(BookPhoto photo) {
    this.photo = photo;
  }

  /**
   * Возвращает id фотографии книги
   *
   * @return id фотографии книги
   */
  public Long getPhotoId() {
    if (photo == null) {
      return null;
    }
    return photo.getId();
  }

  /**
   * Возвращает список запросов на передачу книги
   *
   * @return список запросов на передачу книги
   */
  @JsonIgnore
  public List<BookExchangeRequest> getExchangeRequests() {
    return exchangeRequests;
  }

  /**
   * Устанавливает список запросов на передачу книги
   *
   * @param exchangeRequests список запросов на передачу книги
   */
  public void setExchangeRequests(
      List<BookExchangeRequest> exchangeRequests) {
    this.exchangeRequests = exchangeRequests;
  }

  /**
   * Возвращает список id запросов на передачу книги
   *
   * @return список id запросов на передачу книги
   */
  public List<Long> getExchangeRequestIds() {
    return exchangeRequests.stream().map(BookExchangeRequest::getId).collect(
        Collectors.toList());
  }

  /**
   * Переводит информацию о книге в строку
   *
   * @return информация о книге
   */
  @Override
  public String toString() {
    return "Book{" +
        "id=" + id +
        ", exteriorQualityId=" + getExteriorQualityId() +
        ", creatorId=" + getCreatorId() +
        ", baseId=" + getBaseId() +
        ", townId=" + getTownId() +
        '}';
  }

  /**
   * Уровень публичности книги
   */
  public enum PublicityStatus {
    Private, Public
  }
}
