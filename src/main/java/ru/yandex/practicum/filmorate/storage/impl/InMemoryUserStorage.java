package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 0;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }

    @Override
    public User addUser(User user) {
        user.setId(++idCounter);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    @Override
    public void checkUserId(int id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(String.format("User %d is not found", id));
        }
    }

    @Override
    public void addFriend(int userId, int friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        users.get(userId).addFriend(friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        users.get(userId).deleteFriend(friendId);
    }
}
