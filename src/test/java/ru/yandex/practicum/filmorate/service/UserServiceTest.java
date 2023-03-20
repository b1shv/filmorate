package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void userServiceInit() {
        userService = new UserService(new InMemoryUserStorage());
    }

    @Test
    void addUserShouldMakeNameFromLogin_ifNameEmpty() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        user1.setName("Petya");
        user1.setLogin("petya74");

        user2.setLogin("kolyan11");
        user3.setLogin("grisha38");

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);

        List<String> expected = List.of("Petya", user2.getLogin(), user3.getLogin());
        List<String> actual = List.of(user1.getName(), user2.getName(), user3.getName());

        assertEquals(expected, actual);
    }

    @Test
    void getUserByIdShouldThrowException_ifWrongId() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);

        List<User> expected = List.of(user1, user2, user3);
        List<User> actual = List.of(
                userService.getUserById(1),
                userService.getUserById(2),
                userService.getUserById(3));

        assertEquals(expected, actual);
        assertThrows(NotFoundException.class, () -> userService.getUserById(4));
    }

    @Test
    void updateUserShouldThrowException_ifWrongId() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        User user4 = new User();
        user4.setId(4);

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);

        User user1Updated = new User();
        user1Updated.setId(1);
        user1Updated.setName("Vasya");

        userService.updateUser(user1Updated);

        assertEquals(user1Updated, userService.getUserById(1));
        assertThrows(NotFoundException.class, () -> userService.updateUser(user4));
    }

    @Test
    void deleteUserShouldThrowException_ifWrongId() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        User user4 = new User();
        User user5 = new User();
        User user6 = new User();
        User user7 = new User();

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);
        userService.addUser(user4);
        userService.addUser(user5);
        userService.addUser(user6);
        userService.addUser(user7);

        assertEquals(List.of(user1, user2, user3, user4, user5, user6, user7), userService.getUsers());

        userService.deleteUser(1);
        userService.deleteUser(3);
        userService.deleteUser(7);

        assertEquals(List.of(user2, user4, user5, user6), userService.getUsers());
        assertThrows(NotFoundException.class, () -> userService.deleteUser(8));
    }

    @Test
    void addFriendShouldAddFriendsToBothUsers() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        User user4 = new User();
        User user5 = new User();
        User user6 = new User();
        User user7 = new User();

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);
        userService.addUser(user4);
        userService.addUser(user5);
        userService.addUser(user6);
        userService.addUser(user7);

        userService.addFriend(1, 2);
        userService.addFriend(1, 3);
        userService.addFriend(1, 4);
        userService.addFriend(1, 5);
        userService.addFriend(1, 6);
        userService.addFriend(1, 7);
        userService.addFriend(2, 4);
        userService.addFriend(2, 7);

        assertEquals(List.of(user2, user3, user4, user5, user6, user7), userService.getFriends(1));
        assertEquals(List.of(user1, user4, user7), userService.getFriends(2));
        assertEquals(List.of(user1, user2), userService.getFriends(4));
        assertEquals(List.of(user1, user2), userService.getFriends(7));

        assertEquals(Collections.singletonList(user1), userService.getFriends(3));
        assertEquals(Collections.singletonList(user1), userService.getFriends(5));
        assertEquals(Collections.singletonList(user1), userService.getFriends(6));

        assertThrows(NotFoundException.class, () -> userService.addFriend(1, 8));
        assertThrows(NotFoundException.class, () -> userService.addFriend(9, 4));
        assertThrows(NotFoundException.class, () -> userService.addFriend(9, 10));
    }

    @Test
    void deleteFriendShouldRemoveFriendsFromBothUsers() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        User user4 = new User();
        User user5 = new User();

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);
        userService.addUser(user4);
        userService.addUser(user5);

        userService.addFriend(1, 2);
        userService.addFriend(1, 3);
        userService.addFriend(3, 2);
        userService.addFriend(1, 5);
        userService.addFriend(3, 4);
        userService.addFriend(2, 5);
        userService.addFriend(2, 4);

        userService.deleteFriend(1, 3);
        userService.deleteFriend(2, 3);
        userService.deleteFriend(4, 2);
        userService.deleteFriend(5, 2);

        assertEquals(List.of(user2, user5), userService.getFriends(1));
        assertEquals(Collections.singletonList(user1), userService.getFriends(2));
        assertEquals(Collections.singletonList(user4), userService.getFriends(3));
        assertEquals(Collections.singletonList(user3), userService.getFriends(4));
        assertEquals(Collections.singletonList(user1), userService.getFriends(5));

        assertThrows(NotFoundException.class, () -> userService.deleteFriend(1, 3));
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(2, 3));
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(1, 7));
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(6, 2));
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(6, 10));
    }

    @Test
    void getCommonFriendsShouldReturnCommonFriends() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        User user4 = new User();
        User user5 = new User();

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);
        userService.addUser(user4);
        userService.addUser(user5);

        userService.addFriend(1, 3);
        userService.addFriend(1, 4);
        userService.addFriend(1, 5);
        userService.addFriend(2, 3);
        userService.addFriend(2, 5);

        assertEquals(List.of(user3, user5), userService.getCommonFriends(1, 2));
        assertEquals(List.of(user1, user2), userService.getCommonFriends(3, 5));

        assertThrows(NotFoundException.class, () -> userService.getCommonFriends(1, 8));
        assertThrows(NotFoundException.class, () -> userService.getCommonFriends(8, 1));
        assertThrows(NotFoundException.class, () -> userService.getCommonFriends(8, 9));
    }
}
