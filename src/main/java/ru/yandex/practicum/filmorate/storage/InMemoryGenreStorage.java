package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class InMemoryGenreStorage implements GenreStorage {
    private final Set<Integer> genreIds = Set.of(1, 2, 3, 4, 5);

    @Override
    public boolean existsById(Integer genreId) {
        return genreIds.contains(genreId);
    }
}