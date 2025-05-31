package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    private Integer duration;

    private Mpa mpa;

    private Set<Genre> genres = new LinkedHashSet<>();

    private Set<Long> likes = new HashSet<>();
}