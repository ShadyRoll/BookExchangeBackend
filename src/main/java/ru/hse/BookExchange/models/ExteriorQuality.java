package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import ru.hse.BookExchange.models.abstractions.DatedEntity;

/**
 * Уровень поношенности книги
 */
@SuppressWarnings("unused")
@Entity(name = "ExteriorQuality")
@Table(name = "exterior_quality")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class ExteriorQuality extends DatedEntity {

  // Id уровня поношенности книги
  private @Id
  @Column(name = "exterior_quality_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Название
  private String name;

  // Книги с этим уровнем поношенности
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "exteriorQuality")
  private List<Book> books;

  public ExteriorQuality() {
  }

  public ExteriorQuality(String name) {
    this.name = name;
  }

  /**
   * Возвращает название уровня поношенности книги
   *
   * @return название уровеня поношенности книги
   */
  public String getName() {
    return name;
  }

  /**
   * Устанавливает название уровня поношенности книги
   *
   * @param name название уровня поношенности книги
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Возвращает id уровня поношенности книги
   *
   * @return id уровня поношенности книги
   */
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id уровня поношенности книги
   *
   * @param id id уровня поношенности книги
   */
  public void setId(Long id) {
  }

  /**
   * Возвращает список id книг с этис уровнем поношенности
   *
   * @return список id книг с этис уровнем поношенности
   */
  @JsonIgnore
  public List<Long> getBookIds() {
    return books.stream().map(Book::getId).collect(Collectors.toList());
  }

  /**
   * Устанавливает книги с этим уровнем поношенности
   *
   * @param books книги
   */
  public void setBooks(List<Book> books) {
    this.books = books;
  }

  /**
   * Преобразует информацию об уровне поношенности в строку
   *
   * @return строка с инофрмацией об уровне поношенности книги
   */
  @Override
  public String toString() {
    return "ExteriorQuality{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
