package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

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
        checkUserId(id);
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
        checkUserId(user.getId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        log.debug(String.format("PUT request handled: user %d is updated", user.getId()));
        return userStorage.updateUser(user);
    }

    public void deleteUser(int id) {
        checkUserId(id);
        userStorage.deleteUser(id);
        log.debug(String.format("DELETE request handled: user %d is deleted", id));
    }

    public void addFriend(int userId, int friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        userStorage.getUserById(userId).addFriend(friendId);
        userStorage.getUserById(friendId).addFriend(userId);
        log.debug(String.format("POST request handled: users %d and %d are now friends", userId, friendId));
    }

    public void deleteFriend(int userid, int friendId) {
        checkUserId(userid);
        checkUserId(friendId);

        if (!userStorage.getUserById(userid).getFriendIds().contains(friendId)
                || !userStorage.getUserById(friendId).getFriendIds().contains(userid)) {
            throw new NotFoundException(String.format("Users %d and %d are not friends", userid, friendId));
        }

        userStorage.getUserById(userid).deleteFriend(friendId);
        userStorage.getUserById(friendId).deleteFriend(userid);
    }

    public List<User> getFriends(int userId) {
        return userStorage.getUserById(userId).getFriendIds().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        checkUserId(userId);
        checkUserId(otherUserId);

        return userStorage.getUserById(userId).getFriendIds().stream()
                .filter(id -> userStorage.getUserById(otherUserId).getFriendIds().contains(id))
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    protected void checkUserId(int id) {
        userStorage.checkUserId(id);
    }
}
