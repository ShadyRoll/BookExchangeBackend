package ru.hse.BookExchange.security;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.hse.BookExchange.exceptions.ForbiddenException;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Сервис для работы с пользователями
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  // Репозитория пользователей
  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Загружает пользователя по юзернейму
   *
   * @param username юзернейм
   * @return пользователя
   * @throws UsernameNotFoundException если юзернейм не найден
   * @throws ForbiddenException        ошибка доступа
   */
  @Override
  public UserDetails loadUserByUsername(String username)
      throws UsernameNotFoundException, ForbiddenException {
    ru.hse.BookExchange.models.User applicationUser = userRepository
        .findByUsername(username);

    if (applicationUser == null) {
      throw new UsernameNotFoundException(username);
    }
    if (applicationUser.isBlocked()) {
      throw new LockedException("Your account is blocked");
    }
    return applicationUser;
  }

  public void checkNotBlocked(String username) {
    loadUserByUsername(username);
  }
}