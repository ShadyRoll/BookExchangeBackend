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
import javax.persistence.Table;
import ru.hse.BookExchange.models.abstractions.DatedEntity;

/**
 * Жанр книги
 */
@SuppressWarnings("unused")
@Entity(name = "Genre")
@Table(name = "genre")
public class Genre extends DatedEntity {

  // Id жанра
  private @Id
  @Column(name = "genre_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Название жанра
  private String name;

  // Книги (bookBase) с этим жанром
  @ManyToMany(fetch = FetchType.EAGER)
  private List<BookBase> bookBases = new ArrayList<>();

  public Genre() {
  }

  public Genre(String name) {
    this.name = name;
  }

  /**
   * Возвращает id жанра
   *
   * @return id жанра
   */
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id жанра
   *
   * @param id id жанра
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает название жанра
   *
   * @return название жанра
   */
  public String getName() {
    return name;
  }

  /**
   * Устанавливает название жанра
   *
   * @param name название жанра
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Возвращает книги с этим жанром
   *
   * @return книги с этим жанром
   */
  @JsonIgnore
  public List<BookBase> getBookBases() {
    return bookBases;
  }

  /**
   * Устанавливает список книг этого жанра
   *
   * @param bookBases список книг этого жанра
   */
  public void setBookBases(
      List<BookBase> bookBases) {
    this.bookBases = bookBases;
  }

  /**
   * Возвращает id книг с этим жанром
   *
   * @return id книг с этим жанром
   */
  public List<Long> getBookBaseIds() {
    return bookBases.stream().map(BookBase::getId).collect(Collectors.toList());
  }

  /**
   * Добавляет книгу в список книг этого жанра
   *
   * @param bookBase книга
   */
  public void addBookBase(BookBase bookBase) {
    bookBases.add(bookBase);
  }

  /**
   * Возвращает строку с информацией о книге
   *
   * @return строку с информацией о книге
   */
  @Override
  public String toString() {
    return "Genre{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        ", bookBaseIds=" + getBookBaseIds() +
        '}';
  }
}
