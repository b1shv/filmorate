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
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MpaDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    MpaDbStorage mpaDbStorage;
    private final static Mpa G = new Mpa(1, "G");
    private final static Mpa PG = new Mpa(2, "PG");
    private final static Mpa PG13 = new Mpa(3, "PG-13");
    private final static Mpa R = new Mpa(4, "R");
    private final static Mpa NC17 = new Mpa(5, "NC-17");

    @BeforeEach
    public void setUp() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addDefaultScripts()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        mpaDbStorage = new MpaDbStorage(jdbcTemplate);
    }

    @AfterEach
    public void shutDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void getAllMpaShouldReturnAllMpa() {
        List<Mpa> expected = List.of(G, PG, PG13, R, NC17);
        List<Mpa> actual = mpaDbStorage.getAllMpa();

        assertEquals(expected, actual);
    }

    @Test
    void getMpaByIdShouldReturnMpaOrThrowException() {
        List<Mpa> expected = List.of(G, NC17);
        List<Mpa> actual = List.of(mpaDbStorage.getMpaById(1), mpaDbStorage.getMpaById(5));

        assertEquals(expected, actual);
        assertThrows(NotFoundException.class, () -> mpaDbStorage.getMpaById(6));
    }

    @Test
    void getMpaByFilmIdShouldReturnMpaOrNull() {
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film2", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film3", Date.valueOf("2000-01-01"), 90);

        jdbcTemplate.update("insert into films_mpa (film_id, mpa_id) values (?, ?)", 1, 5);
        jdbcTemplate.update("insert into films_mpa (film_id, mpa_id) values (?, ?)", 2, 3);
        jdbcTemplate.update("insert into films_mpa (film_id, mpa_id) values (?, ?)", 3, 1);

        List<Mpa> expected = List.of(NC17, PG13, G);
        List<Mpa> actual = List.of(
                mpaDbStorage.getMpaByFilmId(1),
                mpaDbStorage.getMpaByFilmId(2),
                mpaDbStorage.getMpaByFilmId(3));

        assertEquals(expected, actual);
        assertNull(mpaDbStorage.getMpaByFilmId(4));
        assertNull(mpaDbStorage.getMpaByFilmId(1111));
    }

    @Test
    void updateFilmMpaShouldChangeFilmMpa() {
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films_mpa (film_id, mpa_id) values (?, ?)", 1, 5);

        Film film = Film.builder().id(1).build();
        mpaDbStorage.updateFilmMpa(film);

        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from films_mpa where film_id = ?",
                Integer.class, 1));

        film.setMpa(R);
        mpaDbStorage.updateFilmMpa(film);

        assertEquals(4, jdbcTemplate.queryForObject("select mpa_id from films_mpa where film_id = ?",
                Integer.class, 1));

    }

    @Test
    void deleteFilmMpaShouldRemoveMpa() {
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film2", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film3", Date.valueOf("2000-01-01"), 90);

        jdbcTemplate.update("insert into films_mpa (film_id, mpa_id) values (?, ?)", 1, 5);
        jdbcTemplate.update("insert into films_mpa (film_id, mpa_id) values (?, ?)", 2, 1);
        jdbcTemplate.update("insert into films_mpa (film_id, mpa_id) values (?, ?)", 3, 2);

        assertEquals(3, jdbcTemplate.queryForObject("select count(*) from films_mpa", Integer.class));

        mpaDbStorage.deleteFilmMpa(1);

        assertEquals(2, jdbcTemplate.queryForObject("select count(*) from films_mpa", Integer.class));

        mpaDbStorage.deleteFilmMpa(2);
        mpaDbStorage.deleteFilmMpa(3);
        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from films_mpa", Integer.class));
    }
}
