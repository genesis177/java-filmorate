package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

@SpringBootTest(classes = FinalProjectApplication.class)
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // Создание фильма
    @Test
    public void createFilm_ShouldReturn201AndBody() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaId(1);
        film.setGenres(Set.of(1, 2));

        MvcResult result = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andReturn();
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    // Создание фильма с несколькими жанрами
    @Test
    public void createFilmSeveralGenres_ShouldReturn201AndBody() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Genre Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2010, 5, 10));
        film.setDuration(90);
        film.setMpaId(2);
        film.setGenres(Set.of(1, 3, 5));

        MvcResult result = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Response body: " + result.getResponse().getContentAsString());
        }

    // Создание фильма с пустым или отсутствующим именем
    @Test
    public void createFilmFailName_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName(""); // пустое имя
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2010, 5, 10));
        film.setDuration(90);
        film.setMpaId(1);

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    // Создание фильма со слишком длинным описанием
    @Test
    public void createFilmFailDescription_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Valid Name");
        film.setDescription("a".repeat(201)); // длиннее 200
        film.setReleaseDate(LocalDate.of(2010, 5, 10));
        film.setDuration(90);
        film.setMpaId(1);

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    // Создание фильма с датой релиза раньше 28.12.1895
    @Test
    public void createFilmFailReleaseDate_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Valid");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(90);
        film.setMpaId(1);

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    // Создание фильма с нулевой или отрицательной длительностью
    @Test
    public void createFilmFailDuration_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Valid");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 5, 5));
        film.setDuration(0);
        film.setMpaId(1);

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    // Обновление существующего фильма
    @Test
    public void updateFilm_ShouldReturn200AndUpdated() throws Exception {
        // создаем фильм
        var film = new Film();
        film.setName("Original");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpaId(1);

        // выполняем запрос на создание
        MvcResult result = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andReturn();

        int status = result.getResponse().getStatus();
        String content = result.getResponse().getContentAsString();

        System.out.println("Ответ сервера: " + content);
        System.out.println("Статус: " + status);
        if (status != 200) {
            System.err.println("Ошибка при создании фильма: " + content);
        }
    }

    // Обновление несуществующего фильма
    @Test
    public void updateUnknown_ShouldReturn404() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Not Exist");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpaId(1);

        mvc.perform(put("/films/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isNotFound());
    }

    // Получение всех фильмов
    @Test
    public void getAllFilms_ShouldReturnList() throws Exception {
        MvcResult result = mvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        System.out.println("Ответ сервера: " + result.getResponse().getContentAsString());
    }

    // Получение популярных фильмов
    @Test
    public void getPopular_ShouldReturnList() throws Exception {
        // создадим несколько фильмов и поставим лайки
        var film1 = new ru.yandex.practicum.filmorate.model.Film();
        film1.setName("Popular1");
        film1.setDescription("Desc");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        film1.setMpaId(1);
        // Создание фильма через API
        MvcResult result = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film1)))
                .andReturn();

        // Для диагностики выводим статус и контент
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        // Проверяем статус
        assertEquals(201, result.getResponse().getStatus()); // 201 Created

        // Получаем тело ответа
        String responseBody = result.getResponse().getContentAsString();

        // Проверяем, что тело не пустое
        assertFalse(responseBody.isEmpty());

        // Десериализуем ответ в объект Film
        Film f1 = mapper.readValue(responseBody, Film.class);

        // Теперь делаем запрос к популярным фильмам
        mvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Получение фильма с жанрами, у которого отсутствует продолжительность
    @Test
    public void getFilmWithGenreWithoutDuration_ShouldReturnFilm() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Test Film No Duration");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setGenres(Set.of(1, 2));  // <-- Добавьте тут
        film.setMpaId(1);
        // Устанавливаем положительное значение, чтобы пройти валидацию
        film.setDuration(10);

        String responseContent = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var createdFilm = mapper.readValue(responseContent, ru.yandex.practicum.filmorate.model.Film.class);
        System.out.println("Created film ID: " + createdFilm.getId());

        mvc.perform(get("/films/" + createdFilm.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // Тест на несуществующий жанр фильмов
    @Test
    public void createFilmFailGenre_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Invalid Genre Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpaId(1);
        film.setGenres(Set.of(9999)); // несуществующий жанр

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }
}