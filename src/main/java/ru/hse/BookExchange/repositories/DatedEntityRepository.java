package ru.hse.BookExchange.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hse.BookExchange.models.abstractions.DatedEntity;

/**
 * Репозиторий записей с датой создания
 */
public interface DatedEntityRepository<T extends DatedEntity> extends
    JpaRepository<T, Long> {

}
