package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getGenres() {
        String sql = "SELECT * FROM genres";
        return jdbcTemplate.query(sql, this::makeGenre);
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, this::makeGenre, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Genre %d is not found", id));
        }
    }

    @Override
    public List<Genre> getGenresByFilmId(int filmId) {
        String sql =
                "SELECT g.* FROM genres AS g JOIN films_genres AS fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";
        try {
            return jdbcTemplate.query(sql, this::makeGenre, filmId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void updateFilmGenres(Film film) {
        deleteFilmGenres(film.getId());

        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream()
                    .distinct()
                    .collect(Collectors.toList()));

            String insertGenreSql = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";

            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(insertGenreSql, film.getId(), genre.getId());
            }
        }
    }

    @Override
    public void deleteFilmGenres(int filmId) {
        String sql = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .name(rs.getString("name"))
                .build();
    }
}
