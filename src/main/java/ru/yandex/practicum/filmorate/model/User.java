package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private Long id;
    private String email;
    private String login;
    private String name;
    private java.time.LocalDate birthday;

    private Set<Long> friends = new HashSet<>();

    // Для хранения входящих заявок на дружбу:
    private Set<Long> pendingRequests = new HashSet<>();
}