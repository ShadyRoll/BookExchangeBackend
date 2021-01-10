package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.hse.BookExchange.models.abstractions.DatedEntity;

/**
 * Книга (книга-бразец)
 */
@SuppressWarnings("unused")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity(name = "BookBase")
@Table(name = "book_base")
public class BookBase extends DatedEntity {

  // Id книги
  protected @Id
  @Column(name = "book_base_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Список пользователей, добавивший книгу в избранное
  @ManyToMany(mappedBy = "wishList")
  protected final List<User> wishers = new ArrayList<>();

  // Автор книги
  protected String author;

  // Язык книги
  @Enumerated(EnumType.ORDINAL)
  protected Language language;

  // Заголовок книги
  protected String title;

  // Количество страниц в книге
  @Column(name = "number_of_pages")
  protected Integer numberOfPages;

  // Год выпуска книги
  protected Integer publishYear;

  // Описание книги
  @Column(columnDefinition = "varchar(1000)")
  protected String description;

  // Жанры
  @ManyToMany(mappedBy = "bookBases")
  protected List<Genre> genres = new ArrayList<>();

  // Объекты книг для передачи (book), основанные на этой книге-образце
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "base", cascade = CascadeType.REMOVE)
  protected List<Book> books = new ArrayList<>();

  // Оценки книги
  @OneToMany(mappedBy = "ratedBookBase", cascade = CascadeType.REMOVE)
  protected List<BookBaseRate> rates = new ArrayList<>();

  // Обложка книги (фото книги)
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private BookBasePhoto photo;

  @Transient
  private transient List<Long> genreIds;

  @Transient
  private transient byte[] photoTrans;


  public BookBase() {
  }

  public BookBase(String author, Language language, String title,
      int numberOfPages, int publishYear, List<Genre> genres) {
    this.author = author;
    this.language = language;
    this.title = title;
    this.numberOfPages = numberOfPages;
    this.publishYear = publishYear;
    this.genres = genres;

    for (Genre genre : genres) {
      genre.addBookBase(this);
    }
  }

  public BookBase(String author, Language language, String title,
      int numberOfPages, int publishYear, List<Genre> genres,
      String description) {
    this(author, language, title, numberOfPages, publishYear, genres);
    this.description = description;
  }

  public BookBase(BookBaseRequest request) {
    this(request.getAuthor(), request.getLanguage(), request.getTitle(),
        request.getNumberOfPages(), request.getPublishYear(),
        List.copyOf(request.getGenres()),
        request.getDescription());
    this.setPhoto(request.getPhoto());
  }

  /**
   * Возвращает список id жанров книги
   *
   * @return список id жанров книги
   */
  public List<Long> getGenreIds() {
    if (genreIds != null && genreIds.size() > 0) {
      return genreIds;
    }
    if (genres == null) {
      return null;
    }
    return getGenres().stream().map(Genre::getId).collect(Collectors.toList());
  }


  public void setGenreIds(List<Long> genreIds) {
    this.genreIds = genreIds;
  }


  /**
   * Добавляет пользователя, добавившего эту книгу в избранное, в список
   *
   * @param user пользователь, добавивший эту книгу в избранное
   */
  public void addWisher(User user) {
    if (!wishers.contains(user)) {
      wishers.add(user);
    }
  }

  /**
   * Удаляет пользователя, удалившего эту книгу из избранных, из списка
   *
   * @param user пользователь, удаливший эту книгу из избранных
   */
  public void removeWisher(User user) {
    wishers.removeIf(u -> u.getId().equals(user.getId()));
  }

  /**
   * Добавляет книгу для передачи в список
   *
   * @param book книгу для передачи
   */
  public void addBook(Book book) {
    books.add(book);
  }

  /**
   * Возвращает рейтинг книги
   *
   * @return список рейтинг книги
   */
  public float getRating() {
    if (rates.size() == 0) {
      return 0.0F;
    }
    double rateSum = 0;
    for (var rate : rates) {
      rateSum += rate.getRate();
    }
    return (float) (rateSum / rates.size());
  }

  /**
   * Добавляет оценку книге
   *
   * @param rate оценка книги
   */
  public void addRate(BookBaseRate rate) {
    rates.add(rate);
  }

  /**
   * Сравнивает книги
   *
   * @param o - другая книга
   * @return одинаковы ли книги
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof BookBase)) {
      return false;
    }
    BookBase book = (BookBase) o;
    return this.id.equals(book.id) && this.title.equals(book.title)
        && this.author.equals(book.author)
        && this.language.equals(book.language)
        && (numberOfPages.equals(book.numberOfPages))
        && (publishYear.equals(book.publishYear))
        && (description.equals(book.description));
  }

  /**
   * Рассчитывает код для хеширования книги
   *
   * @return код для хеширования книги
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.id, this.title, this.author, this.language,
        this.numberOfPages, this.publishYear);
  }

  /**
   * Возвращает id книги
   *
   * @return id книги
   */
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id книге
   *
   * @param id id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает автора книги
   *
   * @return автора книги
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Устанавливает автора книги
   *
   * @param author автор книги
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * Возвращает язык книги
   *
   * @return язык книги
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * Устанавливает язык книги
   *
   * @param language язык книги
   */
  public void setLanguage(Language language) {
    this.language = language;
  }

  /**
   * Возвращает заголовок книги
   *
   * @return заголовок книги
   */
  public String getTitle() {
    return title;
  }

  /**
   * Устанавливает заголовок книги
   *
   * @param title заголовок книги
   */
  public void setTitle(String title) {
    this.title = title;
  }


  /**
   * Возвращает количество страниц в книге
   *
   * @return количество страниц в книге
   */
  public int getNumberOfPages() {
    return numberOfPages;
  }

  /**
   * Устанавливает количество страниц в книге
   *
   * @param numberOfPages количество страниц в книге
   */
  public void setNumberOfPages(int numberOfPages) {
    this.numberOfPages = numberOfPages;
  }

  /**
   * Возвращает список id связанных книг для передачи (book)
   *
   * @return список id связанных книг для передачи (book)
   */
  public List<Long> getBookIds() {
    return books.stream().map(Book::getId).collect(Collectors.toList());
  }

  /**
   * Возвращает список связанных книг для передачи (book)
   *
   * @return список связанных книг для передачи (book)
   */
  @JsonIgnore
  public List<Book> getBooks() {
    return books;
  }

  /**
   * Устанавливает список связанных книг для передачи (book)
   *
   * @param books список связанных книг для передачи (book)
   */
  public void setBooks(List<Book> books) {
    this.books = books;
  }

  /**
   * Возвращает год публикации книги
   *
   * @return год публикации книги
   */
  public Integer getPublishYear() {
    return publishYear;
  }

  /**
   * Устанавливает год публикации книги
   *
   * @param publishYear год публикации книги
   */
  public void setPublishYear(int publishYear) {
    this.publishYear = publishYear;
  }

  /**
   * Возвращает список id пользователей, добавивших эту книгу в избранное
   *
   * @return список id пользователей, добавивших эту книгу в избранное
   */
  public List<Long> getWishersIds() {
    return wishers.stream().map(User::getId).collect(Collectors.toList());
  }

  /**
   * Возвращает список жанров книги
   *
   * @return список жанров книги
   */
  @JsonIgnore
  public List<Genre> getGenres() {
    return genres;
  }

  /**
   * Устанавливает список жанров книги
   *
   * @param genres список жанров книги
   */
  public void setGenres(List<Genre> genres) {
    this.genres = genres;
  }

  /**
   * Возвращает описание книги
   *
   * @return описание книги
   */
  public String getDescription() {
    return description;
  }

  /**
   * Устанавливает описание книги
   *
   * @param description описание книги
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Возвращает id обложки книги
   *
   * @return id обложки книги
   */
  public Long getPhotoId() {
    if (photo == null) {
      return null;
    }
    return photo.getId();
  }


  @JsonIgnore
  public BookBasePhoto getPhoto() {
    return photo;
  }


  /**
   * Устанавливает обложку книги
   *
   * @param photo обложку книги
   */
  public void setPhoto(BookBasePhoto photo) {
    this.photo = photo;
  }

  /**
   * Преобразует информацию о книге в строку
   *
   * @return строка с информацией о книге
   */
  @Override
  public String toString() {
    return "BookBase{" +
        "id=" + id +
        ", author='" + author + '\'' +
        ", language=" + language +
        ", title='" + title + '\'' +
        ", numberOfPages=" + numberOfPages +
        ", books(ids)=" + Arrays.toString(books.stream().map(
        Book::getId).toArray()) +
        '}';
  }

  @JsonIgnore
  public byte[] getPhotoTrans() {
    return photoTrans;
  }

  public void setInitialPhoto(byte[] photoTrans) {
    this.photoTrans = photoTrans;
  }

  /**
   * Язык написания книги
   */
  public enum Language {
    RU, ENG
  }
}