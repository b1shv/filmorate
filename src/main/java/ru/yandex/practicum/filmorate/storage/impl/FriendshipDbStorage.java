package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.Set;

@Component
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Set<Integer> getFiendsIds(int userId) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ?";
        return Set.copyOf(jdbcTemplate.queryForList(sql, Integer.class, userId));
    }

    @Override
    public void updateUserFriends(User user) {
        deleteUserFriends(user.getId());

        if (user.getFriendIds() != null) {
            String insertLikeSql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";

            for (Integer friendId : user.getFriendIds()) {
                jdbcTemplate.update(insertLikeSql, user.getId(), friendId);
            }
        }
    }

    @Override
    public void deleteUserFriends(int userId) {
        String sql = "DELETE FROM friendship WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        Integer friendCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
                Integer.class, userId, friendId);

        if (friendCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)", userId, friendId);
        }
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }
}
