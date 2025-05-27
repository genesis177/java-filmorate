package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.time.LocalDateTime;

@Service
public class FriendshipService {

    private final UserStorage userStorage;

    private static final String PENDING = "PENDING";
    private static final String CONFIRMED = "CONFIRMED";

    private final List<Friendship> friendships = new ArrayList<>();

    @Autowired
    public FriendshipService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void sendFriendRequest(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (areFriends(userId, friendId)) {
            throw new IllegalStateException("Пользователь уже в друзьях");
        }

        if (isFriendshipPending(userId, friendId)) {
            throw new IllegalStateException("Заявка уже отправлена");
        }

        friendships.add(new Friendship(userId, friendId, PENDING, LocalDateTime.now()));
    }

    public void confirmFriendship(Long userId, Long friendId) {
        Optional<Friendship> f1 = findFriendship(userId, friendId);
        Optional<Friendship> f2 = findFriendship(friendId, userId);

        if (f1.isEmpty() || f2.isEmpty()) {
            throw new NoSuchElementException("Заявка не найдена");
        }

        if (!f1.get().getStatus().equals(PENDING) || !f2.get().getStatus().equals(PENDING)) {
            throw new IllegalStateException("Дружба уже подтверждена");
        }

        f1.get().setStatus(CONFIRMED);
        f2.get().setStatus(CONFIRMED);
    }

    public void removeFriend(Long userId, Long friendId) {
        Optional<Friendship> f1 = findFriendship(userId, friendId);
        Optional<Friendship> f2 = findFriendship(friendId, userId);

        if (f1.isEmpty() || f2.isEmpty()) {
            throw new IllegalStateException("Дружба не найдена");
        }

        friendships.remove(f1.get());
        friendships.remove(f2.get());
    }

    public Set<Long> getFriends(Long userId) {
        Set<Long> friends = new HashSet<>();
        for (Friendship f : friendships) {
            if (f.getStatus().equals(CONFIRMED)) {
                if (f.getUserId().equals(userId)) {
                    friends.add(f.getFriendId());
                }
            }
        }
        return friends;
    }

    public Set<Long> getCommonFriends(Long userId, Long otherId) {
        Set<Long> friends1 = getFriends(userId);
        Set<Long> friends2 = getFriends(otherId);
        friends1.retainAll(friends2);
        return friends1;
    }

    private Optional<Friendship> findFriendship(Long u1, Long u2) {
        return friendships.stream()
                .filter(f -> f.getUserId().equals(u1) && f.getFriendId().equals(u2))
                .findFirst();
    }

    private boolean areFriends(Long u1, Long u2) {
        return friendships.stream().anyMatch(f ->
                f.getUserId().equals(u1) && f.getFriendId().equals(u2) && f.getStatus().equals(CONFIRMED));
    }

    private boolean isFriendshipPending(Long u1, Long u2) {
        return friendships.stream().anyMatch(f ->
                f.getUserId().equals(u1) && f.getFriendId().equals(u2) && f.getStatus().equals(PENDING));
    }

    private User getUserById(Long id) {
        return userStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }
}