package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
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

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    public void setup() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM FILM_LIKES");
            stmt.execute("DELETE FROM FILM_GENRES");
            stmt.execute("DELETE FROM FRIENDS");
            stmt.execute("DELETE FROM FILMS");
            stmt.execute("DELETE FROM USERS");

            stmt.execute("MERGE INTO GENRES (id, name) VALUES " +
                    "(1, 'Комедия'), " +
                    "(2, 'Драма'), " +
                    "(3, 'Мультфильм'), " +
                    "(4, 'Триллер'), " +
                    "(5, 'Документальный'), " +
                    "(6, 'Боевик');");

            stmt.execute("MERGE INTO MPA (id, name) VALUES " +
                    "(1, 'G'), " +
                    "(2, 'PG'), " +
                    "(3, 'PG-13'), " +
                    "(4, 'R'), " +
                    "(5, 'NC-17');");

            for (int i = 1; i <= 20; i++) {
                stmt.execute(String.format(
                        "MERGE INTO USERS (id, email, login, name, birthday) VALUES (%d, 'user%d@example.com', 'user%d', 'User %d', DATE '1990-01-01')",
                        i, i, i, i));
            }
        }
    }

    @Test
    public void createFilm_ShouldReturn201AndBody() throws Exception {
        var film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));
        film.setGenres(Set.of(new Genre(1, null), new Genre(2, null)));

        String responseStr = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer id = mapper.readTree(responseStr).get("id").asInt();

        mvc.perform(get("/films/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Test Film"))
                .andExpect(jsonPath("$.genres.length()").value(2));
    }

    @Test
    public void createFilmFailName_ShouldReturn400() throws Exception {
        var film = new Film();
        film.setName("");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2010, 5, 10));
        film.setDuration(90);
        film.setMpa(new Mpa(1, null));

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Название не может быть пустым"));
    }

    @Test
    public void updateFilm_ShouldReturn200AndUpdated() throws Exception {
        var film = new Film();
        film.setName("Original");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpa(new Mpa(1, null));

        String resp = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int id = mapper.readTree(resp).get("id").asInt();

        var updated = new Film();
        updated.setId(id);
        updated.setName("Updated");
        updated.setDescription("New description");
        updated.setReleaseDate(LocalDate.of(2000, 1, 1));
        updated.setDuration(100);
        updated.setMpa(new Mpa(1, null));

        mvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.description").value("New description"));
    }

    @Test
    public void getPopular_ShouldReturnList() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Film film = new Film();
            film.setName("Film " + i);
            film.setDescription("Description " + i);
            film.setReleaseDate(LocalDate.of(2000, 1, 1));
            film.setDuration(100);
            film.setMpa(new Mpa(1, null));

            String response = mvc.perform(post("/films")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(film)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Film createdFilm = mapper.readValue(response, Film.class);

            for (long userId = 1; userId <= i; userId++) {
                mvc.perform(put("/films/" + createdFilm.getId() + "/like/" + userId))
                        .andExpect(status().isOk());
            }
        }

        mvc.perform(get("/films/popular?count=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].likes.length()").value(5))
                .andExpect(jsonPath("$[1].likes.length()").value(4));
    }

    @Test
    public void createFilmFailGenre_ShouldReturn400() throws Exception {
        var film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));
        film.setGenres((Set.of(new Genre(999, null)))); // Invalid genre ID

        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Genre with id 999 not found"));
    }
}