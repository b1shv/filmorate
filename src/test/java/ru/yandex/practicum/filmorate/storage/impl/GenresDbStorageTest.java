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
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenresDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    GenreDbStorage genreDbStorage;
    final Genre comedy = new Genre(1, "Комедия");
    final Genre drama = new Genre(2, "Драма");
    final Genre animation = new Genre(3, "Мультфильм");
    final Genre thriller = new Genre(4, "Триллер");
    final Genre documentary = new Genre(5, "Документальный");
    final Genre action = new Genre(6, "Боевик");

    @BeforeEach
    public void setUp() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addDefaultScripts()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        genreDbStorage = new GenreDbStorage(jdbcTemplate);
    }

    @AfterEach
    public void shutDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void getGenresShouldReturnAllGenres() {
        List<Genre> expected = List.of(comedy, drama, animation, thriller, documentary, action);
        List<Genre> actual = genreDbStorage.getGenres();

        assertEquals(expected, actual);
    }

    @Test
    void getGenreByIdShouldReturnGenreOrThrowException() {
        assertEquals(comedy, genreDbStorage.getGenreById(1));
        assertEquals(thriller, genreDbStorage.getGenreById(4));
        assertEquals(action, genreDbStorage.getGenreById(6));

        assertThrows(NotFoundException.class, () -> genreDbStorage.getGenreById(7));
        assertThrows(NotFoundException.class, () -> genreDbStorage.getGenreById(999));
    }

    @Test
    void getGenresByFilmIdShouldReturnGenresOrEmptyList() {
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film2", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film3", Date.valueOf("2000-01-01"), 90);

        jdbcTemplate.update("insert into films_genres (film_id, genre_id) values (?, ?)",
                1, 2);
        jdbcTemplate.update("insert into films_genres (film_id, genre_id) values (?, ?)",
                1, 6);
        jdbcTemplate.update("insert into films_genres (film_id, genre_id) values (?, ?)",
                3, 1);

        assertEquals(List.of(drama, action), genreDbStorage.getGenresByFilmId(1));
        assertEquals(List.of(comedy), genreDbStorage.getGenresByFilmId(3));
        assertEquals(Collections.emptyList(), genreDbStorage.getGenresByFilmId(2));
    }

    @Test
    void updateFilmGenresShouldChangeFilmGenres() {
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films_genres (film_id, genre_id) values (?, ?)",
                1, 2);

        Film film = Film.builder().id(1).build();
        genreDbStorage.updateFilmGenres(film);

        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from films_genres where film_id = ?",
                Integer.class, 1));

        film.setGenres(List.of(comedy, documentary));
        genreDbStorage.updateFilmGenres(film);

        List<Integer> expected = List.of(1, 5);
        List<Integer> actual = jdbcTemplate.queryForList(
                "select genre_id from films_genres where film_id = ?", Integer.class, 1);
    }

    @Test
    void deleteFilmGenresShouldRemoveFilmGenres() {
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90);

        jdbcTemplate.update("insert into films_genres (film_id, genre_id) values (?, ?)",
                1, 2);
        jdbcTemplate.update("insert into films_genres (film_id, genre_id) values (?, ?)",
                1, 6);

        List<Integer> expected = List.of(2, 6);
        List<Integer> actual = jdbcTemplate.queryForList(
                "select genre_id from films_genres where film_id = ?", Integer.class, 1);

        assertEquals(expected, actual);

        genreDbStorage.deleteFilmGenres(1);

        List<Integer> expectedAfterDelete = Collections.EMPTY_LIST;
        List<Integer> actualAfterDelete = jdbcTemplate.queryForList(
                "select genre_id from films_genres where film_id = ?", Integer.class, 1);

        assertEquals(expectedAfterDelete, actualAfterDelete);
    }
}
