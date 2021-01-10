package ru.hse.BookExchange.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.DatedEntityController;
import ru.hse.BookExchange.models.Town;
import ru.hse.BookExchange.repositories.TownRepository;

/**
 * Контроллер городов
 */
@RestController
@RequestMapping("town")
public class TownController extends DatedEntityController<Town> {

  TownController(TownRepository repository) {
    super(repository);
  }

}


