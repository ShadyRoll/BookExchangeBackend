package ru.hse.BookExchange.models.abstractions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import ru.hse.BookExchange.models.Complaint;


/**
 * Запись, имеющая дату создания
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DatedEntity implements Comparable<DatedEntity> {

  // Id записи
  @Id
  @GeneratedValue
  private final Long id;

  // Дата создания
  @Temporal(TemporalType.TIMESTAMP)
  protected Date creationDate;

  // Жалобы
  @JsonIgnore
  @OneToMany(mappedBy = "connectedEntity")
  private List<Complaint> complaints = new ArrayList<>();

  public DatedEntity() {
    id = -1L;
    // Дату создания задаем текущим временем
    this.creationDate = new Date();
  }

  /**
   * Сравнивает записи по дате создания
   *
   * @param other другая запись
   * @return 0, если даты равны; >0, если 2я запись новее; иначе <0
   */
  public int compareTo(DatedEntity other) {
    return creationDate.compareTo(other.creationDate);
  }

  /**
   * Возвращает дату создания
   *
   * @return дата создания
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Устанавливает дату создания
   *
   * @param creationDate дата создания
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Возвращает список жалоб
   *
   * @return список жалоб
   */
  @JsonIgnore
  public List<Complaint> getComplaints() {
    return complaints;
  }

  /**
   * Устанавливает список жалоб
   *
   * @param complaints список жалоб
   */
  public void setComplaints(
      List<Complaint> complaints) {
    this.complaints = complaints;
  }

  /**
   * Возвращает список id жалоб
   *
   * @return список id жалоб
   */
  @JsonIgnore
  public List<Long> getComplaintIds() {
    return complaints.stream().map(Complaint::getId)
        .collect(Collectors.toList());
  }

  /**
   * Возвращает id
   *
   * @return id
   */
  public abstract Long getId();

  /**
   * Устанавливает id
   *
   * @param id id
   */
  public abstract void setId(Long id);
}
