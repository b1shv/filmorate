package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int id) {
        if (!userStorage.userExists(id)) {
            throw new NotFoundException(String.format("User %d is not found", id));
        }

        return userStorage.getUserById(id);
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        log.debug("POST request handled: new user added");
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (!userStorage.userExists(user.getId())) {
            throw new NotFoundException(String.format("User %d is not found", user.getId()));
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        log.debug(String.format("PUT request handled: user %d is updated", user.getId()));
        return userStorage.updateUser(user);
    }

    public void deleteUser(int id) {
        if (!userStorage.userExists(id)) {
            throw new NotFoundException(String.format("User %d is not found", id));
        }

        userStorage.deleteUser(id);
        log.debug(String.format("DELETE request handled: user %d is deleted", id));
    }

    public void addFriend(int userId, int friendId) {
        if (!userStorage.userExists(userId)) {
            throw new NotFoundException(String.format("User %d is not found", userId));
        }
        if (!userStorage.userExists(friendId)) {
            throw new NotFoundException(String.format("User %d is not found", friendId));
        }

        userStorage.addFriend(userId, friendId);
        log.debug(String.format("POST request handled: user %d is now friend of user %d", userId, friendId));
    }

    public void deleteFriend(int userId, int friendId) {
        if (!userStorage.userExists(userId)) {
            throw new NotFoundException(String.format("User %d is not found", userId));
        }
        if (!userStorage.userExists(friendId)) {
            throw new NotFoundException(String.format("User %d is not found", friendId));
        }

        userStorage.deleteFriend(userId, friendId);
        log.debug(String.format("DELETE request handled: user %d is deleted from user %d friends", friendId, userId));
    }

    public List<User> getFriends(int userId) {
        if (!userStorage.userExists(userId)) {
            throw new NotFoundException(String.format("User %d is not found", userId));
        }

        return userStorage.getUserFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        if (!userStorage.userExists(userId)) {
            throw new NotFoundException(String.format("User %d is not found", userId));
        }
        if (!userStorage.userExists(otherUserId)) {
            throw new NotFoundException(String.format("User %d is not found", otherUserId));
        }

        return userStorage.getCommonFriends(userId, otherUserId);
    }
}
