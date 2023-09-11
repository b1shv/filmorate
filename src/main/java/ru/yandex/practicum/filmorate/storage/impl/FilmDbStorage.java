package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT f.film_id, "
                + "f.name, "
                + "f.description, "
                + "f.release_date, "
                + "f.duration, "
                + "f.mpa_id, "
                + "m.name AS mpa_name "
                + "FROM films AS f LEFT OUTER JOIN mpa AS m ON f.mpa_id = m.mpa_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        String sql = "SELECT f.*, m.mpa_id, m.name AS mpa_name, COUNT(fl.film_id) " +
                "FROM films AS f " +
                "LEFT JOIN films_likes AS fl on f.film_id = fl.film_id " +
                "JOIN mpa AS m on m.mpa_id = f.mpa_id " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(fl.film_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT f.film_id, "
                + "f.name, "
                + "f.description, "
                + "f.release_date, "
                + "f.duration, "
                + "f.mpa_id, "
                + "m.name AS mpa_name "
                + "FROM films AS f LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id "
                + "WHERE film_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), id);
    }

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> filmValues = new HashMap<>();
        filmValues.put("name", film.getName());
        filmValues.put("description", film.getDescription());
        filmValues.put("release_date", Date.valueOf(film.getReleaseDate()));
        filmValues.put("duration", film.getDuration());
        filmValues.put("mpa_id", film.getMpa().getId());

        int filmId = simpleJdbcInsert.executeAndReturnKey(filmValues).intValue();
        film.setId(filmId);

        return getFilmById(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films " +
                "SET name = ?, description = ?, " +
                "release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(),
                film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId(), film.getId());

        return getFilmById(film.getId());
    }

    @Override
    public void deleteFilm(int id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO films_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        } catch (DuplicateKeyException e) {
            throw new ValidationException(
                    String.format("User %d already likes film %d", userId, filmId));
        }
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        jdbcTemplate.update(
                "DELETE FROM films_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public boolean filmExists(int id) {
        String sql = "SELECT film_id FROM films WHERE film_id = ?";

        try {
            jdbcTemplate.queryForObject(sql, Integer.class, id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        return Film.builder()
                .id(rs.getInt("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")))
                .build();
    }
}
