package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    UserDbStorage userDbStorage;
    FriendshipDbStorage friendshipDbStorage;
    User user1;
    User user2;
    User user3;

    @BeforeEach
    public void setUp() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addDefaultScripts()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        friendshipDbStorage = new FriendshipDbStorage(jdbcTemplate);
        userDbStorage = new UserDbStorage(jdbcTemplate, friendshipDbStorage);

        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user1", "user1login", "user1@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user2", "user2login", "user2@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user3", "user3login", "user3@user.com", Date.valueOf("2000-01-01"));

        user1 = User.builder()
                .id(1).name("user1").login("user1login").email("user1@user.com")
                .birthday(LocalDate.of(2000, 01, 01))
                .friendIds(Collections.emptySet()).build();
        user2 = User.builder()
                .id(2).name("user2").login("user2login").email("user2@user.com")
                .birthday(LocalDate.of(2000, 01, 01))
                .friendIds(Collections.emptySet()).build();
        user3 = User.builder()
                .id(3).name("user3").login("user3login").email("user3@user.com")
                .birthday(LocalDate.of(2000, 01, 01))
                .friendIds(Collections.emptySet()).build();
    }

    @AfterEach
    public void shutDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void getUsersShouldReturnAllUsers() {
        List<User> expected = List.of(user1, user2, user3);
        List<User> actual = userDbStorage.getUsers();

        assertEquals(expected, actual);
    }

    @Test
    void getUserByIdShouldReturnUser() {
        List<User> expected = List.of(user1, user2, user3);
        List<User> actual = List.of(
                userDbStorage.getUserById(1),
                userDbStorage.getUserById(2),
                userDbStorage.getUserById(3));

        assertEquals(expected, actual);
    }

    @Test
    void addUserShouldAddNewUserToDb() {
        User user4 = User.builder().name("user4")
                .login("user4login").email("user4@user.com")
                .birthday(LocalDate.of(2000, 01, 01))
                .friendIds(Collections.emptySet()).build();

        userDbStorage.addUser(user4);
        assertEquals(user4, userDbStorage.getUserById(4));
    }

    @Test
    void updateUserShouldUpdateUser() {
        user1.setName("user1upd");
        user1.setLogin("user111");

        user2.setName("user2upd");
        user2.setLogin("user222");

        user3.setName("user3upd");
        user3.setLogin("user333");

        userDbStorage.updateUser(user1);
        userDbStorage.updateUser(user2);
        userDbStorage.updateUser(user3);

        List<User> expected = List.of(user1, user2, user3);
        List<User> actual = List.of(
                userDbStorage.getUserById(1),
                userDbStorage.getUserById(2),
                userDbStorage.getUserById(3));

        assertEquals(expected, actual);
    }

    @Test
    void deleteUserShouldDeleteUserFromDb() {
        assertEquals(List.of(user1, user2, user3), userDbStorage.getUsers());

        userDbStorage.deleteUser(1);
        assertEquals(List.of(user2, user3), userDbStorage.getUsers());

        userDbStorage.deleteUser(2);
        assertEquals(List.of(user3), userDbStorage.getUsers());

        userDbStorage.deleteUser(3);
        assertEquals(Collections.emptyList(), userDbStorage.getUsers());
    }

    @Test
    void checkUserIdShouldThrowException_ifWrongId() {
        assertDoesNotThrow(() -> userDbStorage.checkUserId(1));
        assertDoesNotThrow(() -> userDbStorage.checkUserId(2));
        assertDoesNotThrow(() -> userDbStorage.checkUserId(3));
        assertThrows(NotFoundException.class, () -> userDbStorage.checkUserId(4));
        assertThrows(NotFoundException.class, () -> userDbStorage.checkUserId(4444));
    }

    @Test
    void addFriendShouldAddFriendToUser() {
        userDbStorage.addFriend(1, 2);
        userDbStorage.addFriend(1, 3);
        userDbStorage.addFriend(2, 3);
        userDbStorage.addFriend(3, 1);

        assertEquals(Set.of(2, 3), userDbStorage.getUserById(1).getFriendIds());
        assertEquals(Set.of(3), userDbStorage.getUserById(2).getFriendIds());
        assertEquals(Set.of(1), userDbStorage.getUserById(3).getFriendIds());
    }

    @Test
    void deleteFriendShouldDeleteFriendFromUser() {
        userDbStorage.addFriend(1, 2);
        userDbStorage.addFriend(1, 3);

        assertEquals(Set.of(2, 3), userDbStorage.getUserById(1).getFriendIds());

        userDbStorage.deleteFriend(1, 2);
        assertEquals(Set.of(3), userDbStorage.getUserById(1).getFriendIds());

        userDbStorage.deleteFriend(1, 3);
        assertEquals(Collections.emptySet(), userDbStorage.getUserById(1).getFriendIds());
    }
}
