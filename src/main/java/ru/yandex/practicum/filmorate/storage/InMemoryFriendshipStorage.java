package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.status.FriendshipStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryFriendshipStorage {

    // Ключ — пара userId и friendId (userId -> friendId)
    private final Map<Long, Map<Long, Friendship>> friendships = new ConcurrentHashMap<>();

    public boolean userExists(Long userId) {
        // В in-memory хранилище друзей не храним пользователей,
        // предполагается, что проверка пользователей происходит в UserStorage
        return true;
    }

    public boolean existsFriendship(Long userId, Long friendId) {
        Map<Long, Friendship> userFriends = friendships.get(userId);
        if (userFriends != null && userFriends.containsKey(friendId)) {
            return true;
        }
        Map<Long, Friendship> friendFriends = friendships.get(friendId);
        return friendFriends != null && friendFriends.containsKey(userId);
    }

    public void addFriend(Long userId, Long friendId) {
        friendships.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(friendId, new Friendship(userId, friendId, FriendshipStatus.PENDING.name(), LocalDateTime.now()));
    }

    public void confirmFriend(Long userId, Long friendId) {
        Map<Long, Friendship> friendRequests = friendships.get(friendId);
        if (friendRequests == null) {
            throw new IllegalStateException("Заявки нет");
        }
        Friendship request = friendRequests.get(userId);
        if (request == null || !FriendshipStatus.PENDING.name().equals(request.getStatus())) {
            throw new IllegalStateException("Заявки нет");
        }
        // Обновляем статус на CONFIRMED в обеих сторонах
        request.setStatus(FriendshipStatus.CONFIRMED.name());
        request.setRequestTime(LocalDateTime.now());

        // Здесь нужно обновить существующую запись у userId, если она есть, а не создавать новую
        Map<Long, Friendship> userFriends = friendships.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        Friendship reciprocal = userFriends.get(friendId);
        if (reciprocal == null) {
            reciprocal = new Friendship(userId, friendId, FriendshipStatus.CONFIRMED.name(), LocalDateTime.now());
            userFriends.put(friendId, reciprocal);
        } else {
            reciprocal.setStatus(FriendshipStatus.CONFIRMED.name());
            reciprocal.setRequestTime(LocalDateTime.now());
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        Map<Long, Friendship> userFriends = friendships.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }
        Map<Long, Friendship> friendFriends = friendships.get(friendId);
        if (friendFriends != null) {
            friendFriends.remove(userId);
        }
    }

    public List<Long> getFriends(Long userId) {
        Map<Long, Friendship> userFriends = friendships.get(userId);
        if (userFriends == null) {
            return Collections.emptyList();
        }
        List<Long> confirmedFriends = new ArrayList<>();
        for (Friendship f : userFriends.values()) {
            if (FriendshipStatus.CONFIRMED.name().equals(f.getStatus())) {
                confirmedFriends.add(f.getFriendId());
            }
        }
        return confirmedFriends;
    }

    public List<Long> getCommonFriends(Long userId1, Long userId2) {
        Set<Long> friends1 = new HashSet<>(getFriends(userId1));
        Set<Long> friends2 = new HashSet<>(getFriends(userId2));
        friends1.retainAll(friends2);
        return new ArrayList<>(friends1);
    }
}
