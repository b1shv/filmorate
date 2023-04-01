package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    FilmDbStorage filmDbStorage;
    MpaDbStorage mpaDbStorage;
    GenreDbStorage genreDbStorage;
    LikesDbStorage likesDbStorage;
    Film film1;
    Film film2;
    Film film3;

    @BeforeEach
    public void setUp() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addDefaultScripts()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        mpaDbStorage = new MpaDbStorage(jdbcTemplate);
        genreDbStorage = new GenreDbStorage(jdbcTemplate);
        likesDbStorage = new LikesDbStorage(jdbcTemplate);
        filmDbStorage = new FilmDbStorage(jdbcTemplate, mpaDbStorage, genreDbStorage, likesDbStorage);

        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film2", Date.valueOf("2001-01-01"), 100);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film3", Date.valueOf("2002-01-01"), 110);

        film1 = Film.builder().id(1).name("film1")
                .releaseDate(LocalDate.of(2000, 01, 01))
                .genres(Collections.emptyList()).likes(Collections.emptySet())
                .duration(90).build();

        film2 = Film.builder().id(2).name("film2")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .genres(Collections.emptyList()).likes(Collections.emptySet())
                .duration(100).build();

        film3 = Film.builder().id(3).name("film3")
                .releaseDate(LocalDate.of(2002, 01, 01))
                .genres(Collections.emptyList()).likes(Collections.emptySet())
                .duration(110).build();
    }

    @AfterEach
    public void shutDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void getFilmsShouldReturnAllFilms() {
        List<Film> expected = List.of(film1, film2, film3);
        List<Film> actual = filmDbStorage.getFilms();

        assertEquals(expected, actual);
    }

    @Test
    void getFilmByIdShouldReturnFilm() {
        List<Film> expected = List.of(film1, film2, film3);
        List<Film> actual = List.of(
                filmDbStorage.getFilmById(1),
                filmDbStorage.getFilmById(2),
                filmDbStorage.getFilmById(3));

        assertEquals(expected, actual);
    }

    @Test
    void addFilmShouldAddNewFilmToDb() {
        Film film4 = Film.builder().name("film4")
                .releaseDate(LocalDate.of(2003, 01, 01))
                .genres(Collections.emptyList()).likes(Collections.emptySet())
                .duration(120).build();

        filmDbStorage.addFilm(film4);
        assertEquals(film4, filmDbStorage.getFilmById(4));
    }

    @Test
    void updateFilmShouldUpdateFilmInDb() {
        film1.setName("film1Upd");
        film1.setDuration(30);

        film2.setName("film2Upd");
        film2.setDuration(40);

        film3.setName("film3Upd");
        film3.setDuration(50);

        filmDbStorage.updateFilm(film1);
        filmDbStorage.updateFilm(film2);
        filmDbStorage.updateFilm(film3);

        List<Film> expected = List.of(film1, film2, film3);
        List<Film> actual = List.of(
                filmDbStorage.getFilmById(1),
                filmDbStorage.getFilmById(2),
                filmDbStorage.getFilmById(3));

        assertEquals(expected, actual);
    }

    @Test
    void deleteFilmShouldDeleteFilmFromDb() {
        assertEquals(List.of(film1, film2, film3), filmDbStorage.getFilms());

        filmDbStorage.deleteFilm(1);
        assertEquals(List.of(film2, film3), filmDbStorage.getFilms());

        filmDbStorage.deleteFilm(2);
        assertEquals(List.of(film3), filmDbStorage.getFilms());

        filmDbStorage.deleteFilm(3);
        assertEquals(Collections.emptyList(), filmDbStorage.getFilms());
    }

    @Test
    void checkFilmIdShouldThrowException_ifWrongId() {
        assertDoesNotThrow(() -> filmDbStorage.checkFilmId(1));
        assertDoesNotThrow(() -> filmDbStorage.checkFilmId(3));
        assertThrows(NotFoundException.class, () -> filmDbStorage.checkFilmId(4));
        assertThrows(NotFoundException.class, () -> filmDbStorage.checkFilmId(444));
    }

    @Test
    void addLikeShouldAddLikeToFilm() {
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user1", "user1login", "user1@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user2", "user2login", "user2@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user3", "user3login", "user3@user.com", Date.valueOf("2000-01-01"));

        filmDbStorage.addLike(1, 1);
        filmDbStorage.addLike(1, 2);
        filmDbStorage.addLike(1, 3);

        Set<Integer> expected = Set.of(1, 2, 3);
        Set<Integer> actual = filmDbStorage.getFilmById(1).getLikes();

        assertEquals(expected, actual);
    }

    @Test
    void deleteLikeShouldDeleteLikeFromFilm() {
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user1", "user1login", "user1@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user2", "user2login", "user2@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user3", "user3login", "user3@user.com", Date.valueOf("2000-01-01"));

        filmDbStorage.addLike(1, 1);
        filmDbStorage.addLike(1, 2);
        filmDbStorage.addLike(1, 3);

        filmDbStorage.deleteLike(1, 1);
        assertEquals(Set.of(2, 3), filmDbStorage.getFilmById(1).getLikes());

        filmDbStorage.deleteLike(1, 2);
        assertEquals(Set.of(3), filmDbStorage.getFilmById(1).getLikes());

        filmDbStorage.deleteLike(1, 3);
        assertEquals(Collections.emptySet(), filmDbStorage.getFilmById(1).getLikes());
    }
}
