package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.hse.BookExchange.models.abstractions.DatedEntity;

/**
 * Диалог (чат)
 */
@Entity(name = "Dialog")
@Table(name = "dialog")
public class Dialog extends DatedEntity {

  // Id чата
  private @Id
  @Column(name = "dialog_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Участники диалога
  @ManyToMany(fetch = FetchType.LAZY)
  private List<User> participants = new ArrayList<>();

  // Сообщения
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "dialog", orphanRemoval = true)
  private List<Message> messages = new ArrayList<>();

  // Поле для парсинга участников
  @Transient
  private transient List<Long> participantIds;

  // Первое сообщение
  @Transient
  private transient Message initialMessage;

  @OneToOne(mappedBy = "dialog")
  private BookExchangeRequest exchangeRequest;


  public Dialog() {
  }

  public Dialog(List<User> participants) {
    this.participants = participants;
  }

  public Dialog(List<User> participants, Message initialMessage) {
    this.participants = participants;
    addMessage(initialMessage);
  }

  /**
   * Возвращает id диалога
   *
   * @return id диалога
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Устаеавливает id диалога
   *
   * @param id id диалога
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает участников диалога
   *
   * @return участников диалога
   */
  @JsonIgnore
  public List<User> getParticipants() {
    return participants;
  }

  /**
   * Устанавливает участников диалога
   *
   * @param participants участников диалога
   */
  public void setParticipants(
      List<User> participants) {
    this.participants = participants;
  }

  /**
   * Возвращает id участников диалога
   *
   * @return id участников диалога
   */
  public List<Long> getParticipantIds() {
    if (participantIds == null) {
      return participants.stream().map(User::getId)
          .collect(Collectors.toList());
    }
    return participantIds;
  }

  /**
   * Устанавливает id участников диалога
   *
   * @param participantIds id участников диалога
   */
  public void setParticipantIds(List<Long> participantIds) {
    this.participantIds = participantIds;
  }

  /**
   * Возвращает сообщения в диалоге
   *
   * @return сообщения в диалоге
   */
  @JsonIgnore
  public List<Message> getMessages() {
    return messages;
  }

  /**
   * Устанавливает сообщения в диалоге
   *
   * @param messages сообщения в диалоге
   */
  public void setMessages(List<Message> messages) {
    this.messages = messages;
    for (Message msg : messages) {
      msg.setDialog(this);
    }
  }

  /**
   * Возвращает id сообщений в диалоге
   *
   * @return id сообщений в диалоге
   */
  public List<Long> getMessageIds() {
    return messages.stream().map(Message::getId).collect(Collectors.toList());
  }

  /**
   * Добавляет сообщение в диалог
   *
   * @param message сообщение
   */
  public void addMessage(Message message) {
    message.setDialog(this);
    messages.add(message);
  }

  /**
   * Возвращает инициирующее сообщение
   *
   * @return инициирующее сообщение
   */
  @JsonIgnore
  public Message getInitialMessage() {
    return initialMessage;
  }

  /**
   * Устанавливает  инициирующее сообщение
   *
   * @param initialMessage инициирующее сообщение
   */
  public void setInitialMessage(Message initialMessage) {
    this.initialMessage = initialMessage;
  }

  @JsonIgnore
  public BookExchangeRequest getExchangeRequest() {
    return exchangeRequest;
  }

  public Long getExchangeRequestId() {
    if (exchangeRequest == null) {
      return null;
    }
    return exchangeRequest.getId();
  }

  public void setExchangeRequest(
      BookExchangeRequest exchangeRequest) {
    this.exchangeRequest = exchangeRequest;
  }
}
