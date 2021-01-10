package ru.hse.BookExchange.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hse.BookExchange.controllers.abstractions.DatedEntityController;
import ru.hse.BookExchange.models.ExteriorQuality;
import ru.hse.BookExchange.repositories.ExteriorQualityRepository;

/**
 * Контроллер уровня поношенности книги
 */
@RestController
@RequestMapping("exteriorQuality")
public class ExteriorQualityController extends
    DatedEntityController<ExteriorQuality> {

  ExteriorQualityController(ExteriorQualityRepository repository) {
    super(repository);
  }


}


