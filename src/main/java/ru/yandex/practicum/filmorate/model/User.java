package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<User> friends = new HashSet<>();

    @Override
    public String toString() {
        return "User(id=" + id +
                ", email=" + email +
                ", login=" + login +
                ", name=" + name +
                ", birthday=" + birthday +
                ", friendsCount=" + (friends == null ? 0 : friends.size()) +
                ")";
    }
}