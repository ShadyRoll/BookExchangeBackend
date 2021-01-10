package ru.hse.BookExchange.models;

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
 * Аватар пользователя
 */
@SuppressWarnings("unused")
@Entity(name = "Avatar")
@Table(name = "avatar")
public class Avatar extends Picture {

  // Id аватара
  private @Id
  @Column(name = "avatar_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Владелец аватара
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "avatar_owner_id")
  private User avatarOwner;

  @Transient
  private transient Long ownerId;

  public Avatar() {
  }

  public Avatar(byte[] pic, User creatorAndOwner) {
    super(pic, creatorAndOwner);
    this.avatarOwner = creatorAndOwner;
  }

  /**
   * Возвращает id
   *
   * @return id
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
   * Возвращает id владельца аватара
   *
   * @return id владельца аватара
   */
  public Long getOwnerId() {
    if (ownerId != null) {
      return ownerId;
    }
    if (avatarOwner == null) {
      return null;
    }
    return avatarOwner.getId();
  }

  public void setOwnerId(Long ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * Устанавливает владельца аватара
   *
   * @param avatarOwner владелец аватара
   */
  public void setOwner(User avatarOwner) {
    this.avatarOwner = avatarOwner;
  }
}
