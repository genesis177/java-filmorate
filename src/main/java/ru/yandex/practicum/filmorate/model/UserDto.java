package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Integer friends;  // количество друзей
}