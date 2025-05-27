package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage; // Хранилище пользователей

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // Получение пользователя по id
    public User getUserById(Long id) {
        return userStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }

    // Создание пользователя
    public User addUser(User user) {
        validateUser(user);
        return userStorage.add(user);
    }

    // Обновление пользователя
    public Optional<User> updateUser(User user) {
        validateUser(user);
        return userStorage.update(user);
    }

    // Получение всех пользователей
    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    // Удаление пользователя
    public void deleteUser(Long id) {
        userStorage.delete(id);
    }

    // Валидация данных
    private void validateUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Некорректная дата рождения");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}