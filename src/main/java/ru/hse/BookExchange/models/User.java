package ru.hse.BookExchange.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.hse.BookExchange.models.Book.PublicityStatus;
import ru.hse.BookExchange.models.abstractions.DatedEntity;


/**
 * Пользователь
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties("hibernateLazyInitializer")
@Entity(name = "User")
@Table(name = "user_")
public class User extends DatedEntity implements UserDetails {

  // Префикс роли
  static final String ROLE_PREFIX = "ROLE_";

  // Id пользователя
  private @Id
  @Column(name = "user_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  //Роль пользователя
  @Enumerated
  private Role role;

  // Юзернейм (логин) пользователя
  private String username;

  // Пароль пользователя
  @JsonProperty(access = Access.WRITE_ONLY)
  private String password;

  // Имя (ФИО) пользователя
  private String name;

  // Заблокирован ли пользователь
  @Column(name = "is_blocked")
  private boolean isBlocked;

  // Список желаемых книг пользователя
  @ManyToMany
  private List<BookBase> wishList = new ArrayList<>();

  /*
  @OneToMany(mappedBy = "creator")
  private List<Message> sentMessages;
  @OneToMany(mappedBy = "receiver")
  private List<Message> receivedMessages;
  */

  // Список книг для передачи
  @OneToMany(mappedBy = "owner")
  private List<Book> exchangeList = new ArrayList<>();

  // Запросы на добавление книги (bookBase)
  @OneToMany(mappedBy = "creator")
  private List<BookBaseRequest> bookBaseAddRequests = new ArrayList<>();

  // Запросы на передачу книги, в которых пользователь отдает книгу
  @OneToMany(mappedBy = "userFrom")
  private List<BookExchangeRequest> giveBookExchangeRequests = new ArrayList<>();

  // Запросы на передачу книги, в которых пользователь получает книгу
  @OneToMany(mappedBy = "userTo")
  private List<BookExchangeRequest> getBookExchangeRequests = new ArrayList<>();

  // Жалобы, оставленные пользователем
  @OneToMany(mappedBy = "creator")
  private List<Complaint> complaintsList = new ArrayList<>();

  @ManyToOne
  private Town town;

  @Transient
  private transient Long townId;

  // Аватар пользователя
  @OneToOne(mappedBy = "avatarOwner")
  private Avatar avatar;

  public User() {
    this.role = Role.None;
    isBlocked = false;
  }

  public User(Role role, String username, String password, String name,
      Town town) {
    this();
    this.role = role;
    this.username = username;
    this.password = password;
    this.name = name;
    this.town = town;
  }

  /**
   * Добавляет книгу (bookBase) в список любимых книг
   *
   * @param book книга
   */
  public void addToWishList(BookBase book) {
    if (wishList.stream().noneMatch(b -> b.getId().equals(book.getId()))) {
      wishList.add(book);
    }
  }

  /**
   * Убирает книгу (bookBase) из списка любимых книг
   *
   * @param book книга
   */
  public void removeFromWishList(BookBase book) {
    wishList.removeIf(b -> b.getId().equals(book.getId()));
  }

  /**
   * Добавляет книгу (book) в список книг для передачи
   *
   * @param book книга (book)
   */
  public void addBookToExchangeList(Book book) {
    exchangeList.add(book);
  }

  /**
   * Удаляет книгу (book) из списка книг для передачи
   *
   * @param book книга (book)
   */
  public void removeBookFromExchangeList(Book book) {
    exchangeList.remove(book);
  }

  /**
   * Делает все книги для передачи публичными
   * <p>
   * НЕ СОХРАНЯТЬ ЮЗЕРА ПОСЛЕ ИСПОЛЬЗОВАНИЯ ЭТОГО МЕТОДА!
   */
  public void switchExchangeListToPublic() {
    exchangeList = exchangeList.stream()
        .filter(b -> b.getPublicityStatus() == PublicityStatus.Public).collect(
            Collectors.toList());
  }

  /**
   * Возвращает имя (ФИО) пользователя
   *
   * @return имя (ФИО) пользователя
   */
  public String getName() {
    return name;
  }

  /**
   * Устанавливает имя (ФИО) пользователя
   *
   * @param name имя (ФИО) пользователя
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Возвращает id пользователя
   *
   * @return id пользователя
   */
  public Long getId() {
    return id;
  }

  /**
   * Устанавливает id пользователя
   *
   * @param id id пользователя
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Возвращает список книг для передачи
   *
   * @return список книг для передачи
   */
  @JsonIgnore
  public List<Book> getExchangeList() {
    return exchangeList;
  }

  /**
   * Возвращает список id книги для передачи
   *
   * @return список id книги для передачи
   */
  public List<Long> getExchangeListIds() {
    return exchangeList.stream().map(Book::getId).collect(Collectors.toList());
  }

  /**
   * Возвращает список избранных книг пользователя
   *
   * @return список избранных книг пользователя
   */
  @JsonIgnore
  public List<BookBase> getWishList() {
    return wishList;
  }

  /**
   * Устанавливает список избранных книг пользователя
   *
   * @param wishList список избранных книг пользователя
   */
  public void setWishList(List<BookBase> wishList) {
    this.wishList = wishList;
  }

  /**
   * Возвращает список id избранных книг пользователя
   *
   * @return список id избранных книг пользователя
   */
  public List<Long> getWishListIds() {
    return wishList.stream().map(BookBase::getId).collect(Collectors.toList());
  }

  /**
   * Возвращает юзернейм (логин) пользователя
   *
   * @return юзернейм (логин) пользователя
   */
  public String getUsername() {
    return username;
  }

  /**
   * Устанавливает юзернейм (логин) пользователя
   *
   * @param username юзернейм (логин) пользователя
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Проверяет, не истекло ли время существования пользователя (всегда
   * возвращает true)
   *
   * @return true, если не истекло, иначе false
   */
  @JsonIgnore
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * Проверяет, не заблокирован ли пользователь
   *
   * @return true, если заблокирован, иначе false
   */
  @JsonIgnore
  @Override
  public boolean isAccountNonLocked() {
    return !isBlocked;
  }

  /**
   * Проверяет, не истекло ли время существования прав пользователя
   *
   * @return true, если заблокирован, иначе false
   */
  @JsonIgnore
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * Возвращает, может ли пользователь делать запросы
   *
   * @return true, если может, иначе false
   */
  @JsonIgnore
  @Override
  public boolean isEnabled() {
    return isAccountNonLocked();
  }

  /**
   * Возвращает права(роль) пользователя
   *
   * @return права(роль) пользователя
   */
  @JsonIgnore
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> list = new ArrayList<>();

    list.add(new SimpleGrantedAuthority(ROLE_PREFIX + role));

    return list;
  }

  /**
   * Возвращает пароль пользователя
   *
   * @return пароль пользователя
   */
  @JsonIgnore
  public String getPassword() {
    return password;
  }

  /**
   * Устанавливает пароль пользователя
   *
   * @param password пароль пользователя
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Возвращает роль пользователя
   *
   * @return роль пользователя
   */
  public Role getRole() {
    return role;
  }

  /**
   * Устанавливает роль пользователя
   *
   * @param role роль пользователя
   */
  public void setRole(Role role) {
    this.role = role;
  }

  /**
   * Возвращает запросы на бодавлнеи книг (bookBase) пользователя
   *
   * @return запросы на бодавлнеи книг (bookBase) пользователя
   */
  public List<Long> getBookBaseAddRequestIds() {
    return bookBaseAddRequests.stream().map(BookBaseRequest::getId)
        .collect(Collectors.toList());
  }

  /**
   * Возвращает список id жалоб пользователя
   *
   * @return список id жалоб пользователя
   */
  public List<Long> getComplaintsIds() {
    return complaintsList.stream().map(Complaint::getId)
        .collect(Collectors.toList());
  }

  /**
   * Возвращает аватары пользователя
   *
   * @return аватары пользователя
   */
  @JsonIgnore
  public Avatar getAvatar() {
    return avatar;
  }

  /**
   * Возвращает id аватара пользователя
   *
   * @return id аватара пользователя
   */
  public Long getAvatarId() {
    if (avatar == null) {
      return null;
    }
    return avatar.getId();
  }

  /**
   * Устанавливает аватар пользователя
   *
   * @param avatar аватар пользователя
   */
  public void setAvatar(Avatar avatar) {
    this.avatar = avatar;
  }

  /**
   * Устанавливает список жадоб пользователя
   *
   * @param complaintsList список жадоб пользователя
   */
  public void setComplaintsList(
      List<Complaint> complaintsList) {
    this.complaintsList = complaintsList;
  }

  /**
   * Устанавливает запросы на бодавлнеи книг (bookBase) пользователя
   *
   * @param requestsList запросы на бодавлнеи книг (bookBase) пользователя
   */
  public void setBookBaseAddRequests(
      List<BookBaseRequest> requestsList) {
    this.bookBaseAddRequests = requestsList;
  }

  /**
   * Устанавливает список книг для передачи
   *
   * @param exchangeList список книг для передачи
   */
  public void setExchangeList(List<Book> exchangeList) {
    this.exchangeList = exchangeList;
  }

  /**
   * Возвращает id запросов на передачу книги, в которых пользователь отдает
   * книгу
   *
   * @return id запросов на передачу книги, в которых пользователь отдает книгу
   */
  public List<Long> getOutcomingBookExchangeRequestIds() {
    return giveBookExchangeRequests.stream()
        .map(BookExchangeRequest::getId)
        .collect(Collectors.toList());
  }

  /**
   * Устанавливает список запросов на передачу книги, в которых пользователь
   * отдает
   *
   * @param giveBookExchangeRequests список запросов на передачу книги, в
   *                                 которых пользователь отдает книгу
   */
  public void setGiveBookExchangeRequests(
      List<BookExchangeRequest> giveBookExchangeRequests) {
    this.giveBookExchangeRequests = giveBookExchangeRequests;
  }

  /**
   * Добавляет запрос в список запросов на передачу книги, в которых
   * пользователь отдает
   *
   * @param request запрос на передачу книги
   */
  public void addOutcomingBookExchangeRequest(BookExchangeRequest request) {
    giveBookExchangeRequests.add(request);
  }

  /**
   * Возвращает id запросов на передачу книги, в которых пользователь получает
   * книгу
   *
   * @return id запросов на передачу книги, в которых пользователь получает
   * книгу
   */
  public List<Long> getIncomingBookExchangeRequestIds() {
    return getBookExchangeRequests.stream().map(BookExchangeRequest::getId)
        .collect(Collectors.toList());
  }

  /**
   * Устанавливает список запросов на передачу книги, в которых пользователь
   * получает книгу
   *
   * @param getBookExchangeRequests список запрсовов на передачу книги
   */
  public void setGetBookExchangeRequests(
      List<BookExchangeRequest> getBookExchangeRequests) {
    this.getBookExchangeRequests = getBookExchangeRequests;
  }

  /**
   * Добавляет запрос в список запросов на передачу книги, в которых
   * пользователь получает книгу
   *
   * @param request запрос на передачу книги
   */
  public void addIncomingBookExchangeRequest(BookExchangeRequest request) {
    getBookExchangeRequests.add(request);
  }

  /**
   * Возвращает, заблокирован ли пользователь
   *
   * @return true, если заблокирован, иначе false
   */
  public boolean isBlocked() {
    return isBlocked;
  }

  /**
   * Возвращает, заблокирован ли пользователь
   *
   * @param blocked заблокирован ли пользователь
   */
  public void setBlocked(boolean blocked) {
    isBlocked = blocked;
  }

  /**
   * Формирует строку с информацией о пользователе
   *
   * @return строку с информацией о пользователе
   */
  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'';
  }

  @JsonIgnore
  public Town getTown() {
    return town;
  }

  public Long getTownId() {
    if (townId != null) {
      return townId;
    }
    if (town == null) {
      return null;
    }
    return town.getId();
  }

  public void setTown(Town town) {
    this.town = town;
  }

  public void setTownId(Long townId) {
    this.townId = townId;
  }

  /**
   * Роль пользователя
   */
  public enum Role {
    None(0), User(1), Moderator(2), Admin(3);
    private final int accessLevel;

    Role(int level) {
      accessLevel = level;
    }

    public boolean hasUserPermits() {
      return accessLevel >= User.accessLevel;
    }

    public boolean hasModeratorPermits() {
      return accessLevel >= Moderator.accessLevel;
    }

    public boolean hasAdminPermits() {
      return accessLevel >= Admin.accessLevel;
    }
  }
}
