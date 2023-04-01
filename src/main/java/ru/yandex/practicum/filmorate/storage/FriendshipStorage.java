package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

public interface FriendshipStorage {
    Set<Integer> getFiendsIds(int userId);

    void updateUserFriends(User user);

    void deleteUserFriends(int userId);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);
}
