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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    UserDbStorage userDbStorage;
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
        userDbStorage = new UserDbStorage(jdbcTemplate);

        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user1", "user1login", "user1@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user2", "user2login", "user2@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user3", "user3login", "user3@user.com", Date.valueOf("2000-01-01"));

        user1 = User.builder()
                .id(1).name("user1").login("user1login").email("user1@user.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        user2 = User.builder()
                .id(2).name("user2").login("user2login").email("user2@user.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        user3 = User.builder()
                .id(3).name("user3").login("user3login").email("user3@user.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
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
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

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
    void getUserFriendsShouldReturnUserFriends() {
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user4", "user4login", "user4@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user5", "user5login", "user5@user.com", Date.valueOf("2000-01-01"));

        User user4 = User.builder()
                .id(4).name("user4").login("user4login").email("user4@user.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user5 = User.builder()
                .id(5).name("user5").login("user5login").email("user5@user.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        jdbcTemplate.update("insert into friendship values(?, ?)", 1, 3);
        jdbcTemplate.update("insert into friendship values(?, ?)", 1, 4);
        jdbcTemplate.update("insert into friendship values(?, ?)", 1, 5);
        jdbcTemplate.update("insert into friendship values(?, ?)", 3, 2);
        jdbcTemplate.update("insert into friendship values(?, ?)", 3, 4);

        assertEquals(List.of(user3, user4, user5), userDbStorage.getUserFriends(1));
        assertEquals(List.of(user2, user4), userDbStorage.getUserFriends(3));
        assertEquals(Collections.emptyList(), userDbStorage.getUserFriends(4));
    }

    @Test
    void getCommonFriendsShouldReturnCommonFriends() {
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user4", "user4login", "user4@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user5", "user5login", "user5@user.com", Date.valueOf("2000-01-01"));

        User user5 = User.builder()
                .id(5).name("user5").login("user5login").email("user5@user.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        jdbcTemplate.update("insert into friendship values(?, ?)", 1, 3);
        jdbcTemplate.update("insert into friendship values(?, ?)", 1, 4);
        jdbcTemplate.update("insert into friendship values(?, ?)", 1, 5);
        jdbcTemplate.update("insert into friendship values(?, ?)", 2, 3);
        jdbcTemplate.update("insert into friendship values(?, ?)", 2, 1);
        jdbcTemplate.update("insert into friendship values(?, ?)", 2, 5);

        List<User> expected = List.of(user3, user5);
        List<User> actual = userDbStorage.getCommonFriends(1, 2);

        assertEquals(expected, actual);
    }

    @Test
    void userExistsShouldReturnFalse_ifWrongId() {
        assertTrue(userDbStorage.userExists(1));
        assertTrue(userDbStorage.userExists(2));
        assertTrue(userDbStorage.userExists(3));

        assertFalse(userDbStorage.userExists(4));
        assertFalse(userDbStorage.userExists(4444));
    }
}
