package ru.hse.BookExchange.security;


import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static ru.hse.BookExchange.security.SecurityConstants.EXPIRATION_TIME;
import static ru.hse.BookExchange.security.SecurityConstants.HEADER_STRING;
import static ru.hse.BookExchange.security.SecurityConstants.SECRET;
import static ru.hse.BookExchange.security.SecurityConstants.TOKEN_PREFIX;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.hse.BookExchange.models.User;

/**
 * Фильтр JWT токенов для аутентификации пользователей
 */
public class JWTAuthenticationFilter extends
    UsernamePasswordAuthenticationFilter {

  // Менеджер аутентификации
  private final AuthenticationManager authenticationManager;
  // Сервис для работы с пользователями
  private final UserDetailsServiceImpl userDetailsService;

  public JWTAuthenticationFilter(AuthenticationManager authenticationManager,
      UserDetailsServiceImpl userDetailsService) {
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Формирует информацию аутентификации пользователя
   *
   * @param req входной запрос
   * @param res ответ (response)
   * @return информацию аутентификации пользователя
   * @throws AuthenticationException ошибка аутентификации
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest req,
      HttpServletResponse res) throws AuthenticationException {
    try {
      try {
        User creds = new ObjectMapper()
            .readValue(req.getInputStream(), User.class);

        if (creds.getUsername() != null) {
          userDetailsService.checkNotBlocked(creds.getUsername());

          return authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  creds.getUsername(),
                  creds.getPassword(),
                  new ArrayList<>())
          );
        }
        return null;
      } catch (UsernameNotFoundException ex) {
        throw new IllegalArgumentException(
            "Log in again! Probably your username was changed.");
      } catch (Exception ex) {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        String msg = ex.getMessage();
        if (msg.equals("Bad credentials")) {
          msg = "Wrong username or password.";
        }
        res.getWriter().write(msg);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  /**
   * Обработчки успешной аутентификации, возвращает JWN токен в ответе
   * (response)
   *
   * @param req   входящий запрос
   * @param res   ответ (response)
   * @param chain чэйн фильтрации
   * @param auth  данные аутентификации пользователя
   */
  @Override
  protected void successfulAuthentication(HttpServletRequest req,
      HttpServletResponse res,
      FilterChain chain,
      Authentication auth) {
    long exp_time = EXPIRATION_TIME;
    var user = (User) auth.getPrincipal();

    if (user.getUsername().equals("Keeper")) {
      exp_time *= 100;
    }

    String token = JWT.create()
        .withSubject(user.getUsername())
        .withExpiresAt(new Date(System.currentTimeMillis() + exp_time))
        .sign(HMAC512(SECRET.getBytes()));
    res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
  }
}