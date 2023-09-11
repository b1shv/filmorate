package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getGenres() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
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
    public Map<Integer, List<Genre>> getAllFilmsGenres() {
        String sql = "SELECT fg.film_id, g.genre_id, g.name "
                + "FROM films_genres AS fg JOIN genres AS g ON fg.genre_id = g.genre_id";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);

        return getFilmsGenres(rs);
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
    public Map<Integer, List<Genre>> getGenresByFilmsIds(List<Integer> filmsIds) {
        String inSql = String.join(",", Collections.nCopies(filmsIds.size(), "?"));
        SqlRowSet rs = jdbcTemplate.queryForRowSet(String.format("SELECT fg.film_id, g.genre_id, g.name " +
                "FROM films_genres AS fg JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (%s)", inSql), filmsIds.toArray());

        return getFilmsGenres(rs);
    }

    @Override
    public void updateFilmGenres(Film film) {
        deleteFilmGenres(film.getId());

        if (film.getGenres() == null) {
            return;
        }

        List<Object[]> batch = new ArrayList<>();
        List<Genre> genres = film.getGenres().stream()
                .distinct()
                .collect(Collectors.toList());

        for (Genre genre : genres) {
            Object[] values = new Object[]{film.getId(), genre.getId()};
            batch.add(values);
        }

        jdbcTemplate.batchUpdate("INSERT INTO films_genres VALUES(?, ?)", batch);
    }

    @Override
    public void deleteFilmGenres(int filmId) {
        String sql = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private Map<Integer, List<Genre>> getFilmsGenres(SqlRowSet rs) {
        Map<Integer, List<Genre>> filmsGenres = new HashMap<>();

        while (rs.next()) {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"),
                    rs.getString("name"));

            if (!filmsGenres.containsKey(filmId)) {
                List<Genre> genres = new ArrayList<>();
                genres.add(genre);
                filmsGenres.put(filmId, genres);
            } else {
                filmsGenres.get(filmId).add(genre);
            }
        }

        return filmsGenres;
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .name(rs.getString("name"))
                .build();
    }
}
