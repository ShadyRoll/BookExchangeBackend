package ru.hse.BookExchange.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.DatedEntityController;
import ru.hse.BookExchange.models.Genre;
import ru.hse.BookExchange.repositories.GenreRepository;

/**
 * Контроллер жанров книги
 */
@RestController
@RequestMapping("genre")
public class GenreController extends DatedEntityController<Genre> {

  GenreController(GenreRepository repository) {
    super(repository);
  }
}


