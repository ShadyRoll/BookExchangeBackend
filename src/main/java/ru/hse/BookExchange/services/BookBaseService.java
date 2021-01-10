package ru.hse.BookExchange.services;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import ru.hse.BookExchange.controllers.AuthenticationController;
import ru.hse.BookExchange.controllers.BookBaseController.SortType;
import ru.hse.BookExchange.controllers.abstractions.DatedEntityController;
import ru.hse.BookExchange.exceptions.BookBaseNotFoundException;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.models.Book.PublicityStatus;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.repositories.BookBaseRepository;

@Service
public class BookBaseService {

  private final Map<Long, List<BookBase>> recommendationMap = new HashMap<>();
  private final BookBaseRepository bookBaseRepository;
  private final AuthenticationController auth;

  public BookBaseService(
      BookBaseRepository bookBaseRepository,
      AuthenticationController auth) {
    this.bookBaseRepository = bookBaseRepository;
    this.auth = auth;
  }


  public List<BookBase> all(
      @RequestParam(required = false, defaultValue = "0") int skip,
      @RequestParam(required = false, defaultValue = "100") int limit,
      @RequestParam(required = false, defaultValue = "none") String sortBy,
      @RequestParam(required = false, defaultValue = "true") boolean ascending,
      @RequestParam(required = false) List<Long> ids,

      @Deprecated
      @RequestParam(required = false, defaultValue = "false") boolean latest,
      @Deprecated
      @RequestParam(required = false, defaultValue = "false") boolean recommended,
      Authentication authentication) {
    if (ids != null) {
      User user = auth.isUser(authentication);
      //List<BookBase> res = bookBaseRepository.findAllById(ids);
      List<BookBase> res = new ArrayList<>();
      // Чтобы гарантировать ту же последовательность id приходится собирать вручную
      for (int i = 0; i < ids.size(); i++) {
        final int finalI = i;
        res.add(bookBaseRepository.findById(ids.get(i))
            .orElseThrow(() -> new BookBaseNotFoundException(ids.get(finalI))));
        hidePrivateBooks(res.get(res.size() - 1), user);
      }
      return res;
    }

    DatedEntityController.checkSkipAndLimit(skip, limit);
    if (recommended && latest) {
      throw new IllegalArgumentException(
          "You cannot use deprecated latest and recommended flags"
              + " at the same time. Moreover, use sortBy argument instead them!");
    }

    var stream = bookBaseRepository.findAllBookBases().stream();

    SortType sortType;
    try {
      sortType = SortType.valueOf(sortBy);
    } catch (IllegalArgumentException ex) { // Неверный тип сортировки
      List<String> sortByValues = EnumUtils.getEnumList(SortType.class)
          .stream()
          .map(Enum::toString).collect(Collectors.toList());

      throw new IllegalArgumentException("Wrong sortBy argument value. "
          + "Possible values: " + String.join(", ", sortByValues) + '.');
    }

    if (sortType != SortType.none && (latest || recommended)) {
      throw new IllegalArgumentException(
          "You cannot use deprecated latest and recommended flags"
              + " and sortBy argument at the same time. Use sortBy!");
    }

    if (sortType == SortType.date || latest) {
      stream = stream.sorted(Collections.reverseOrder());
    } else if (sortType == SortType.recommended || recommended) {
      if (authentication == null) {
        throw new ForbiddenException(
            "You must specify authentication token to access recommendations.");
      }
      var curRecommendations = getRecommendations(authentication, skip, limit);
      stream = curRecommendations.stream();
    } else if (sortType == SortType.rate) {
      stream = stream.sorted(Comparator.comparingDouble(BookBase::getRating));
    }

    // Разворачиваем массив, если порядок обратный
    if (!ascending || SortType.valueOf(sortBy) == SortType.rate) {
      var temp = stream.collect(Collectors.toList());
      Collections.reverse(temp);
      stream = temp.stream();
    }

    User user = auth.isUser(authentication);
    List<BookBase> res = stream.skip(skip).limit(limit)
        .collect(Collectors.toList());

    // Прячем приватные книги
    res.forEach((b) -> hidePrivateBooks(b, user));

    return res;
  }

  public List<BookBase> getRecommendations(Authentication authentication,
      int skip, int limit) {
    Long userId = auth.isUser(authentication).getId();

    if (recommendationMap.containsKey(userId) && skip != 0) {
      return recommendationMap.get(userId);
    }

    var newRecommendations = generateRecommendations(authentication, skip,
        limit);
    recommendationMap.put(userId, newRecommendations);
    return newRecommendations;
  }

  private List<BookBase> generateRecommendations(Authentication
      authentication,
      int skip, int limit) {
    User user = auth.isUser(authentication);
    var curRecommendations = bookBaseRepository.getRecommendations(user);

    // Если рекомендаций слишком мало - добавил книги с самым большим рейтингом
    if (curRecommendations.size() < 30) {
      List<BookBase> ratedBookBases = all(0, Integer.MAX_VALUE,
          "rate", false, null, false, false, authentication);
      // Добавляем по 30 книг, пока не наберется нужное количетсво
      for (int i = 0; i < ratedBookBases.size(); i += 30) {
        curRecommendations
            .addAll(ratedBookBases.stream().skip(i).limit(30).collect(
                Collectors.toList()));
        // Убираем повторы
        curRecommendations = curRecommendations.stream().distinct().collect(
            Collectors.toList());
        if (curRecommendations.size() >= limit + skip) {
          break;
        }
      }

    }
    return curRecommendations;
  }


  public void hidePrivateBooks(BookBase bookBase, User user) {
    bookBase.getBooks().removeIf(
        book -> book.getPublicityStatus() == PublicityStatus.Private
            && book.getOwner() != user && !user.getRole()
            .hasModeratorPermits());
  }
}