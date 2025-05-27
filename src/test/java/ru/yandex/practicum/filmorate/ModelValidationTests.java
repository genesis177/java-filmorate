package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.ValidationUtil;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ModelValidationTests {

    @Test
    public void testValidFilm() {
        Film film = new Film();
        film.setName("Тест фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        assertDoesNotThrow(() -> ValidationUtil.validateFilm(film));
    }

    @Test
    public void testInvalidFilmName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        assertThrows(AssertionError.class, () -> ValidationUtil.validateFilm(film));
    }

    @Test
    public void testInvalidFilmDescription() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("x".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        assertThrows(AssertionError.class, () -> ValidationUtil.validateFilm(film));
    }

    @Test
    public void testInvalidReleaseDate() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(120);
        assertThrows(AssertionError.class, () -> ValidationUtil.validateFilm(film));
    }

    @Test
    public void testInvalidDuration() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);
        assertThrows(AssertionError.class, () -> ValidationUtil.validateFilm(film));
    }

    @Test
    public void testValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertDoesNotThrow(() -> ValidationUtil.validateUser(user));
        assertEquals("test login", user.getName());
    }

    @Test
    public void testInvalidEmail() {
        User user = new User();
        user.setEmail("invalid email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(AssertionError.class, () -> ValidationUtil.validateUser(user));
    }

    @Test
    public void testEmptyLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(AssertionError.class, () -> ValidationUtil.validateUser(user));
    }

    @Test
    public void testLoginWithSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(AssertionError.class, () -> ValidationUtil.validateUser(user));
    }

    @Test
    public void testFutureBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(AssertionError.class, () -> ValidationUtil.validateUser(user));
    }

    @Test
    public void testValidBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertDoesNotThrow(() -> ValidationUtil.validateUser(user));
    }

    @Test
    public void testNullNameDefaultsToLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertDoesNotThrow(() -> ValidationUtil.validateUser(user));
        assertEquals("test login", user.getName());
    }
}
