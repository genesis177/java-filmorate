package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.ValidationUtil;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinalProjectApplication.class)
public class ModelValidationTests {

    //проверка валидности правильного фильма
    @Test
    public void testValidFilm() {
        Film film = new Film();
        film.setName("Тест фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));
        film.setGenres(Set.of(new Genre(1, null))); // добавляем жанры, чтобы избежать ошибки
        // не должно выбрасывать исключение
        assertDoesNotThrow(() -> ValidationUtil.validateFilm(film, null));
    }

    //проверка на то, что фильм с пустым названием не проходит валидацию
    @Test
    public void testInvalidFilmName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));
        film.setGenres(Set.of(new Genre(1, null)));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateFilm(film, null));
    }

    //проверка, что описание слишком длинное не проходит валидацию
    @Test
    public void testInvalidFilmDescription() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("x".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));
        film.setGenres(Set.of(new Genre(1, null)));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateFilm(film, null));
    }

    // проверка, что дата релиза раньше 1895 года вызывает ошибку
    @Test
    public void testInvalidReleaseDate() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));
        film.setGenres(Set.of(new Genre(1, null)));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateFilm(film, null));
    }

    //проверка, что длительность равная 0 или меньше, вызывает ошибку
    @Test
    public void testInvalidDuration() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);
        film.setMpa(new Mpa(1, null));
        film.setGenres(Set.of(new Genre(1, null)));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateFilm(film, null));
    }

    //проверка валидации корректного пользователя
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

    // тест на некорректную почту
    @Test
    public void testInvalidEmail() {
        User user = new User();
        user.setEmail("invalid email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateUser(user));
    }

    // тест на пустой login
    @Test
    public void testEmptyLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateUser(user));
    }

    // тест на login, состоящий только из пробелов
    @Test
    public void testLoginWithSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateUser(user));
    }

    // тест на дату рождения в будущем
    @Test
    public void testFutureBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateUser(user));
    }

    //тест на правильность даты рождения
    @Test
    public void testValidBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertDoesNotThrow(() -> ValidationUtil.validateUser(user));
    }

    //если поле name пустое, оно должно автоматически заполниться логином
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