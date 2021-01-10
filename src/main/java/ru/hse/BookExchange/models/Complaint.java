package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import ru.hse.BookExchange.models.abstractions.DatedEntity;
import ru.hse.BookExchange.models.abstractions.Request;

/**
 * Жалоба
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties({"complaintIds"})
@Entity(name = "Complaint")
@Table(name = "complaint")
public class Complaint extends DatedEntity implements Request {

  // Id жалобы
  private @Id
  @Column(name = "complaint_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Статус запроса
  @Enumerated(EnumType.ORDINAL)
  RequestStatus status;

  // Создатель запроса
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User creator;

  // Текст жалобы
  private String text;

  // Запись в бд, на которую оставлена жалоба
  @ManyToOne(fetch = FetchType.LAZY)
  private DatedEntity connectedEntity;

  public Complaint() {
    status = BookBaseRequest.RequestStatus.Pending;
  }

  public Complaint(User creator, String text, DatedEntity connectedEntity) {
    this();
    this.creator = creator;
    this.text = text;
    this.connectedEntity = connectedEntity;
  }

  /**
   * Возвращает id жалобы
   *
   * @return id жалобы
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id жалобы
   *
   * @param id id жалобы
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает статус жалобы
   *
   * @return статус жалобы
   */
  @Override
  public RequestStatus getStatus() {
    return status;
  }

  /**
   * Устанавливает статус жалобы
   *
   * @param status статус жалобы
   */
  @Override
  public void setStatus(RequestStatus status) {
    this.status = status;
  }

  /**
   * Возвращает id создателя жалобы
   *
   * @return id создателя жалобы
   */
  @Override
  public Long getCreatorId() {
    return creator.getId();
  }

  /**
   * Возвращает создателя жалобы
   *
   * @return создателя жалобы
   */
  @Override
  @JsonIgnore
  public User getCreator() {
    return creator;
  }

  /**
   * Устанавливает создателя жалобы
   *
   * @param creator создателя жалобы
   */
  @Override
  public void setCreator(User creator) {
    this.creator = creator;
  }

  /**
   * Возвращает текст жалобы
   *
   * @return текст жалобы
   */
  public String getText() {
    return text;
  }

  /**
   * Устанавливает текст жалобы
   *
   * @param text текст жалобы
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Возвращает запись в бд, на которую оставлена жалоба
   *
   * @return текст жалобы
   */
  public DatedEntity getConnectedEntity() {
    return connectedEntity;
  }

  /**
   * Устанавливает запись в бд, на которую оставлена жалоба
   *
   * @param connectedEntity запись в бд, на которую оставлена жалоба
   */
  public void setConnectedEntity(DatedEntity connectedEntity) {
    this.connectedEntity = connectedEntity;
  }
}
