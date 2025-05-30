package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Set;

@SpringBootTest(classes = FinalProjectApplication.class)
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    public void setup() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM FILM_GENRES");
            stmt.execute("DELETE FROM FILMS");
            stmt.execute("DELETE FROM GENRES");
            stmt.execute("DELETE FROM MPA");
            // вставляем жанры
            stmt.execute("MERGE INTO GENRES (id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Триллер'), (4, 'Боевик'), (5, 'Мелодрама');");
            // вставляем MPA
            stmt.execute("MERGE INTO MPA (id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17');");
        }
    }

    @Test
    public void createFilm_ShouldReturn201AndBody() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaId(1);
        film.setGenres(Set.of(1, 2));

        String responseStr = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer id = mapper.readTree(responseStr).get("id").asInt();

        mvc.perform(get("/films/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    public void createFilmSeveralGenres_ShouldReturn201() throws Exception {
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
                .andExpect(status().isCreated());
    }

    @Test
    public void createFilmFailName_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName(""); // некорректное имя
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2010, 5, 10));
        film.setDuration(90);
        film.setMpaId(1);

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Название не может быть пустым"));
    }

    @Test
    public void createFilmFailDescription_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Valid");
        film.setDescription("x".repeat(201));
        film.setReleaseDate(LocalDate.of(2010, 5, 10));
        film.setDuration(90);
        film.setMpaId(1);

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Описание не должно превышать 200 символов"));
    }

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата релиза не может быть раньше 28.12.1895"));
    }

    @Test
    public void createFilmFailDuration_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Valid");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);
        film.setMpaId(1);

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Длительность должна быть положительным числом"));
    }

    @Test
    public void updateFilm_ShouldReturn200AndUpdated() throws Exception {
        // создаем фильм
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("Original");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpaId(1);

        String resp = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int id = mapper.readTree(resp).get("id").asInt();

        // обновляем
        var updated = new ru.yandex.practicum.filmorate.model.Film();
        updated.setId(id);
        updated.setName("Updated");
        updated.setDescription("Desc");
        updated.setReleaseDate(LocalDate.of(2000, 1, 1));
        updated.setDuration(100);
        updated.setMpaId(1);

        mvc.perform(put("/films/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

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

    @Test
    public void getAllFilms_ShouldReturnList() throws Exception {
        mvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void getPopular_ShouldReturnList() throws Exception {
        // создаем фильм
        var film = new Film();
        film.setName("Popular");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpaId(1);
        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isCreated());

        mvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk());
    }

    @Test
    public void createFilmWithInvalidGenre_ShouldReturn400() throws Exception {
        var film = new Film();
        film.setName("Test Invalid Genre");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpaId(1);
        film.setGenres(Set.of(9999)); // несуществующий жанр

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Жанр с id 9999 не существует"));
    }

    @Test
    public void createFilmFailGenre_ShouldReturn400() throws Exception {
        var film = new ru.yandex.practicum.filmorate.model.Film();
        film.setName("InvalidGenre");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpaId(1);
        film.setGenres(Set.of(9999));

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Жанр с id 9999 не существует"));
    }
}