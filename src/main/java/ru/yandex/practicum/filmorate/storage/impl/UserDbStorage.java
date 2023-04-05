package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeUser(rs), id);
    }

    @Override
    public User addUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> userValues = new HashMap<>();
        userValues.put("name", user.getName());
        userValues.put("email", user.getEmail());
        userValues.put("login", user.getLogin());
        userValues.put("birthday", Date.valueOf(user.getBirthday()));

        user.setId(simpleJdbcInsert.executeAndReturnKey(userValues).intValue());

        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users " +
                "SET name = ?, email = ?, login = ?, birthday = ? " +
                "WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getEmail(),
                user.getLogin(), Date.valueOf(user.getBirthday()), user.getId());

        return user;
    }

    @Override
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<User> getUserFriends(int userId) {
        String sql = "SELECT u.* " +
                "FROM friendship AS f " +
                "JOIN users AS u ON f.friend_id = u.user_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherUserId) {
        String sql = "SELECT * " +
                "FROM users " +
                "WHERE user_id in(" +
                "SELECT friend_id " +
                "FROM friendship " +
                "WHERE user_id = ? OR user_id = ? " +
                "GROUP BY friend_id " +
                "HAVING COUNT(friend_id) > 1)";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), userId, otherUserId);
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

    @Override
    public boolean userExists(int id) {
        String sql = "SELECT user_id FROM users WHERE user_id = ?";

        try {
            jdbcTemplate.queryForObject(sql, Integer.class, id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
