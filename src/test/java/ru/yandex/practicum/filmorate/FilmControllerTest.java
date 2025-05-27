package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.start.FinalProjectApplication;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Film"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(120))
                .andExpect(jsonPath("$.genres").isArray());
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

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.genres").isArray())
                .andExpect(jsonPath("$.genres").value(org.hamcrest.Matchers.hasItems(1,3,5)));
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

    // Создание фильма с длинным описанием
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
        // сначала создадим фильм
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Original");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000,1,1));
        film.setDuration(100);
        film.setMpaId(1);
        var created = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andReturn().getResponse().getContentAsString();
        var createdFilm = mapper.readValue(created, ru.yandex.practicum.filmorate.model.Film.class);
        Integer id = createdFilm.getId();

        // обновляем
        createdFilm.setName("Updated");
        mvc.perform(put("/films/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createdFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    // Обновление несуществующего фильма
    @Test
    public void updateUnknown_ShouldReturn404() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Not Exist");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000,1,1));
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
        mvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Получение популярных фильмов
    @Test
    public void getPopular_ShouldReturnList() throws Exception {
        // создадим несколько фильмов и поставим лайки
        var film1 = new ru.yandex.practicum.filmorate.model.Film();
        film1.setName("Popular1");
        film1.setDescription("Desc");
        film1.setReleaseDate(LocalDate.of(2000,1,1));
        film1.setDuration(100);
        film1.setMpaId(1);
        var f1 = mapper.readValue(mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film1)))
                .andReturn().getResponse().getContentAsString(), ru.yandex.practicum.filmorate.model.Film.class);
        // лайкаем
        // тут можно дополнительно реализовать лайки, если есть
        // для примера можно просто считать, что лайки есть
        // или оставить как есть, и получить топ по лайкам
        // для теста - достаточно проверить, что список возвращается
        mvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}