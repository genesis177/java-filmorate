package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FriendshipService {

    private final UserStorage userStorage;

    private final List<Friendship> friendships = new ArrayList<>();

    @Autowired
    public FriendshipService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void sendFriendRequest(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            throw new IllegalStateException("Пользователь уже в друзьях");
        }

        Optional<Friendship> existing = findFriendship(userId, friendId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Заявка уже отправлена или дружба уже установлена");
        }

        Friendship friendship = new Friendship();
        friendship.setUserId(user.getId());
        friendship.setFriendId(friend.getId());
        friendship.setStatus("PENDING");
        friendship.setRequestTime(LocalDateTime.now());

        friendships.add(friendship);

        user.getPendingRequests().add(friend.getId());
        friend.getPendingRequests().add(user.getId());

        userStorage.update(user);
        userStorage.update(friend);
    }

    public void confirmFriendship(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (!user.getPendingRequests().contains(friendId) ||
                !friend.getPendingRequests().contains(userId)) {
            throw new NoSuchElementException("Заявка не найдена");
        }

        user.getPendingRequests().remove(friendId);
        friend.getPendingRequests().remove(userId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        updateFriendshipStatus(userId, friendId, "CONFIRMED");
        updateFriendshipStatus(friendId, userId, "CONFIRMED");
    }

    public void removeFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (!user.getFriends().contains(friendId)) {
            throw new IllegalStateException("Дружба не найдена");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    private Optional<Friendship> findFriendship(Long u1, Long u2) {
        return friendships.stream()
                .filter(f ->
                        (f.getUserId().equals(u1) && f.getFriendId().equals(u2)) ||
                                (f.getUserId().equals(u2) && f.getFriendId().equals(u1))
                )
                .findFirst();
    }

    private User getUserById(long id) {
        return userStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }

    private void updateFriendshipStatus(Long u1, Long u2, String status) {
        for (Friendship f : friendships) {
            if (f.getUserId().equals(u1) && f.getFriendId().equals(u2)) {
                f.setStatus(status);
                break;
            }
        }
    }
}