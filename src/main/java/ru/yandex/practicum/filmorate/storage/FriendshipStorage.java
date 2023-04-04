package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FriendshipStorage {
    Map<Integer, Set<Integer>> getAllUsersFriendsIds();

    Set<Integer> getUserFriendsIds(int userId);

    Map<Integer, Set<Integer>> getUsersFriendsIds(List<Integer> usersIds);

    void updateUserFriends(User user);

    void deleteUserFriends(int userId);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);
}
