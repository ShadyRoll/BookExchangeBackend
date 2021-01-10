package ru.hse.BookExchange.models.abstractions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import ru.hse.BookExchange.models.User;

@MappedSuperclass
public abstract class Picture extends DatedEntity implements Created {

  // Id изображения
  private @Id
  @Column(name = "picture_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Создатель
  @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
  @JoinColumn(name = "user_id")
  private User creator;

  // Изображение
  @Lob
  @Column(name = "image")
  private byte[] image;

  public Picture() {
  }

  public Picture(byte[] pic, User creator) {
    this.image = pic;
    this.creator = creator;
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
   * Устанвливает id
   *
   * @param id id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает изображение
   *
   * @return изображение
   */
  public byte[] getImage() {
    return this.image;
  }

  /**
   * Устанвливает изображение
   *
   * @param pic изображение
   */
  public void setImage(byte[] pic) {
    this.image = pic;
  }

  /**
   * Возвращает id создателя
   *
   * @return id создателя
   */
  @Override
  public Long getCreatorId() {
    return creator.getId();
  }

  /**
   * Возвращает создателя
   *
   * @return создателя
   */
  @JsonIgnore
  @Override
  public User getCreator() {
    return creator;
  }

  /**
   * Устанвливает  создателя
   *
   * @param creator создатель
   */
  @Override
  public void setCreator(User creator) {
    this.creator = creator;
  }

}
