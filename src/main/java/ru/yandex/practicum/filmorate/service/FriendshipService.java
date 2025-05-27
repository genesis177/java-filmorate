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

    // Для хранения заявок можно использовать Map<Pair<Integer, Integer>, Friendship>
    // или список заявок. Для простоты — используем список.
    private final List<Friendship> friendships = new ArrayList<>();

    @Autowired
    public FriendshipService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void sendFriendRequest(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        // Проверка уже существующих отношений
        if (user.getFriends().contains((long) friendId)) {
            throw new IllegalStateException("Пользователь уже в друзьях");
        }

        // Проверка наличия заявки или дружбы в статусе PENDING или CONFIRMED
        Optional<Friendship> existingFriendship = findFriendship(user.getId(), friend.getId());
        if (existingFriendship.isPresent()) {
            throw new IllegalStateException("Заявка уже отправлена или дружба уже установлена");
        }

        // Создаем заявку
        Friendship friendship = new Friendship();
        friendship.setUserId(user.getId()); // исправлено
        friendship.setFriendId(friend.getId()); // исправлено
        friendship.setStatus("PENDING");
        friendship.setRequestTime(LocalDateTime.now());

        friendships.add(friendship);

        // Добавляем в pendingRequests обоих пользователей
        user.getPendingRequests().add((long) friend.getId());
        friend.getPendingRequests().add((long) user.getId());

        userStorage.update(user);
        userStorage.update(friend);
    }

    public void confirmFriendship(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        // Проверка наличия заявки
        if (!user.getPendingRequests().contains((long) friend.getId()) ||
                !friend.getPendingRequests().contains((long) user.getId())) {
            throw new NoSuchElementException("Заявка не найдена");
        }

        // Удаляем заявки из pendingRequests
        user.getPendingRequests().remove((long) friend.getId());
        friend.getPendingRequests().remove((long) user.getId());

        // Добавляем в друзья
        user.getFriends().add((long) friend.getId());
        friend.getFriends().add((long) user.getId());

        // Обновляем пользователей в хранилище
        userStorage.update(user);
        userStorage.update(friend);

        // Обновление статуса заявок можно реализовать при необходимости.
        // Например, менять статус заявки на "CONFIRMED" в списке заявок.
        updateFriendshipStatus(user.getId(), friend.getId(), "CONFIRMED");
        updateFriendshipStatus(friend.getId(), user.getId(), "CONFIRMED");
    }

    public void removeFriend(int userId, int friendId) {
        User user = getUserByInt(userId);
        User friend = getUserByInt(friendId);

        if (!user.getFriends().contains((long)friend.getId())) {
            throw new IllegalStateException("Дружба не найдена");

        }

// Удаляем из друзей
        user.getFriends().remove((long)friend.getId());
        friend.getFriends().remove((long)user.getId());

// Обновляем пользователей в хранилище
        userStorage.update(user);
        userStorage.update(friend);
    }

    private Optional<Friendship> findFriendship(Long u1, Long u2){
        // Реализуйте поиск заявки между пользователями по хранилищу заявок.
        // Для простоты — ищем в списке friendships.
        return friendships.stream()
                .filter(f ->
                        (f.getUserId().equals(u1) && f.getFriendId().equals(u2)) ||
                                (f.getUserID().equals(u2)&& f.geTfriendID().equals(u1))
                )
                .findFirst();
    }

    private User getUserByInt(int id){
        Optional<User> optionalUser = userStorage.getByID(id);
        return optionalUser.orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }

    private User getUserByID(Long id){
        Optional<User> optionalUser = userStorage.getByID(id);
        return optionalUser.orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }

    private void updateFriendshipStatus(int u1, int u2, String status){
        for(Friendship f : friendships){
            if(f.getUserID() == u1 && f.geTfriendID() == u2){
                f.setStatus(status);
                break;
            }
        }
    }