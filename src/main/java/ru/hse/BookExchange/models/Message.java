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
 * Сообщение в диалоге (чате)
 */
@Entity(name = "Message")
@Table(name = "message")
public class Message extends DatedEntity implements Created {

  // Id сообщения
  private @Id
  @Column(name = "message_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Диалог
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dialog_id")
  private Dialog dialog;

  // Текст сообщения
  @Column(name = "message_body")
  private String body;

  // Создатель сообщения
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id")
  private User creator;

  // Получатель сообщения
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  private User receiver;

  // Поля для парсинга
  @Transient
  private transient Long creatorId, receiverId, dialogId;

  public Message() {
  }

  public Message(User creator, User receiver, String body) {
    this.creator = creator;
    this.receiver = receiver;
    this.body = body;
  }

  /**
   * Возвращает id сообщения
   *
   * @return id сообщения
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id сообщения
   *
   * @param id id сообщения
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает id создателя сообщения
   *
   * @return id создателя сообщения
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
   * Устанавливает id создателя сообщения
   *
   * @param creatorId id создателя сообщения
   */
  public void setCreatorId(Long creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * Возвращает создателя сообщения
   *
   * @return создателя сообщения
   */
  @JsonIgnore
  @Override
  public User getCreator() {
    return creator;
  }

  /**
   * Устанавливает создателя сообщения
   *
   * @param creator создатель сообщения
   */
  @Override
  public void setCreator(User creator) {
    this.creator = creator;
  }

  /**
   * Возвращает получателя сообщения
   *
   * @return получателя сообщения
   */
  @JsonIgnore
  public User getReceiver() {
    return receiver;
  }

  /**
   * Устанавливает получателя сообщения
   *
   * @param receiver получатель сообщения
   */
  public void setReceiver(User receiver) {
    this.receiver = receiver;
  }

  /**
   * Возвращает id получателя сообщения
   *
   * @return id получателя сообщения
   */
  public Long getReceiverId() {
    if (receiverId == null) {
      if (receiver == null) {
        return null;
      }
      return receiver.getId();
    }
    return receiverId;
  }

  /**
   * Устанавливает id получателя сообщения
   *
   * @param receiverId id получателя сообщения
   */
  public void setReceiverId(Long receiverId) {
    this.receiverId = receiverId;
  }

  /**
   * Возвращает текст сообщения
   *
   * @return текст сообщения
   */
  public String getBody() {
    return body;
  }

  /**
   * Устанавливает текст сообщения
   *
   * @param body текст сообщения
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Возвращает id диалога
   *
   * @return id диалога
   */
  public Long getDialogId() {
    if (dialogId == null) {
      return dialog.getId();
    }
    return dialogId;
  }

  public void setDialogId(Long dialogId) {
    this.dialogId = dialogId;
  }

  /**
   * Устанавливает диалог
   *
   * @param dialog диалог
   */
  public void setDialog(Dialog dialog) {
    this.dialog = dialog;
  }
}
