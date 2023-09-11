package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    FilmDbStorage filmDbStorage;
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
        filmDbStorage = new FilmDbStorage(jdbcTemplate);

        jdbcTemplate.update("insert into films (name, release_date, duration, mpa_id) values (?, ?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90, 1);
        jdbcTemplate.update("insert into films (name, release_date, duration, mpa_id) values (?, ?, ?, ?)",
                "film2", Date.valueOf("2001-01-01"), 100, 1);
        jdbcTemplate.update("insert into films (name, release_date, duration, mpa_id) values (?, ?, ?, ?)",
                "film3", Date.valueOf("2002-01-01"), 110, 1);

        film1 = Film.builder().id(1).name("film1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(90).mpa(new Mpa(1, "G")).build();

        film2 = Film.builder().id(2).name("film2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(100).mpa(new Mpa(1, "G")).build();

        film3 = Film.builder().id(3).name("film3")
                .releaseDate(LocalDate.of(2002, 1, 1))
                .duration(110).mpa(new Mpa(1, "G")).build();
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
                .releaseDate(LocalDate.of(2003, 1, 1))
                .duration(120).mpa(new Mpa(1, "G")).build();

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
    void filmExistsShouldReturnFalse_ifWrongId() {
        assertTrue(filmDbStorage.filmExists(1));
        assertTrue(filmDbStorage.filmExists(2));
        assertTrue(filmDbStorage.filmExists(3));

        assertFalse(filmDbStorage.filmExists(4));
        assertFalse(filmDbStorage.filmExists(999));
    }
}
