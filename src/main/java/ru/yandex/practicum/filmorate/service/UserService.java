package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    @Autowired
    public UserService(UserStorage userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public List<User> getUsers() {
        Map<Integer, Set<Integer>> friendsIds = friendshipStorage.getAllUsersFriendsIds();

        return userStorage.getUsers().stream()
                .map(user -> {
                    user.setFriendIds(new HashSet<>());

                    if (friendsIds.containsKey(user.getId())) {
                        user.setFriendIds(friendsIds.get(user.getId()));
                    }
                    return user;
                })
                .collect(Collectors.toList());

    }

    public User getUserById(int id) {
        checkUserId(id);
        User user = userStorage.getUserById(id);
        user.setFriendIds(friendshipStorage.getUserFriendsIds(id));

        return user;
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User userReturned = userStorage.addUser(user);
        friendshipStorage.updateUserFriends(user);
        userReturned.setFriendIds(friendshipStorage.getUserFriendsIds(userReturned.getId()));

        log.debug("POST request handled: new user added");
        return userReturned;
    }

    public User updateUser(User user) {
        checkUserId(user.getId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User userReturned = userStorage.updateUser(user);
        friendshipStorage.updateUserFriends(user);
        userReturned.setFriendIds(friendshipStorage.getUserFriendsIds(userReturned.getId()));

        log.debug(String.format("PUT request handled: user %d is updated", user.getId()));
        return userReturned;
    }

    public void deleteUser(int id) {
        checkUserId(id);
        userStorage.deleteUser(id);
        log.debug(String.format("DELETE request handled: user %d is deleted", id));
    }

    public void addFriend(int userId, int friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        friendshipStorage.addFriend(userId, friendId);
        log.debug(String.format("POST request handled: user %d is now friend of user %d", userId, friendId));
    }

    public void deleteFriend(int userId, int friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        friendshipStorage.deleteFriend(userId, friendId);
        log.debug(String.format("DELETE request handled: user %d is deleted from user %d friends", friendId, userId));
    }

    public List<User> getFriends(int userId) {
        checkUserId(userId);

        List<User> friends = userStorage.getUserFriends(userId);
        return collectUsersFriends(friends);
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        checkUserId(userId);
        checkUserId(otherUserId);

        List<User> commonFriends = userStorage.getCommonFriends(userId, otherUserId);
        return collectUsersFriends(commonFriends);
    }

    protected void checkUserId(int id) {
        userStorage.checkUserId(id);
    }

    private List<User> collectUsersFriends(List<User> users) {
        Map<Integer, Set<Integer>> usersFriendsIds = friendshipStorage.getUsersFriendsIds(
                users.stream()
                        .map(User::getId)
                        .collect(Collectors.toList()));

        return users.stream()
                .map(user -> {
                    user.setFriendIds(new HashSet<>());

                    if (usersFriendsIds.containsKey(user.getId())) {
                        user.setFriendIds(usersFriendsIds.get(user.getId()));
                    }
                    return user;
                }).collect(Collectors.toList());
    }
}
