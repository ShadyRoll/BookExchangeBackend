package ru.hse.BookExchange.security;


import static ru.hse.BookExchange.security.SecurityConstants.SIGN_UP_URL;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Класс конфигурации
 */
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

  // Репозитория пользователей
  private final UserDetailsServiceImpl userDetailsService;
  // Кодировщик паролей
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public WebSecurity(UserDetailsServiceImpl userDetailsService,
      BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.userDetailsService = userDetailsService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  /**
   * Конфигурирует доступ к эндпоинтам
   *
   * @param http http конфигурация безопасности
   * @throws Exception ошибка при конфигурации
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable().formLogin().loginProcessingUrl("/admin")
        .defaultSuccessUrl("/admin").and().authorizeRequests()
        .antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
        .antMatchers("/v3/api-docs").permitAll()
        .antMatchers("/v2/api-docs").permitAll()
        .antMatchers("/town/**").permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilter(new JWTAuthenticationFilter(authenticationManager(),
            userDetailsService))
        .addFilter(new JWTAuthorizationFilter(authenticationManager(),
            userDetailsService));
    // this disables session creation on Spring Security
    //       .sessionManagement()
    //       .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  /**
   * Конфигурация аутентификации
   *
   * @param auth билдер аутентификации
   * @throws Exception ошибка при конфигурации
   */
  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService)
        .passwordEncoder(bCryptPasswordEncoder);
  }

  /**
   * Конфигурация cords
   *
   * @return сурс конфигурации cords
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**",
        new CorsConfiguration().applyPermitDefaultValues());
    return source;
  }
}