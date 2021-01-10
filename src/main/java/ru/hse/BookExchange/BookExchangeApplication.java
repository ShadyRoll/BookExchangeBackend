package ru.hse.BookExchange;

import java.util.Date;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Главный класс, содержащий точку входа
 */
@SpringBootApplication
public class BookExchangeApplication {

  /**
   * Точка входа в программу
   *
   * @param args аргументы запуска
   */
  public static void main(String[] args) {
    SpringApplication.run(BookExchangeApplication.class, args);
  }

  /**
   * Инициализатор. Устанавливает часовой пояс
   */
  @PostConstruct
  public void init() {
    // Установим часовой пояс (Москва)
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+3")); // MSK(GMT+3)
    System.out.println(
        "Spring boot application running Moscow(GMT+3) timezone. Now :"
            + new Date());
  }


  /**
   * Бин для шифрования паролей
   *
   * @return шифровщик паролей
   */
  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
