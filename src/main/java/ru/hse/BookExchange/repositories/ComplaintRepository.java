package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.Complaint;

/**
 * Репозиторий жалоб пользователь
 */
@Repository
@Transactional
public interface ComplaintRepository extends DatedEntityRepository<Complaint> {

}