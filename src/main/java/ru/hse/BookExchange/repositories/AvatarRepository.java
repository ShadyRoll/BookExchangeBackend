package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.Avatar;

/**
 * Репозиторий аватаров пользователей
 */
@Repository
@Transactional
public interface AvatarRepository extends DatedEntityRepository<Avatar> {

}


