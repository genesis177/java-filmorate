package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Integer userID;      // исправлено на camelCase для согласованности с остальным кодом
    private Integer friendID;
    private String status; // 'PENDING' или 'CONFIRMED'
    private LocalDateTime requestTime;

}