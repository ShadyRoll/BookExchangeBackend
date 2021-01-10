package ru.hse.BookExchange.repositories;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.BookBaseRequest;
import ru.hse.BookExchange.models.Genre;
import ru.hse.BookExchange.models.User;

/**
 * Репозиторий книг (bookBase)
 */
@Repository
@Transactional
public interface BookBaseRepository extends DatedEntityRepository<BookBase> {

  // Очки, набранные при поиске
  Map<List<String>, Integer> scores = new HashMap<>();

  /**
   * Возвращает все книги (bookBase), игнорируя BookBaseRequest
   *
   * @return все книги
   */
  default List<BookBase> findAllBookBases() {
    return findAll().stream()
        .filter(bookBase -> !(bookBase instanceof BookBaseRequest)).collect(
            Collectors.toList());
  }

  default List<BookBase> findByGenre(List<Long> genreIds) {
    return findAllBookBases().stream()
        .filter((bookBase -> bookBase.getGenreIds().containsAll(genreIds)))
        .collect(
            Collectors.toList());
  }

  /**
   * Получает список рекомендованных книг для пользователя
   *
   * @param user - пользователь
   * @return список рекомендованных книг
   */
  default List<BookBase> getRecommendations(User user) {
    List<BookBase> bookBases = new ArrayList<>();

    List<Genre> genres = new ArrayList<>();
    for (BookBase base : user.getWishList()) {
      genres.addAll(base.getGenres());
    }
    genres = genres.stream().distinct().collect(Collectors.toList());

    for (Genre genre : genres) {
      var a = findByGenre(List.of(genre.getId()));
      bookBases.addAll(findByGenre(List.of(genre.getId())));
    }

    for (BookBase base : findAllBookBases()) {
      if (user.getWishList().stream()
          .anyMatch(
              (wishedBook) -> base.getAuthor()
                  .equals(wishedBook.getAuthor()))) {
        bookBases.add(base);
      }
    }

    // Перемешиваем книги
    Collections.shuffle(bookBases,
        new Random(ChronoUnit.DAYS.between(LocalDate.of(2000, 1, 1),
            LocalDate.now())));
    // Удаляем повторы
    return bookBases.stream().distinct().collect(Collectors.toList());
  }

  /**
   * Ищет кники (bookBase) по совпадению названия
   *
   * @param searchText - строка поиска
   * @return список наиболее подходящих книг
   */
  default List<BookBase> searchByTitle(String searchText, int limit,
      int skip) {
    // Ключевые слова
    String[] searchWords = getSearchWords(searchText);

    return search(Comparator.comparingInt(
        bookBase ->
            // Ищем по названию книги
            -getSearchScore(List.of(bookBase.getTitle()),
                searchWords)), limit, skip);
  }

  /**
   * Ищет кники (bookBase) по совпадению автора
   *
   * @param searchText - строка поиска
   * @return список наиболее подходящих книг
   */
  default List<BookBase> searchByAuthor(String searchText, int limit,
      int skip) {
    // Ключевые слова
    String[] searchWords = getSearchWords(searchText);

    return search(Comparator.comparingInt(
        bookBase ->
            // Ищем по автору книги
            -getSearchScore(List.of(bookBase.getAuthor()),
                searchWords)), limit, skip);
  }

  /**
   * Ищет кники (bookBase) по совпадению автора и/или названия
   *
   * @param searchText - строка поиска
   * @return список наиболее подходящих книг
   */
  default List<BookBase> searchByText(String searchText, int limit, int skip) {
    // Ключевые слова
    String[] searchWords = getSearchWords(searchText);

    return search(Comparator.comparingInt(
        bookBase ->
            // Ищем в названии и авторе книги
            -getSearchScore(List.of(bookBase.getAuthor(), bookBase.getTitle()),
                searchWords)), limit, skip);
  }

  private List<BookBase> search(Comparator<BookBase> comparator,
      int limit, int skip) {
    // Сортируем книги по набранным при поиске очкам
    List<BookBase> res = findAllBookBases().stream().sorted(comparator)
        .skip(skip).limit(limit).collect(Collectors.toList());
    // Очищаем кешированные очки после поиска
    scores.clear();
    return res;
  }

  /**
   * Определяет релевантность книги (BookBase) при поиске
   *
   * @param strings     - строки с которой проверяем совпадения
   * @param searchWords - ключевые слова для поиска
   * @return количество набранных очков
   */
  private int getSearchScore(List<String> strings,
      String[] searchWords) {

    if (scores.containsKey(strings)) {
      return scores.get(strings);
    }
    int score = 0;
    for (String word : searchWords) {
      /* Ищем совпадения с каждой сторокой из strings */
      for (String str : strings) {
        int best = 0;
        /* Ищем наилучшее совпадения среди слов в str */
        for (String strWord : str.split("\\s")) {
          best = Math.max(best, compareStrings(strWord, word));
        }
        // Добавляем к очкам наилучшее совпадение
        score += best;
      }
    }
    scores.put(strings, score);
    return score;
  }


  // Сравнивает строки на схожесть
  private int compareStrings(String where, String what) {
    String lowerWhere = where.toLowerCase();
    int maxSize = Math.max(what.length(), lowerWhere.length());
    // Счет = максимальное расстояние Левенштейна - фактическое
    return (maxSize - getDamerauLevenshteinDistance(lowerWhere, what));
  }

  /**
   * Определяет модифицированное расстояние Дамерау-Левенштейна адаптированным
   * алгоритмом Вагнера-Фишера
   *
   * <p>
   * Модифицированность означает следующее: Если обнаружено совпадения
   * нескольких символов подряд - расстояние будет не сохранено тем же, а
   * уменьшено. Например, расстояние будет меньше, если одна из строк вложена в
   * другую, нежели если она будет являться только подстрокой.
   * </p>
   *
   * @param s - 1я строка
   * @param t - 2я строка
   * @return расстояние Левенштейна
   */
  private int getDamerauLevenshteinDistance(String s, String t) {

    // Размеры строк
    int sLen = s.length(), tLen = t.length();
    // Массив количества шагов на каждом этапе обработки
    int[][] costs = new int[sLen + 1][tLen + 1];
    // Заполняем его нулями
    for (int i = 0; i <= sLen; ++i) {
      for (int j = 0; j <= tLen; ++j) {
        costs[i][j] = 0;
      }
    }
    // Заполняем случаи, когда одна из строк пустая
    for (int i = 0; i <= sLen; ++i) {
      costs[i][0] = i;
    }
    for (int j = 1; j <= tLen; ++j) {
      costs[0][j] = j;
    }
    // Количество шагов точки пути, используемой при смене символов местами
    int switchVal;
    // Теперь из [1, 1] идем в [sLen, tLen]
    for (int i = 1; i <= sLen; ++i) {
      for (int j = 1; j <= tLen; ++j) {
        // Если символы равны, дополнительных действий делать не надо
        if (s.charAt(i - 1) == t.charAt(j - 1)) {
          costs[i][j] = costs[i - 1][j - 1];
          /* Если и на прошлой шаге было совпадение,
             уменьшаем расстояние (чтобы увеличить очки)  */
          if (i - 2 >= 0 && j - 2 >= 0 && s.charAt(i - 2) == t.charAt(j - 2)) {
            costs[i][j]--;
          }
        } else {
          /* Если имеет смысл менять символы местами, добавим соответствующую
           * точку пути к прочим вариантам продолжения пути  */
          if (i > 1 && j > 1 && s.charAt(i - 1) == t.charAt(j - 2)
              && s.charAt(i - 2) == t.charAt(j - 1)) {
            switchVal = costs[i - 2][j - 2];
          } else {
            /* Иначе присвоим switchVal максимальное значение,
               чтобы мы его точно не выбрали */
            switchVal = Integer.MAX_VALUE;
          }
          /* Выберем путь, где мы сделали меньше всего действий,
             и продолжим от него */
          costs[i][j] = 1 + Math.min(costs[i - 1][j],
              Math.min(costs[i][j - 1],
                  Math.min(costs[i - 1][j - 1], switchVal)));
        }
      }
    }
    return costs[sLen][tLen];
  }

  private String[] getSearchWords(String searchText) {
    return searchText.toLowerCase().split("\\s");
  }

}


