package ru.hse.BookExchange.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Сервис, отвечающий за подготовку ответов на запросы, в которых произошло
 * исключение
 */
@ControllerAdvice
public class ExceptionAdvice {

  /**
   * Обработчик отсутсвия записи в бд
   *
   * @param ex исключение
   * @return тело ответа
   */
  @ResponseBody
  @ExceptionHandler({EntityNotFoundException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String entityNotFoundHandler(RuntimeException ex) {
    return ex.getMessage();
  }

  /**
   * Обработчик пустого(null) id
   *
   * @param ex исключение
   * @return тело ответа
   */
  @ResponseBody
  @ExceptionHandler(NullIdException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  String nullIdHandler(NullIdException ex) {
    return ex.getMessage();
  }

  /**
   * Обработчик попытки превышения уровня доступа
   *
   * @param ex исключение
   * @return тело ответа
   */
  @ResponseBody
  @ExceptionHandler({ForbiddenException.class, LockedException.class})
  @ResponseStatus(HttpStatus.FORBIDDEN)
  String forbiddenHandler(ForbiddenException ex) {
    return ex.getMessage();
  }

  /**
   * Обработчик неверного состояния статуса запроса
   *
   * @param ex исключение
   * @return тело ответа
   */
  @ResponseBody
  @ExceptionHandler(IllegalRequestStatusState.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  String illegalRequestStatusStateHandler(IllegalRequestStatusState ex) {
    return ex.getMessage();
  }

  /**
   * Обработчик неподдерживаемой операции
   *
   * @param ex исключение
   * @return тело ответа
   */
  @ResponseBody
  @ExceptionHandler(UnsupportedOperationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  String UnsupportedOperationExceptionHandler(
      UnsupportedOperationException ex) {
    return ex.getMessage();
  }

  /**
   * Обработчик нелегального аргумента
   *
   * @param ex исключение
   * @return тело ответа
   */
  @ResponseBody
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  String illegalArgumentExceptionHandler(
      IllegalArgumentException ex) {
    return ex.getMessage();
  }
}
