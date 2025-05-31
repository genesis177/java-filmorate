package ru.yandex.practicum.filmorate.exception;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException(Integer genreId) {
        super("Жанр с id " + genreId + " не существует");
    }
}