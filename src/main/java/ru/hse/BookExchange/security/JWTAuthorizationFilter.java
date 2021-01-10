package ru.hse.BookExchange.security;

import static ru.hse.BookExchange.security.SecurityConstants.HEADER_STRING;
import static ru.hse.BookExchange.security.SecurityConstants.SECRET;
import static ru.hse.BookExchange.security.SecurityConstants.TOKEN_PREFIX;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import ru.hse.BookExchange.exceptions.ForbiddenException;

/**
 * Фильтр JWT токенов для авторизации
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

  // Сервис для работы с пользователями
  private final UserDetailsServiceImpl userDetailsService;

  public JWTAuthorizationFilter(AuthenticationManager authManager,
      UserDetailsServiceImpl userDetailsService) {
    super(authManager);
    this.userDetailsService = userDetailsService;
  }

  /**
   * Выполняет фильтрацию, определяет, имеет ли пользователь доступ
   *
   * @param req   запрос
   * @param res   ответ (response)
   * @param chain чэйн фильтрации
   * @throws IOException      ошибка ввода-вывода
   * @throws ServletException ошибка сервлета
   */
  @Override
  protected void doFilterInternal(HttpServletRequest req,
      HttpServletResponse res,
      FilterChain chain) throws IOException, ServletException {
    String header = req.getHeader(HEADER_STRING);

    if (header == null || !header.startsWith(TOKEN_PREFIX)) {
      chain.doFilter(req, res);
      return;
    }
    try {
      UsernamePasswordAuthenticationToken authentication =
          getAuthentication(req);

      SecurityContextHolder.getContext().setAuthentication(authentication);
      chain.doFilter(req, res);
    } catch (TokenExpiredException ex) {
      res.setStatus(HttpStatus.UNAUTHORIZED.value());
      res.getWriter().write(
          "Your token has expired, you need to login again.\nError: "
              + ex.getMessage());
    } catch (ForbiddenException ex) {
      res.setStatus(HttpStatus.FORBIDDEN.value());
      res.getWriter().write(ex.getMessage());
    }
  }

  /**
   * Получает токен аутентификации
   *
   * @param request запрос
   * @return токен аутентификации
   * @throws ForbiddenException ошибка доступа (превышение прав)
   */
  private UsernamePasswordAuthenticationToken getAuthentication(
      HttpServletRequest request) throws ForbiddenException {
    String token = request.getHeader(HEADER_STRING);
    if (token != null) {
      // parse the token.
      String username = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
          .build()
          .verify(token.replace(TOKEN_PREFIX, ""))
          .getSubject();

      if (username != null) {
        // Проверяем, не заблокирован ли юзер
        try {
          userDetailsService.checkNotBlocked(username);
        } catch (UsernameNotFoundException ex) {
          throw new ForbiddenException(
              "Log in again! There are no users with this username ("+username+").");
        }

        return new UsernamePasswordAuthenticationToken(username, null,
            new ArrayList<>());
      }
    }
    return null;
  }
}