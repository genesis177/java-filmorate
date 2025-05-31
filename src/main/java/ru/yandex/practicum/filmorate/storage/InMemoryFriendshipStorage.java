package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.status.FriendshipStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryFriendshipStorage {

    private final Map<Long, Map<Long, Friendship>> friendships = new ConcurrentHashMap<>();

    public boolean userExists(Long userId) {
        // В данном контексте, предполагается, что пользователь существует, проверка вне
        // Можно оставить как есть или реализовать внешнюю проверку
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
        // Проверяем, есть ли заявка от friendId к userId
        Map<Long, Friendship> friendRequests = friendships.get(friendId);
        if (friendRequests == null || !friendRequests.containsKey(userId)) {
            throw new IllegalStateException("Заявки нет");
        }
        Friendship request = friendRequests.get(userId);
        if (!FriendshipStatus.PENDING.name().equals(request.getStatus())) {
            throw new IllegalStateException("Заявки нет");
        }
        // Обновляем статус
        request.setStatus(FriendshipStatus.CONFIRMED.name());
        request.setRequestTime(LocalDateTime.now());

        // Создаем или обновляем обратную запись для двусторонней дружбы
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
        if (!userExists(userId) || !userExists(friendId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
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
        if (!userExists(userId)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
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
        if (!userExists(userId1) || !userExists(userId2)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        Set<Long> friends1 = new HashSet<>(getFriends(userId1));
        Set<Long> friends2 = new HashSet<>(getFriends(userId2));
        friends1.retainAll(friends2);
        return new ArrayList<>(friends1);
    }
}