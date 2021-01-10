package ru.hse.BookExchange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.hse.BookExchange.models.Avatar;
import ru.hse.BookExchange.models.Book;
import ru.hse.BookExchange.models.BookBase;
import ru.hse.BookExchange.models.BookBase.Language;
import ru.hse.BookExchange.models.BookBasePhoto;
import ru.hse.BookExchange.models.BookBaseRate;
import ru.hse.BookExchange.models.BookBaseRequest;
import ru.hse.BookExchange.models.BookPhoto;
import ru.hse.BookExchange.models.Complaint;
import ru.hse.BookExchange.models.ExteriorQuality;
import ru.hse.BookExchange.models.Genre;
import ru.hse.BookExchange.models.Town;
import ru.hse.BookExchange.models.User;
import ru.hse.BookExchange.models.User.Role;
import ru.hse.BookExchange.repositories.AvatarRepository;
import ru.hse.BookExchange.repositories.BookBasePhotoRepository;
import ru.hse.BookExchange.repositories.BookBaseRateRepository;
import ru.hse.BookExchange.repositories.BookBaseRepository;
import ru.hse.BookExchange.repositories.BookBaseRequestRepository;
import ru.hse.BookExchange.repositories.BookExchangeRequestRepository;
import ru.hse.BookExchange.repositories.BookPhotoRepository;
import ru.hse.BookExchange.repositories.BookRepository;
import ru.hse.BookExchange.repositories.ComplaintRepository;
import ru.hse.BookExchange.repositories.DialogRepository;
import ru.hse.BookExchange.repositories.ExteriorQualityRepository;
import ru.hse.BookExchange.repositories.GenreRepository;
import ru.hse.BookExchange.repositories.MessageRepository;
import ru.hse.BookExchange.repositories.TownRepository;
import ru.hse.BookExchange.repositories.UserRepository;

/**
 * Конфигурация с загрузчиком стартовых записей в базу данных
 */
@Configuration
public class DataBaseLoader {

  // Логгер
  private static final Logger log = LoggerFactory
      .getLogger(DataBaseLoader.class);

  /**
   * Загуржает стартовые записи в базу данных
   *
   * @param bookBaseRepository            репозиторий книг (bookBase)
   * @param bookRepository                репозиторий книг для передачи (book)
   * @param userRepository                репозиторий пользователь
   * @param exteriorQualityRepository     репозиторий уровня поношенности книги
   *                                      для передачи
   * @param genreRepository               репозиторий жанров книг
   * @param townRepository                репозиторий городов
   * @param avatarRepository              репозиторий аватаров пользователей
   * @param bookPhotoRepository           репозиторий фотографий книг для
   *                                      передачи
   * @param bookBasePhotoRepository       репозиторий книг (bookBase)
   * @param bookBaseRequestRepository     репозиторий запросов на добавление
   *                                      книг
   * @param complaintRepository           репозиторий жалоб
   * @param bookBaseRateRepository
   * @param bookExchangeRequestRepository
   * @param messageRepository
   * @param dialogRepository
   * @param bCryptPasswordEncoder
   * @return
   */
  @Bean
  CommandLineRunner initDatabase(BookBaseRepository bookBaseRepository,
      BookRepository bookRepository, UserRepository userRepository,
      ExteriorQualityRepository exteriorQualityRepository,
      GenreRepository genreRepository,
      TownRepository townRepository,
      AvatarRepository avatarRepository,
      BookPhotoRepository bookPhotoRepository,
      BookBasePhotoRepository bookBasePhotoRepository,
      BookBaseRequestRepository bookBaseRequestRepository,
      ComplaintRepository complaintRepository,
      BookBaseRateRepository bookBaseRateRepository,
      BookExchangeRequestRepository bookExchangeRequestRepository,
      MessageRepository messageRepository,
      DialogRepository dialogRepository,
      BCryptPasswordEncoder bCryptPasswordEncoder) {
    // Towns
    Town moscow = new Town("Moscow");

    // Users
    List<User> userList = new ArrayList<>() {{
      add(new User(Role.Admin, "Keeper",
          bCryptPasswordEncoder.encode("superSecret"), "Great Void Keeper",
          moscow));
      add(new User(Role.Admin, "OlegStan",
          bCryptPasswordEncoder.encode("superSecret"), "Oleg Koptev", moscow));
      add(new User(Role.Admin, "MaxIO",
          bCryptPasswordEncoder.encode("superSecret"),
          "Maxim Ionko", moscow));
      add(new User(Role.Moderator, "ModeratorJora",
          bCryptPasswordEncoder.encode("superSecret"),
          "Moderator Jora", moscow));
      add(new User(Role.User, "Ivanov", bCryptPasswordEncoder.encode("1234"),
          "Ivan Ivanov", moscow));
      add(new User(Role.User, "Petrov", bCryptPasswordEncoder.encode("1234"),
          "Petr Petrov", moscow));
    }};

    // Exterior qualities
    List<ExteriorQuality> exteriorQualityList = generateExteriorQualityList();

    // Genres
    Genre genrePoems = new Genre("Poems");
    Genre genreScienceFiction = new Genre("Science fiction");
    Genre genreAntiutopia = new Genre("Antiutopia");
    Genre genreThriller = new Genre("Thriller");
    Genre genreFantasy = new Genre("Fantasy");
    Genre genreAdventure = new Genre("Adventure");
    Genre genreMystery = new Genre("Mystery");
    Genre genreCrime = new Genre("Crime");
    Genre genreDetectiveFiction = new Genre("DetectiveFiction");
    Genre genreConspiracyFiction = new Genre("ConspiracyFiction");
    Genre genreWarNovel = new Genre("WarNovel");
    Genre genreDrama = new Genre("Drama");
    Genre genreYoungAdultFiction = new Genre("YoungAdultFiction");
    Genre genreHorror = new Genre("Horror");
    Genre genreGothicNovel = new Genre("GothicNovel");
    Genre genrePsychologicalHorror = new Genre("PsychologicalHorror");
    Genre genrePsychologicalThriller = new Genre("PsychologicalThriller");
    List<Genre> genreList = Arrays
        .asList(genrePoems, genreScienceFiction, genreAntiutopia,
            genreThriller, genreFantasy, genreAdventure, genreMystery,
            genreCrime, genreDetectiveFiction, genreConspiracyFiction,
            genreWarNovel, genreDrama, genreYoungAdultFiction, genreHorror,
            genreGothicNovel, genrePsychologicalHorror,
            genrePsychologicalThriller);

    // BookBases
    String description = "Wonderful book full of wonders!";

    List<BookBase> bookBaseList = new ArrayList<>();
    bookBaseList.add(new BookBase("George Orwell", Language.ENG,
        "1984", 336, 2008,
        Arrays.asList(genreScienceFiction, genreAntiutopia, genreThriller),
        //"https://wordery.com/jackets/00972881/1984-george-orwell-9780141036144.jpg?width=246",
        "1984 or Nineteen Eighty-Four: A Novel, is a dystopian novel by English novelist George Orwell. It was published on 8 June 1949. 1984 is one of George Orwell's most powerful politically charged novels, a beautifully crafted warning against the dangers of a totalitarian society, and one of the most famous novels in the dystopian genre. Winston Smith is a low-ranking member of the ruling party in London whose every move is monitored by telescreens."));
    bookBaseList.add(new BookBase("Ray Bradbury", Language.ENG,
        "Fahrenheit 451", 192, 1993,
        Arrays.asList(genreScienceFiction, genreAntiutopia),
        //"https://images-na.ssl-images-amazon.com/images/I/91EN22C1rbL.jpg",
        "Ray Bradbury's internationally acclaimed novel Fahrenheit 451 is a masterwork of twentieth-century literature set in a bleak, dystopian future."));
    bookBaseList.add(new BookBase("Lem Stanislaw", Language.ENG,
        "Solaris", 224, 2016, Collections.singletonList(genreScienceFiction),
        //"https://i.pinimg.com/originals/f0/6d/76/f06d762d60ea1d59c09da6dcbde0475e.jpg",
        "A scientist examining the ocean that covers the surface of the planet Solaris is forced to confront the incarnation of a painful, hitherto unconscious memory, inexplicably created by the ocean. An undisputed SF classic."));
    bookBaseList.add(new BookBase("Jules Verne", Language.ENG,
        "Journey to the Centre of the Earth", 208, 1996,
        Collections.singletonList(genreScienceFiction),
        //"https://wendyvancamp.files.wordpress.com/2016/04/journey-to-the-center-of-the-earth-book-cover.jpg",
        "\"Science, my lad, is made up of mistakes, but they are mistakes which it is useful to make, because they lead little by little to the truth.\" The father of science fiction, Jules Verne, invites you to join the intrepid and eccentric Professor Liedenbrock and his companions on a thrilling and dramatic expedition as they travel down a secret tunnel in a volcano in Iceland on a journey which will lead them to the centre of the earth. Along the way they encounter various hazards and witness many incredible sights such as the underground forest, illuminated by electricity, the Great Geyser, the battle between prehistoric monsters, the strange whispering gallery, giant insects and the vast subterranean sea with its ferocious whirlpool."));
    bookBaseList.add(new BookBase("Richard Osman", Language.ENG,
        "The Thursday Murder Club", 400, 2020,
        Collections.singletonList(genreThriller),
        //"https://images-na.ssl-images-amazon.com/images/I/81uHYq+cvkL.jpg",
        "But when a local property developer shows up dead, 'The Thursday Murder Club' find themselves in the middle of their first live case."));
    bookBaseList.add(new BookBase("J. R. R. Tolkien", Language.ENG,
        "The Fall of Gondolin", 304, 2020,
        Collections.singletonList(genreFantasy),
        //"https://cdn.shazoo.ru/252606_Yz9lbcNDVW_fall_of_gondolin.jpg",
        "In the Tale of The Fall of Gondolin are two of the greatest powers in the world. There is Morgoth of the uttermost evil, unseen in this story but ruling over a vast military power from his fortress of Angband. Deeply opposed to Morgoth is Ulmo, second in might only to Manwë, chief of the Valar: he is called the Lord of Waters, of all seas, lakes, and rivers under the sky. But he works in secret in Middle-earth to support the Noldor, the kindred of the Elves among whom were numbered Húrin and Túrin Turambar."));
    bookBaseList.add(new BookBase("Andrzej Sapkowski", Language.ENG,
        "The Last Wish", 304, 2020,
        Collections.singletonList(genreFantasy),
        //"https://www.hachettebookgroup.com/wp-content/uploads/2019/09/9780316497541-1.jpg?fit=424%2C675",
        "Geralt the Witcher -- revered and hated -- holds the line against the monsters plaguing humanity in this collection of adventures in the NYT bestselling series that inspired the blockbuster video games."));
    bookBaseList.add(new BookBase("Dan Brown", Language.ENG,
        "Angels and Demons", 496, 2006,
        Arrays.asList(genreAdventure, genreThriller, genreCrime),
        "When a world renowned scientist is found brutally murdered, a Harvard professor, Robert Langdon, is summoned to identify the mysterious symbol seared onto the dead man's chest. His conclusion: it is the work of the Illuminati, a secret brotherhood presumed extinct for nearly four hundred years - now reborn to continue their bitter vendetta against their sworn enemy, the Catholic church."
    ));

    bookBaseList.add(new BookBase("Dan Brown", Language.ENG,
        "The Lost Symbol", 626, 2009,
        Arrays.asList(genreMystery, genreThriller, genreCrime),
        "The Capitol Building, Washington DC: Harvard symbologist Robert Langdon believes he is here to give a lecture. He is wrong. Within minutes of his arrival, a shocking object is discovered. It is a gruesome invitation into an ancient world of hidden wisdom. When Langdon's mentor, Peter Solomon - prominent mason and philanthropist - is kidnapped, Langdon realizes that his only hope of saving his friend's life is to accept this mysterious summons."
    ));

    bookBaseList.add(new BookBase("Dan Brown", Language.ENG,
        "The Da Vinci Code", 597, 2009,
        Arrays.asList(genreMystery, genreThriller, genreDetectiveFiction,
            genreConspiracyFiction),
        "While in Paris, Harvard symbologist Robert Langdon is awakened by a phone call in the dead of the night. The elderly curator of the Louvre has been murdered inside the museum, his body covered in baffling symbols. As Langdon and gifted French cryptologist Sophie Neveu sort through the bizarre riddles, they are stunned to discover a trail of clues hidden in the works of Leonardo da Vinci—clues visible for all to see and yet ingeniously disguised by the painter."
    ));

    bookBaseList.add(new BookBase("Dan Brown", Language.ENG,
        "Origin", 466, 2017,
        Arrays.asList(genreMystery, genreThriller, genreCrime),
        "Robert Langdon, Harvard professor of symbology and religious iconology, arrives at the Guggenheim Museum Bilbao to attend the unveiling of an astonishing scientific breakthrough. The evening's host is billionaire Edmond Kirsch, a futurist whose dazzling high-tech inventions and audacious predictions have made him a controversial figure around the world."
    ));

    bookBaseList.add(new BookBase("Dan Brown", Language.ENG,
        "Inferno", 578, 2013,
        Arrays.asList(genreMystery, genreThriller, genreCrime,
            genreDetectiveFiction, genreConspiracyFiction),
        "Florence: Harvard symbologist Robert Langdon awakes in a hospital bed with no recollection of where he is or how he got there. Nor can he explain the origin of the macabre object that is found hidden in his belongings. A threat to his life will propel him and a young doctor, Sienna Brooks, into a breakneck chase across the city. Only Langdon’s knowledge of the hidden passageways and ancient secrets that lie behind its historic facade can save them from the clutches of their unknown pursuers."
    ));

    bookBaseList.add(new BookBase("Erich Maria Remarque", Language.ENG,
        "Three Comrades", 496, 1998,
        Arrays.asList(genreWarNovel),
        "The year is 1928. On the outskirts of a large German city, three young men are earning a thin and precarious living. Fully armed young storm troopers swagger in the streets. Restlessness, poverty, and violence are everywhere. For these three, friendship is the only refuge from the chaos around them. Then the youngest of them falls in love, and brings into the group a young woman who will become a comrade as well, as they are all tested in ways they can have never imagined."
    ));

    bookBaseList.add(new BookBase("Alexandre Dumas", Language.ENG,
        "The Count of Monte Cristo", 928, 1997,
        Arrays.asList(genreAdventure),
        "Thrown in prison for a crime he has not committed, Edmond Dantès is confined to the grim fortress of If. There he learns of a great hoard of treasure hidden on the Isle of Monte Cristo and he becomes determined not only to escape, but also to use the treasure to plot the destruction of the three men responsible for his incarceration. Dumas' epic tale of suffering and retribution, inspired by a real-life case of wrongful imprisonment, was a huge popular success when it was first serialized in the 1840s."
    ));

    bookBaseList.add(new BookBase("J.K. Rowling", Language.ENG,
        "Harry Potter and the Philosopher's Stone", 345, 2015,
        Arrays.asList(genreFantasy, genreDrama, genreYoungAdultFiction,
            genreMystery, genreThriller),
        "Harry Potter has never even heard of Hogwarts when the letters start dropping on the doormat at number four, Privet Drive. Addressed in green ink on yellowish parchment with a purple seal, they are swiftly confiscated by his grisly aunt and uncle. Then, on Harry's eleventh birthday, a great beetle-eyed giant of a man called Rubeus Hagrid bursts in with some astonishing news: Harry Potter is a wizard, and he has a place at Hogwarts School of Witchcraft and Wizardry. An incredible adventure is about to begin!"
    ));

    bookBaseList.add(new BookBase("J.K. Rowling", Language.ENG,
        "Harry Potter and the Chamber of Secrets", 373, 2015,
        Arrays.asList(genreFantasy, genreDrama, genreYoungAdultFiction,
            genreMystery, genreThriller),
        "Harry Potter's summer has included the worst birthday ever, doomy warnings from a house-elf called Dobby, and rescue from the Dursleys by his friend Ron Weasley in a magical flying car! Back at Hogwarts School of Witchcraft and Wizardry for his second year, Harry hears strange whispers echo through empty corridors - and then the attacks start. Students are found as though turned to stone... Dobby's sinister predictions seem to be coming true."
    ));

    bookBaseList.add(new BookBase("J.K. Rowling", Language.ENG,
        "Harry Potter and the Prisoner of Azkaban", 469, 2015,
        Arrays.asList(genreFantasy, genreDrama, genreYoungAdultFiction,
            genreMystery, genreThriller),
        "When the Knight Bus crashes through the darkness and screeches to a halt in front of him, it's the start of another far from ordinary year at Hogwarts for Harry Potter. Sirius Black, escaped mass-murderer and follower of Lord Voldemort, is on the run - and they say he is coming after Harry. In his first ever Divination class, Professor Trelawney sees an omen of death in Harry's tea leaves... But perhaps most terrifying of all are the Dementors patrolling the school grounds, with their soul-sucking kiss..."
    ));

    bookBaseList.add(new BookBase("J.K. Rowling", Language.ENG,
        "Harry Potter and the Goblet of Fire", 633, 2015,
        Arrays.asList(genreFantasy, genreDrama, genreYoungAdultFiction,
            genreMystery, genreThriller),
        "The Triwizard Tournament is to be held at Hogwarts. Only wizards who are over seventeen are allowed to enter - but that doesn't stop Harry dreaming that he will win the competition. Then at Hallowe'en, when the Goblet of Fire makes its selection, Harry is amazed to find his name is one of those that the magical cup picks out. He will face death-defying tasks, dragons and Dark wizards, but with the help of his best friends, Ron and Hermione, he might just make it through - alive!"
    ));

    bookBaseList.add(new BookBase("J.K. Rowling", Language.ENG,
        "Harry Potter and Order of the Phoenix", 815, 2015,
        Arrays.asList(genreFantasy, genreDrama, genreYoungAdultFiction,
            genreMystery, genreThriller),
        "Dark times have come to Hogwarts. After the Dementors' attack on his cousin Dudley, Harry Potter knows that Voldemort will stop at nothing to find him. There are many who deny the Dark Lord's return, but Harry is not alone: a secret order gathers at Grimmauld Place to fight against the Dark forces. Harry must allow Professor Snape to teach him how to protect himself from Voldemort's savage assaults on his mind. But they are growing stronger by the day and Harry is running out of time..."
    ));

    bookBaseList.add(new BookBase("J.K. Rowling", Language.ENG,
        "Harry Potter and the Half-Blood Prince", 652, 2015,
        Arrays.asList(genreFantasy, genreDrama, genreYoungAdultFiction,
            genreMystery, genreThriller),
        "When Dumbledore arrives at Privet Drive one summer night to collect Harry Potter, his wand hand is blackened and shrivelled, but he does not reveal why. Secrets and suspicion are spreading through the wizarding world, and Hogwarts itself is not safe. Harry is convinced that Malfoy bears the Dark Mark: there is a Death Eater amongst them. Harry will need powerful magic and true friends as he explores Voldemort's darkest secrets, and Dumbledore prepares him to face his destiny..."
    ));

    bookBaseList.add(new BookBase("J.K. Rowling", Language.ENG,
        "Harry Potter and the Deathly Hallows", 313, 2015,
        Arrays.asList(genreFantasy, genreDrama, genreYoungAdultFiction,
            genreMystery, genreThriller),
        "As he climbs into the sidecar of Hagrid's motorbike and takes to the skies, leaving Privet Drive for the last time, Harry Potter knows that Lord Voldemort and the Death Eaters are not far behind. The protective charm that has kept Harry safe until now is broken, but he cannot keep hiding. The Dark Lord is breathing fear into everything Harry loves and to stop him Harry will have to find and destroy the remaining Horcruxes. The final battle must begin - Harry must stand and face his enemy..."
    ));

    bookBaseList.add(new BookBase("Stephen King", Language.ENG,
        "Pet Sematary", 561, 2014,
        Collections.singletonList(genreHorror),
        "When Dr. Louis Creed takes a new job and moves his family to the idyllic rural town of Ludlow, Maine, this new beginning seems too good to be true. Despite Ludlow’s tranquility, an undercurrent of danger exists here. Those trucks on the road outside the Creed’s beautiful old home travel by just a little too quickly, for one thing…as is evidenced by the makeshift graveyard in the nearby woods where generations of children have buried their beloved pets. Then there are the warnings to Louis both real and from the depths of his nightmares that he should not venture beyond the borders of this little graveyard where another burial ground lures with seductive promises and ungodly temptations. A blood-chilling truth is hidden there—one more terrifying than death itself, and hideously more powerful. As Louis is about to discover for himself sometimes, dead is better…"
    ));

    bookBaseList.add(new BookBase("Stephen King", Language.ENG,
        "The Shining", 708, 2008,
        Arrays.asList(genreHorror, genreGothicNovel, genrePsychologicalHorror),
        "Jack Torrance’s new job at the Overlook Hotel is the perfect chance for a fresh start. As the off-season caretaker at the atmospheric old hotel, he’ll have plenty of time to spend reconnecting with his family and working on his writing. But as the harsh winter weather sets in, the idyllic location feels ever more remote . . . and more sinister. And the only one to notice the strange and terrible forces gathering around the Overlook is Danny Torrance, a uniquely gifted five-year-old."
    ));

    bookBaseList.add(new BookBase("Stephen King", Language.ENG,
        "The Dark Half", 529, 2016,
        Collections.singletonList(genrePsychologicalHorror),
        "Thad Beaumont is a writer, and for a dozen years he has secretly published violent bestsellers under the name of George Stark. But Thad is a healthier and happier man now, the father of infant twins, and starting to write as himself again. He no longer needs George Stark and so, with nationwide publicity, the pseudonym is retired. But George Stark won’t go willingly. And now Thad would like to say he is innocent. He’d like to say he has nothing to do with the twisted imagination that produced his bestselling novels. He’d like to say he has nothing to do with the series of monstrous murders that keep coming closer to his home. But how can Thad deny the ultimate embodiment of evil that goes by the name he gave it—and signs its crimes with Thad’s bloody fingerprints?"
    ));

    bookBaseList.add(new BookBase("Stephen King", Language.ENG,
        "Needful Things", 817, 2016,
        Collections.singletonList(genreHorror),
        "The town of Castle Rock, Maine has seen its fair share of oddities over the years, but nothing is as peculiar as the little curio shop that’s just opened for business here. Its mysterious proprietor, Leland Gaunt, seems to have something for everyone out on display at Needful Things…interesting items that run the gamut from worthless to priceless. Nothing has a price tag in this place, but everything is certainly for sale. The heart’s desire for any resident of Castle Rock can easily be found among the curiosities…in exchange for a little money and—at the specific request of Leland Gaunt—a whole lot of menace against their fellow neighbors. Everyone in town seems willing to make a deal at Needful Things, but the devil is in the details. And no one takes heed of the little sign hanging on the wall: Caveat emptor. In other words, let the buyer beware…"
    ));

    bookBaseList.add(new BookBase("Stephen King", Language.ENG,
        "Dolores Claiborne", 337, 2016,
        Collections.singletonList(genrePsychologicalThriller),
        "Dolores Claiborne is suspected of killing Vera Donovan, her wealthy employer, and when the police question her, she tells the story of her life, harkening back to her disintegrating marriage and the suspicious death of her violent husband thirty years earlier. Dolores also tells of Vera’s physical and mental decline and how she became emotionally demanding in recent years. Given a voice as compelling as any in contemporary fiction, the strange intimacy between Dolores and Vera—and the link that binds them—unfolds in Dolores’s account. It shows, finally, how fierce love can be, and how dreadful its consequences. And how the soul, harrowed by the hardest life, can achieve a kind of grace."
    ));
    for (int i = 0; i < 10; i++) {
      bookBaseList.add(new BookBase("Template author", Language.ENG,
          "Template title " + (i + 1), 999, 2020,
          Collections.singletonList(genreList.get(i % genreList.size())),
          description));
    }

    try {
      Thread.sleep(10);
    } catch (Exception ignored) {
    }
    bookBaseList.add(new BookBase("Oleg Popov", Language.RU,
        "Cycle 0", 1000, 2020, Collections.singletonList(genrePoems),
        //"https://shadycharacters.co.uk/wp/wp-content/uploads/2016/12/Book_IMG_1754-1-e1481474081467.jpg",
        "Poems by unknown author about void and being"));

    BookBase anotherBase = new BookBase("Progenitor (0)", Language.RU,
        "Keeper's being", 1000, 2020, new ArrayList<>()
        //,"https://shadycharacters.co.uk/wp/wp-content/uploads/2016/12/Book_IMG_1754-1-e1481474081467.jpg",
    );
    anotherBase.getGenres().add(genrePoems);
    BookBase confuciusBase = new BookBase("Confucius", Language.ENG,
        "Confucian Analects", 230,
        1996, new ArrayList<>(),
        "Confucian Analects is an ancient Chinese book composed of a large collection of sayings and ideas attributed to the Chinese philosopher Confucius and his contemporaries, traditionally believed to have been compiled and written by Confucius's followers. ");
    confuciusBase.getGenres().add(genrePoems);
    // Books
    List<Book> books = new ArrayList<>();
    books.add(new Book(exteriorQualityList.get(2), userList.get(4),
        bookBaseList.get(0), moscow
    ));
    books.add(new Book(exteriorQualityList.get(4), userList.get(5),
        bookBaseList.get(2), moscow
    ));
    books.add(new Book(exteriorQualityList.get(1), userList.get(5),
        bookBaseList.get(7), moscow
    ));

    // Complaint
    List<Complaint> complaints = List.of(new Complaint(userList.get(4),
            "This user look's like bot. Have a look at him.", userList.get(0)),
        new Complaint(userList.get(2),
            "Didn't receive book from him.", userList.get(1)),
        new Complaint(userList.get(3),
            "His avatar seems bad.", userList.get(4)));

    // Book exchange request
    //BookExchangeRequest bookExchangeRequest = new BookExchangeRequest(
    //    userList.get(4), userList.get(0), book);

    // Book base rate
    List<BookBaseRate> bookBaseRates = new ArrayList<>();
    bookBaseRates.add(new BookBaseRate(userList.get(4),
        bookBaseList.get(2), 4.5F,
        "Interesting book. Recommended it to my friends."));
    bookBaseRates.add(new BookBaseRate(userList.get(1),
        bookBaseList.get(2), 3.5F,
        "Good book to read on your holidays."));
    bookBaseRates.add(new BookBaseRate(userList.get(1),
        bookBaseList.get(2), 4F,
        "Like it."));

    bookBaseRates.add(new BookBaseRate(userList.get(0),
        bookBaseList.get(1), 3F,
        "Too philosophic for me. I would rather choose another book to read."));
    bookBaseRates.add(new BookBaseRate(userList.get(2),
        bookBaseList.get(3), 5F,
        "Brilliant story. You have to read it!"));
    bookBaseRates.add(new BookBaseRate(userList.get(1),
        bookBaseList.get(6), 1F,
        "Worst book I have ever read. Dislike it!"));
    bookBaseRates.add(new BookBaseRate(userList.get(0),
        bookBaseList.get(6), 4.5F,
        "I love it! Best book for children."));

    // Dialogs and messages
    //Message initMessage = new Message(userList.get(0), userList.get(1),
    //    "Hello, friend! Please, give me a book!");
    //Dialog dialog = new Dialog(List.of(userList.get(0), userList.get(1)),
    //    initMessage);
    //initMessage.setDialog(dialog);

    return args -> {
      townRepository.save(moscow);

      for (var quality : exteriorQualityList) {
        exteriorQualityRepository.save(quality);
      }
      for (var user : userList) {
        userRepository.save(user);
      }
      for (var bookBase : bookBaseList) {
        bookBaseRepository.save(bookBase);
      }
      //bookBaseRepository.save(anotherBase);
      //bookBaseRepository.save(confuciusBase);

      for (var genre : genreList) {
        genreRepository.save(genre);
      }

      // BookBase request
      List<BookBaseRequest> bookBaseRequests = List.of(
          new BookBaseRequest(anotherBase, userList.get(4)),
          new BookBaseRequest(confuciusBase, userList.get(3)));

      for (var req : bookBaseRequests) {
        bookBaseRequestRepository.save(req);
      }

      //dialogRepository.save(dialog);
      //messageRepository.save(initMessage);

      for (var complaint : complaints) {
        complaintRepository.save(complaint);
      }

      boolean picLoaded = true;
      byte[] bookPhoto1984 = new byte[0];
      byte[] bookPhotoSolaris = new byte[0];
      byte[] bookPhotoAngels = new byte[0];
      byte[] arrayDataGrapefruit = new byte[0];
      byte[] arrayDataBookPhoto = new byte[0];
      List<byte[]> bookPics = new ArrayList<>();
      if (picLoaded) {
        try {
          bookPhoto1984 = Files.readAllBytes(
              Path.of("files/1984photo.jpg"));
          bookPhotoSolaris = Files.readAllBytes(
              Path.of("files/solarisPhoto.jpg"));
          bookPhotoAngels = Files.readAllBytes(
              Path.of("files/angelsPhoto.jpg"));

          arrayDataGrapefruit = Files.readAllBytes(
              Path.of("files/grapefruit.jpg"));
          arrayDataBookPhoto = Files.readAllBytes(
              Path.of("files/bookPhoto.jpg"));
          picLoaded = true;

          bookPics.add(Files.readAllBytes(
              Path.of("files/1984.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/Fahrenheit451.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/solaris.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/journey.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/murderClub.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/theFallOfGondolin.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/theLastWish.jpg")));

          bookPics.add(Files.readAllBytes(
              Path.of("files/angels and demons.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/the lost symbol.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/the da vinci code.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/origin.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/inferno.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/three comrades.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/the count of monte cristo.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/HP philosopher's stone.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/HP chamber of secrets.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/HP prisoner of azkaban.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/HP goblet of fire.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/HP order of the phoenix.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/HP half-blood prince.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/HP deathly hallows.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/pet sematary.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/the shining.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/the dark half.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/needful things.jpg")));
          bookPics.add(Files.readAllBytes(
              Path.of("files/dolores claiborne.jpg")));

          bookPics.add(Files.readAllBytes(
              Path.of("files/cycle0.jpg")));


        } catch (IOException ex) {
          log.info("Error loading picture: " + ex.getMessage());
        }
      }

      if (picLoaded) {
        Avatar userAvatar = new Avatar(arrayDataGrapefruit, userList.get(4));
        userList.get(4).setAvatar(userAvatar);
        avatarRepository.save(userAvatar);
      }

      for (Book book : books) {
        bookRepository.save(book);
        book.getOwner().addBookToExchangeList(book);
        userRepository.save(book.getOwner());

      }

      if (picLoaded) {
        List<BookPhoto> bookPhotos = new ArrayList<>();
        bookPhotos.add(
            new BookPhoto(bookPhoto1984, userList.get(0), books.get(0)));
        bookPhotos.add(
            new BookPhoto(bookPhotoSolaris, userList.get(5), books.get(1)));
        bookPhotos.add(
            new BookPhoto(bookPhotoAngels, userList.get(5), books.get(2)));

        for (int i = 0; i < bookPhotos.size(); i++) {
          books.get(i).setPhoto(bookPhotos.get(i));
          bookPhotoRepository.save(bookPhotos.get(i));
        }

        BookBasePhoto bookBasePhoto;
        for (int i = 0; i < bookBaseList.size(); i++) {
          if (i < bookPics.size()) {
            bookBasePhoto = new BookBasePhoto(bookPics.get(i),
                userList.get(2), bookBaseList.get(i));
          } else {
            bookBasePhoto = new BookBasePhoto(arrayDataBookPhoto,
                userList.get(0), bookBaseList.get(i));
          }
          bookBaseList.get(i).setPhoto(bookBasePhoto);
          bookBaseRepository.save(bookBaseList.get(i));
          bookBasePhotoRepository.save(bookBasePhoto);
        }
      }

      genreRepository.save(genrePoems);

      //bookExchangeRequestRepository.save(bookExchangeRequest);

      for (BookBaseRate rate : bookBaseRates) {
        bookBaseRateRepository.save(rate);
      }

      User user = userList.get(4);
      BookBase bookBase = bookBaseList.get(7);
      user.addToWishList(bookBase);
      bookBase.addWisher(user);
      userRepository.save(user);
      bookBaseRepository.save(bookBase);

      log.info("DONE!");
    };
  }

  private List<ExteriorQuality> generateExteriorQualityList() {
    return new ArrayList<>() {{
      add(new ExteriorQuality("Unknown"));
      add(new ExteriorQuality("Well worn"));
      add(new ExteriorQuality("Worn"));
      add(new ExteriorQuality("MinimalWear"));
      add(new ExteriorQuality("Perfect"));
    }};
  }
}
