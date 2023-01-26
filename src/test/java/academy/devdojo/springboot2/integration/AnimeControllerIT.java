package academy.devdojo.springboot2.integration;

import academy.devdojo.springboot2.domain.Anime;
import academy.devdojo.springboot2.domain.DevDojoUser;
import academy.devdojo.springboot2.repository.AnimeRepository;
import academy.devdojo.springboot2.repository.DevDojoUserRepository;
import academy.devdojo.springboot2.requests.AnimePostRequestBody;
import academy.devdojo.springboot2.util.AnimeCreator;
import academy.devdojo.springboot2.util.AnimePostRequestBodyCreator;
import academy.devdojo.springboot2.wrapper.PageableResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AnimeControllerIT {
    @LocalServerPort
    protected int port;

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private DevDojoUserRepository devDojoUserRepository;

    static String baseUrl = "http://localhost:";

    //user basic auth
    static TestRestTemplate testRestTemplateUser =
            new TestRestTemplate("devdojo",
                    "senha123");

    //admin basic auth
    static TestRestTemplate testRestTemplateAdmin =
            new TestRestTemplate("nicholas",
                    "senha123");

    private static final DevDojoUser ADMIN = DevDojoUser.builder().name("Nicholas")
            .password("$2a$10$qL3in5bAlEM3oeaK9BWBLuZcB4Nh0zqN0VJ2a6XoUwV8m.NbMbOC6")
            .username("nicholas")
            .authorities("ROLE_ADMIN")
            .build();

    private static final DevDojoUser USER = DevDojoUser.builder().name("DevDojo")
            .password("$2a$10$qL3in5bAlEM3oeaK9BWBLuZcB4Nh0zqN0VJ2a6XoUwV8m.NbMbOC6")
            .username("devdojo")
            .authorities("ROLE_USER")
            .build();

    @Test
    @DisplayName("list returns list of anime inside page object when successful")
    void list_ReturnsListOfAnimesInsideObject_WhenSuccessful() {
        Anime anime = AnimeCreator.createValidAnime();
        Anime savedAnime = animeRepository.save(anime);
        String expectedName = savedAnime.getName();
        devDojoUserRepository.save(USER);

        PageableResponse<Anime> animes = testRestTemplateUser.exchange(baseUrl + port + "/animes", HttpMethod.GET
                , null, new ParameterizedTypeReference<PageableResponse<Anime>>() {
                }).getBody();

        Assertions.assertThat(animes.toList()).isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animes.toList().get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("listAllNonPageable returns list of anime when successful")
    void listAllNonPageable_ReturnsListOfAnimes_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createValidAnime());
        String expectedName = savedAnime.getName();
        devDojoUserRepository.save(USER);

        List<Anime> animePage = testRestTemplateUser.exchange(baseUrl + port + "/animes/all", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animePage).isNotNull().isNotEmpty().hasSize(1);

        Assertions.assertThat(animePage.get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("findByName returns a list of anime when successful")
    void findByName_ReturnsListOfAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createValidAnime());
        String expectedName = savedAnime.getName();
        devDojoUserRepository.save(USER);

        String url = String.format(baseUrl + port + "/animes/find?name=%s", expectedName);

        List<Anime> animePage = testRestTemplateUser.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animePage).isNotNull().isNotEmpty().hasSize(1);

        Assertions.assertThat(animePage.get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("findByName returns an empty list of anime when anime is not found")
    void findByName_ReturnsEmptyListOfAnime_WhenAnimeIsNotFound() {
        devDojoUserRepository.save(USER);
        String url = String.format(baseUrl + port + "/animes/find?name=dbz");

        List<Anime> animePage = testRestTemplateUser.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animePage).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("save returns anime when successful")
    void save_ReturnsAnime_WhenSuccessful() {
        AnimePostRequestBody animeToBeSaved = AnimePostRequestBodyCreator.createAnimeToBeSaved();
        devDojoUserRepository.save(ADMIN);

        TestRestTemplate testRestTemplate =
                new TestRestTemplate("nicholas",
                        "senha123");

        ResponseEntity<Anime> animeResponseEntity = testRestTemplate.postForEntity(baseUrl + port + "/animes", animeToBeSaved, Anime.class);

        Assertions.assertThat(animeResponseEntity).isNotNull();

        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Assertions.assertThat(animeResponseEntity.getBody()).isNotNull();

        Assertions.assertThat(animeResponseEntity.getBody().getId()).isNotNull();
    }

    @Test
    @DisplayName("replace updates anime when successful")
    void replace_UpdatesAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createValidAnime());
        devDojoUserRepository.save(ADMIN);
        savedAnime.setName("new name");

        ResponseEntity<Void> animeResponseEntity = testRestTemplateAdmin.exchange(baseUrl + port + "/animes",
                HttpMethod.PUT, new HttpEntity<>(savedAnime), Void.class);

        Assertions.assertThat(animeResponseEntity).isNotNull();

        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("delete removes anime when successful")
    void delete_RemovesAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createValidAnime());
        devDojoUserRepository.save(ADMIN);

        ResponseEntity<Anime> animeResponseEntity = testRestTemplateAdmin
                .exchange(baseUrl + port + "/animes/{id}", HttpMethod.DELETE,
                        null, Anime.class, savedAnime.getId());

        Assertions.assertThat(animeResponseEntity).isNotNull();

        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
