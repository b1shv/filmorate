package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FriendshipDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    FriendshipDbStorage friendshipDbStorage;
    final String sqlInsert = "insert into friendship (user_id, friend_id) values (?, ?)";
    final String sqlSelect = "select friend_id from friendship where user_id = ?";

    @BeforeEach
    public void setUp() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addDefaultScripts()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        friendshipDbStorage = new FriendshipDbStorage(jdbcTemplate);

        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user1", "user1login", "user1@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user2", "user2login", "user2@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user3", "user3login", "user3@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user4", "user4login", "user4@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user5", "user5login", "user5@user.com", Date.valueOf("2000-01-01"));
    }

    @AfterEach
    public void shutDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void getAllUsersFriendsIdsShouldReturnAllUsersFriendsIds() {
        jdbcTemplate.update(sqlInsert, 1, 2);
        jdbcTemplate.update(sqlInsert, 1, 4);
        jdbcTemplate.update(sqlInsert, 1, 5);
        jdbcTemplate.update(sqlInsert, 3, 2);
        jdbcTemplate.update(sqlInsert, 3, 4);
        jdbcTemplate.update(sqlInsert, 5, 2);

        Map<Integer, Set<Integer>> expected = new HashMap<>();
        expected.put(1, Set.of(2, 4, 5));
        expected.put(3, Set.of(2, 4));
        expected.put(5, Set.of(2));

        Map<Integer, Set<Integer>> actual = friendshipDbStorage.getAllUsersFriendsIds();

        assertEquals(expected, actual);
    }

    @Test
    void getUserFiendsIdsShouldReturnFriendsIds() {
        jdbcTemplate.update(sqlInsert, 1, 2);
        jdbcTemplate.update(sqlInsert, 1, 4);
        jdbcTemplate.update(sqlInsert, 1, 5);
        jdbcTemplate.update(sqlInsert, 3, 2);
        jdbcTemplate.update(sqlInsert, 3, 4);
        jdbcTemplate.update(sqlInsert, 5, 2);

        assertEquals(Set.of(2, 4, 5), friendshipDbStorage.getUserFriendsIds(1));
        assertEquals(Set.of(2, 4), friendshipDbStorage.getUserFriendsIds(3));
        assertEquals(Set.of(2), friendshipDbStorage.getUserFriendsIds(5));
    }

    @Test
    void getUsersFriendsIdsShouldReturnUsersFriends() {
        jdbcTemplate.update(sqlInsert, 2, 1);
        jdbcTemplate.update(sqlInsert, 2, 5);
        jdbcTemplate.update(sqlInsert, 2, 4);
        jdbcTemplate.update(sqlInsert, 3, 1);
        jdbcTemplate.update(sqlInsert, 3, 5);
        jdbcTemplate.update(sqlInsert, 5, 1);
        jdbcTemplate.update(sqlInsert, 5, 4);

        Map<Integer, Set<Integer>> expected = new HashMap<>();
        expected.put(2, Set.of(1, 5, 4));
        expected.put(3, Set.of(1, 5));
        expected.put(5, Set.of(1, 4));

        Map<Integer, Set<Integer>> actual = friendshipDbStorage.getUsersFriendsIds(List.of(2, 3, 5));

        assertEquals(expected, actual);
    }

    @Test
    void updateUserFriendsShouldUpdateUserFriends() {
        User user = User.builder().id(1).name("user1").login("user1login").email("user1@user.com")
                .birthday(LocalDate.of(2000, 1, 1)).build();

        user.setFriendIds(Set.of(3, 4, 5));

        friendshipDbStorage.updateUserFriends(user);
        assertEquals(Set.of(3, 4, 5), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));

        user.setFriendIds(Set.of(2, 4));
        friendshipDbStorage.updateUserFriends(user);
        assertEquals(Set.of(2, 4), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));

        user.setFriendIds(Collections.emptySet());
        friendshipDbStorage.updateUserFriends(user);
        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
    }

    @Test
    void deleteUserFriendsShouldRemoveAllUserFriends() {
        jdbcTemplate.update(sqlInsert, 1, 2);
        jdbcTemplate.update(sqlInsert, 1, 5);
        jdbcTemplate.update(sqlInsert, 3, 5);
        jdbcTemplate.update(sqlInsert, 3, 4);
        jdbcTemplate.update(sqlInsert, 5, 1);
        jdbcTemplate.update(sqlInsert, 5, 4);

        assertEquals(Set.of(2, 5), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
        assertEquals(Set.of(5, 4), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 3)));
        assertEquals(Set.of(1, 4), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 5)));

        friendshipDbStorage.deleteUserFriends(1);
        friendshipDbStorage.deleteUserFriends(3);
        friendshipDbStorage.deleteUserFriends(5);

        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 3)));
        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 5)));
    }

    @Test
    void addFriendShouldAddFriendToUser() {
        friendshipDbStorage.addFriend(1, 3);
        friendshipDbStorage.addFriend(1, 5);
        friendshipDbStorage.addFriend(2, 4);
        friendshipDbStorage.addFriend(3, 5);

        assertEquals(Set.of(3, 5), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
        assertEquals(Set.of(4), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));
        assertEquals(Set.of(5), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 3)));
    }

    @Test
    void deleteFriendShouldRemoveFriendFromUser() {
        jdbcTemplate.update(sqlInsert, 2, 3);
        jdbcTemplate.update(sqlInsert, 2, 4);
        jdbcTemplate.update(sqlInsert, 2, 5);

        assertEquals(Set.of(3, 4, 5), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));

        friendshipDbStorage.deleteFriend(2, 3);
        assertEquals(Set.of(4, 5), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));

        friendshipDbStorage.deleteFriend(2, 4);
        assertEquals(Set.of(5), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));

        friendshipDbStorage.deleteFriend(2, 5);
        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));
    }
}
