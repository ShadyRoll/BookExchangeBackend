package ru.hse.BookExchange.repositories;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.Dialog;

/**
 * Репозиторий диалогов (чат) пользователей
 */
@Repository
@Transactional
public interface DialogRepository extends DatedEntityRepository<Dialog> {

}


