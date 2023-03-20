package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getUsers();
    User getUserById(int id);
    User addUser(User user);
    User updateUser(User user);
    void deleteUser(int id);
    void checkUserId(int id);
}
