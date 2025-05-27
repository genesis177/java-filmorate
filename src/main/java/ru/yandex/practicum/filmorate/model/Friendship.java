package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Long userId;
    private Long friendId;
    private String status; // PENDING или CONFIRMED
    private LocalDateTime requestTime;

    public Friendship(Long userId, Long friendId, String status, LocalDateTime requestTime) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.requestTime = requestTime;
    }
}