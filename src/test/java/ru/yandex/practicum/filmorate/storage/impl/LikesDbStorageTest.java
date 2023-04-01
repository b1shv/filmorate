package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LikesDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    LikesDbStorage likesDbStorage;
    final String sqlInsert = "insert into films_likes (film_id, user_id) values (?, ?)";
    final String sqlSelect = "select user_id from films_likes where film_id = ?";

    @BeforeEach
    public void setUp() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addDefaultScripts()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        likesDbStorage = new LikesDbStorage(jdbcTemplate);

        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film1", Date.valueOf("2000-01-01"), 90);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film2", Date.valueOf("2001-01-01"), 100);
        jdbcTemplate.update("insert into films (name, release_date, duration) values (?, ?, ?)",
                "film3", Date.valueOf("2002-01-01"), 110);

        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user1", "user1login", "user1@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user2", "user2login", "user2@user.com", Date.valueOf("2000-01-01"));
        jdbcTemplate.update("insert into users (name, login, email, birthday) values (?, ?, ?, ?)",
                "user3", "user3login", "user3@user.com", Date.valueOf("2000-01-01"));
    }

    @AfterEach
    public void shutDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void getLikesByFilmIdShouldReturnFilmLikes() {
        jdbcTemplate.update(sqlInsert, 1, 1);
        jdbcTemplate.update(sqlInsert, 1, 3);
        jdbcTemplate.update(sqlInsert, 3, 2);

        assertEquals(Set.of(1, 3), likesDbStorage.getLikesByFilmId(1));
        assertEquals(Set.of(2), likesDbStorage.getLikesByFilmId(3));
        assertEquals(Collections.emptySet(), likesDbStorage.getLikesByFilmId(2));
    }

    @Test
    void updateFilmLikesShouldUpdateFilmLikes() {
        Film film1 = Film.builder().id(1).name("film1")
                .releaseDate(LocalDate.of(2000, 01, 01))
                .genres(Collections.emptyList()).likes(Set.of(1, 2, 3))
                .duration(90).build();

        likesDbStorage.updateFilmLikes(film1);
        assertEquals(Set.of(1, 2, 3), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));

        film1.setLikes(Set.of(3));
        likesDbStorage.updateFilmLikes(film1);
        assertEquals(Set.of(3), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));

        film1.setLikes(Collections.emptySet());
        likesDbStorage.updateFilmLikes(film1);
        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
    }

    @Test
    void deleteFilmLikesShouldDeleteAllFilmLikes() {
        jdbcTemplate.update(sqlInsert, 1, 1);
        jdbcTemplate.update(sqlInsert, 1, 2);
        jdbcTemplate.update(sqlInsert, 1, 3);
        jdbcTemplate.update(sqlInsert, 2, 1);
        jdbcTemplate.update(sqlInsert, 2, 3);
        jdbcTemplate.update(sqlInsert, 3, 2);

        assertEquals(Set.of(1, 2, 3), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
        assertEquals(Set.of(1, 3), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));
        assertEquals(Set.of(2), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 3)));

        likesDbStorage.deleteFilmLikes(1);
        likesDbStorage.deleteFilmLikes(2);
        likesDbStorage.deleteFilmLikes(3);

        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));
        assertEquals(Collections.emptySet(), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 3)));
    }

    @Test
    void addLikeShouldAddLikeToFilm() {
        likesDbStorage.addLike(1, 1);
        likesDbStorage.addLike(1, 2);
        likesDbStorage.addLike(1, 3);
        likesDbStorage.addLike(2, 3);

        assertEquals(Set.of(1, 2, 3), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
        assertEquals(Set.of(3), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));
    }

    @Test
    void deleteLikeShouldDeleteLikeFromFilm() {
        jdbcTemplate.update(sqlInsert, 1, 1);
        jdbcTemplate.update(sqlInsert, 1, 2);
        jdbcTemplate.update(sqlInsert, 1, 3);
        jdbcTemplate.update(sqlInsert, 2, 1);
        jdbcTemplate.update(sqlInsert, 2, 3);

        likesDbStorage.deleteLike(1, 2);
        likesDbStorage.deleteLike(2, 1);

        assertEquals(Set.of(1, 3), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 1)));
        assertEquals(Set.of(3), Set.copyOf(jdbcTemplate.queryForList(sqlSelect, Integer.class, 2)));
    }
}
