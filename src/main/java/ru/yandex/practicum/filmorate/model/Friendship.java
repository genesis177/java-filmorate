package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Long userId;
    private Long friendId;
    private String status; // 'PENDING' или 'CONFIRMED'
    private LocalDateTime requestTime;
}