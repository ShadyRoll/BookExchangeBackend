package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import ru.hse.BookExchange.models.abstractions.DatedEntity;

/**
 * Город для обмена
 */
@SuppressWarnings("unused")
@Entity(name = "Town")
@Table(name = "town")
public class Town extends DatedEntity implements Serializable {

  // Id города
  private @Id
  @Column(name = "town_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Название города
  private String name;


  // Список книг для передачи в этом городе
  @OneToMany(mappedBy = "town", orphanRemoval = true)
  private List<Book> books = new ArrayList<>();

  @OneToMany(mappedBy = "town", orphanRemoval = false)
  private List<User> users = new ArrayList<>();

  public Town() {
  }

  public Town(String name) {
    this.name = name;
  }

  /**
   * Возвращает id города
   *
   * @return id города
   */
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает название города
   *
   * @return название города
   */
  public String getName() {
    return name;
  }

  /**
   * Устанавливает название города
   *
   * @param name название города
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Возвращает список id книг в этом города
   *
   * @return список id книг в этом города
   */
  public List<Long> getBookIds() {
    return books.stream().map(Book::getId).collect(Collectors.toList());
  }

  /**
   * Устанавливает список книг
   *
   * @param books список книг
   */
  public void setBooks(
      List<Book> books) {
    this.books = books;
  }

  /**
   * Добавляет книгу
   *
   * @param book книга
   */
  public void addBook(Book book) {
    books.add(book);
  }

  @JsonIgnore
  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  /**
   * Преобразует информацию о городе в строку
   *
   * @return строка с информацией о городе
   */
  @Override
  public String toString() {
    return "Town{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        ", bookIds=" + getBookIds() +
        '}';
  }
}
