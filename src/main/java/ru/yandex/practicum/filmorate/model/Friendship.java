package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Long userId; // ID инициатора заявки
    private Long friendId; // ID получателя заявки
    private String status; // PENDING или CONFIRMED
    private LocalDateTime requestTime; // Время заявки

    public Friendship(Long userId, Long friendId, String status, LocalDateTime requestTime) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.requestTime = requestTime;
    }
}
