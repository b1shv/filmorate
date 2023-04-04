package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Integer, Set<Integer>> getAllUsersFriendsIds() {
        String sql = "SELECT * FROM friendship";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);

        return usersFriendsIds(rs);
    }

    @Override
    public Set<Integer> getUserFriendsIds(int userId) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ?";
        return Set.copyOf(jdbcTemplate.queryForList(sql, Integer.class, userId));
    }

    @Override
    public Map<Integer, Set<Integer>> getUsersFriendsIds(List<Integer> usersIds) {
        String inSql = String.join(",", Collections.nCopies(usersIds.size(), "?"));
        SqlRowSet rs = jdbcTemplate.queryForRowSet(
                String.format("SELECT * FROM friendship WHERE user_id IN (%s)", inSql), usersIds.toArray());

        return usersFriendsIds(rs);
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
        try {
            jdbcTemplate.update(
                    "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)", userId, friendId);
        } catch (DuplicateKeyException e) {
            throw new ValidationException(
                    String.format("User %d is already a friend of user %d", friendId, userId));
        }
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    private Map<Integer, Set<Integer>> usersFriendsIds(SqlRowSet rs) {
        Map<Integer, Set<Integer>> usersFriends = new HashMap<>();

        while (rs.next()) {
            int userId = rs.getInt("user_id");
            int friendId = rs.getInt("friend_id");

            if (!usersFriends.containsKey(userId)) {
                Set<Integer> friendIds = new HashSet<>();
                friendIds.add(friendId);

                usersFriends.put(userId, friendIds);
            } else {
                usersFriends.get(userId).add(friendId);
            }
        }

        return usersFriends;
    }
}
