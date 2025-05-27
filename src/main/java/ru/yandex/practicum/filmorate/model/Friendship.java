package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Integer userId;
    private Integer friendId;
    private String status; // 'PENDING' или 'CONFIRMED'
    private LocalDateTime requestTime;
}

