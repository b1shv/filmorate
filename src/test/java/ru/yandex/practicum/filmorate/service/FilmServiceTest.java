package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void filmServiceInit() {
        userService = new UserService(new InMemoryUserStorage());
        filmService = new FilmService(new InMemoryFilmStorage(), userService);
    }

    @Test
    void getMostPopularFilmsShouldReturnPopularFilms() {
        Film film1 = filmWithReleaseDate("2000-01-01");
        Film film2 = filmWithReleaseDate("2000-01-01");
        Film film3 = filmWithReleaseDate("2000-01-01");
        Film film4 = filmWithReleaseDate("2000-01-01");
        Film film5 = filmWithReleaseDate("2000-01-01");
        Film film6 = filmWithReleaseDate("2000-01-01");
        Film film7 = filmWithReleaseDate("2000-01-01");
        Film film8 = filmWithReleaseDate("2000-01-01");
        Film film9 = filmWithReleaseDate("2000-01-01");
        Film film10 = filmWithReleaseDate("2000-01-01");
        Film film11 = filmWithReleaseDate("2000-01-01");
        Film film12 = filmWithReleaseDate("2000-01-01");
        Film film13 = filmWithReleaseDate("2000-01-01");
        Film film14 = filmWithReleaseDate("2000-01-01");
        Film film15 = filmWithReleaseDate("2000-01-01");

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);
        filmService.addFilm(film4);
        filmService.addFilm(film5);
        filmService.addFilm(film6);
        filmService.addFilm(film7);
        filmService.addFilm(film8);
        filmService.addFilm(film9);
        filmService.addFilm(film10);
        filmService.addFilm(film11);
        filmService.addFilm(film12);
        filmService.addFilm(film13);
        filmService.addFilm(film14);
        filmService.addFilm(film15);

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

        film1.addLike(1);
        film2.addLike(1);
        film2.addLike(2);

        for (int i = 3; i < 10; i++) {
            Film film = filmService.getFilmById(i);

            for (int k = 1; k <= 5; k++) {
                film.addLike(k);
            }
        }

        for (int i = 11; i <= 15; i++) {
            Film film = filmService.getFilmById(i);

            for (int k = 1; k <= 3; k++) {
                film.addLike(k);
            }
        }

        assertEquals(
                List.of(film3, film4, film5, film6, film7, film8, film9, film11, film12, film13),
                filmService.getMostPopularFilms(10));

        assertEquals(
                List.of(film3, film4, film5, film6),
                filmService.getMostPopularFilms(4));

    }

    @Test
    void getFilmByIdShouldThrowException_ifWrongId() {
        Film film1 = filmWithReleaseDate("1999-08-01");
        Film film2 = filmWithReleaseDate("1999-08-01");
        Film film3 = filmWithReleaseDate("1999-08-01");

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        assertEquals(film1, filmService.getFilmById(1));
        assertEquals(film2, filmService.getFilmById(2));
        assertEquals(film3, filmService.getFilmById(3));

        assertThrows(NotFoundException.class, () -> filmService.getFilmById(4));
    }

    @Test
    void addFilmShouldThrowException_ifWrongReleaseDate() {
        Film film1 = filmWithReleaseDate("1600-01-01");
        Film film2 = filmWithReleaseDate("1895-12-27");
        Film film3 = filmWithReleaseDate("1895-12-28");

        assertThrows(ValidationException.class, () -> filmService.addFilm(film1));
        assertThrows(ValidationException.class, () -> filmService.addFilm(film2));
        Assertions.assertDoesNotThrow(() -> filmService.addFilm(film3));
    }

    @Test
    void updateFilmShouldThrowException_ifWrongReleaseDate() {
        Film film1 = filmWithReleaseDate("1999-01-01");
        Film film2 = filmWithReleaseDate("1975-11-03");
        Film film3 = filmWithReleaseDate("1895-12-28");

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        Film film1Updated = filmWithReleaseDate("2000-01-01");
        film1Updated.setId(1);

        Film film2Updated = filmWithReleaseDate("1711-02-19");
        film2Updated.setId(2);

        Film film3Updated = filmWithReleaseDate("1895-12-27");
        film3Updated.setId(3);

        Assertions.assertDoesNotThrow(() -> filmService.updateFilm(film1Updated));
        assertThrows(ValidationException.class, () -> filmService.updateFilm(film2Updated));
        assertThrows(ValidationException.class, () -> filmService.updateFilm(film3Updated));
    }

    @Test
    void updateFilmShouldThrowException_ifWrongId() {
        Film film1 = filmWithReleaseDate("2000-01-01");
        Film film2 = filmWithReleaseDate("2000-01-01");
        Film film3 = filmWithReleaseDate("2000-01-01");
        Film film4 = filmWithReleaseDate("2000-01-01");
        Film film5 = filmWithReleaseDate("2000-01-01");

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        film4.setId(4);

        assertThrows(NotFoundException.class, () -> filmService.updateFilm(film4));
        assertThrows(NotFoundException.class, () -> filmService.updateFilm(film5));
    }

    @Test
    void deleteFilmShouldThrowException_ifWrongId() {
        Film film1 = filmWithReleaseDate("2000-01-01");
        Film film2 = filmWithReleaseDate("2000-01-01");
        Film film3 = filmWithReleaseDate("2000-01-01");

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        filmService.deleteFilm(1);
        filmService.deleteFilm(3);

        assertEquals(Collections.singletonList(film2), filmService.getFilms());
        assertThrows(NotFoundException.class, () -> filmService.deleteFilm(3));
        assertThrows(NotFoundException.class, () -> filmService.deleteFilm(25));
    }

    @Test
    void addLikeShouldThrowException_ifWrongId() {
        Film film1 = filmWithReleaseDate("2000-01-01");
        Film film2 = filmWithReleaseDate("2000-01-01");
        Film film3 = filmWithReleaseDate("2000-01-01");

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        userService.addUser(new User());
        userService.addUser(new User());
        userService.addUser(new User());

        filmService.addLike(1, 1);
        filmService.addLike(2, 2);
        filmService.addLike(3, 3);
        filmService.addLike(1, 2);
        filmService.addLike(2, 3);
        filmService.addLike(3, 1);

        assertEquals(List.of(1, 2), new ArrayList<>(film1.getLikes()));
        assertEquals(List.of(2, 3), new ArrayList<>(film2.getLikes()));
        assertEquals(List.of(1, 3), new ArrayList<>(film3.getLikes()));

        assertThrows(NotFoundException.class, () -> filmService.addLike(4, 1));
        assertThrows(NotFoundException.class, () -> filmService.addLike(1, 4));
        assertThrows(NotFoundException.class, () -> filmService.addLike(4, 4));
    }

    @Test
    void deleteLikeShouldThrowException_ifWrongId() {
        Film film1 = filmWithReleaseDate("2000-01-01");
        Film film2 = filmWithReleaseDate("2000-01-01");
        Film film3 = filmWithReleaseDate("2000-01-01");

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        userService.addUser(new User());
        userService.addUser(new User());
        userService.addUser(new User());

        filmService.addLike(1, 1);
        filmService.addLike(2, 2);
        filmService.addLike(3, 3);
        filmService.addLike(1, 2);
        filmService.addLike(2, 3);
        filmService.addLike(3, 1);

        filmService.deleteLike(1, 1);
        filmService.deleteLike(2, 2);
        filmService.deleteLike(3, 3);

        assertEquals(Collections.singletonList(2), new ArrayList<>(film1.getLikes()));
        assertEquals(Collections.singletonList(3), new ArrayList<>(film2.getLikes()));
        assertEquals(Collections.singletonList(1), new ArrayList<>(film3.getLikes()));

        assertThrows(NotFoundException.class, () -> filmService.deleteLike(1, 1));
        assertThrows(NotFoundException.class, () -> filmService.deleteLike(1, 5));
        assertThrows(NotFoundException.class, () -> filmService.deleteLike(4, 1));
    }

    private Film filmWithReleaseDate(String date) {
        LocalDate releaseDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Film film = new Film();

        film.setReleaseDate(releaseDate);

        return film;
    }
}
