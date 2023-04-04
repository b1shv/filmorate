package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MpaDbStorageTest {
    EmbeddedDatabase embeddedDatabase;
    JdbcTemplate jdbcTemplate;
    MpaDbStorage mpaDbStorage;
    private static final Mpa G = new Mpa(1, "G");
    private static final Mpa PG = new Mpa(2, "PG");
    private static final Mpa PG13 = new Mpa(3, "PG-13");
    private static final Mpa R = new Mpa(4, "R");
    private static final Mpa NC17 = new Mpa(5, "NC-17");

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
}
