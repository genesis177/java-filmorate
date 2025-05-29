package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id; // Идентификатор пользователя
    private String email;
    private String login; // Логин пользователя
    private String name; // Имя пользователя, может быть пустым, тогда заполняется логином

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday; // День рождения

    private Set<Long> friends = new HashSet<>(); // Множество ID друзей
    private Set<Long> pendingRequests = new HashSet<>(); // Множество входящих заявок
}
