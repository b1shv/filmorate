package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
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
    private final FriendshipStorage friendshipStorage;

    public UserDbStorage(JdbcTemplate jdbcTemplate, FriendshipStorage friendshipStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.friendshipStorage = friendshipStorage;
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
        friendshipStorage.updateUserFriends(user);

        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users " +
                "SET name = ?, email = ?, login = ?, birthday = ? " +
                "WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getEmail(),
                user.getLogin(), Date.valueOf(user.getBirthday()), user.getId());

        friendshipStorage.updateUserFriends(user);
        return user;
    }

    @Override
    public void deleteUser(int id) {
        friendshipStorage.deleteUserFriends(id);

        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void checkUserId(int id) {
        String sql = "SELECT user_id FROM users WHERE user_id = ?";

        try {
            jdbcTemplate.queryForObject(sql, Integer.class, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("User %d is not found", id));
        }
    }

    @Override
    public void addFriend(int userId, int friendId) {
        friendshipStorage.addFriend(userId, friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        friendshipStorage.deleteFriend(userId, friendId);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .friendIds(friendshipStorage.getFiendsIds(rs.getInt("user_id")))
                .build();
    }
}
