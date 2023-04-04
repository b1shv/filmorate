package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
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
    public Film getFilmById(int id) {
        String sql = "SELECT f.film_id, "
                + "f.name, "
                + "f.description, "
                + "f.release_date, "
                + "f.duration, "
                + "f.mpa_id, "
                + "m.name AS mpa_name "
                + "FROM films AS f LEFT OUTER JOIN mpa AS m ON f.mpa_id = m.mpa_id "
                + "WHERE film_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), id);
    }

    @Override
    public List<Film> getFilmsByIds(List<Integer> filmIds) {
        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        return jdbcTemplate.query(
                String.format("SELECT f.film_id, "
                        + "f.name, "
                        + "f.description, "
                        + "f.release_date, "
                        + "f.duration, "
                        + "f.mpa_id, "
                        + "m.name AS mpa_name "
                        + "FROM films AS f LEFT OUTER JOIN mpa AS m ON f.mpa_id = m.mpa_id "
                        + "WHERE film_id IN (%s)",
                        inSql), (rs, rowNum) -> makeFilm(rs), filmIds.toArray());
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

        if (film.getMpa() != null) {
            filmValues.put("mpa_id", film.getMpa().getId());
        }

        int filmId = simpleJdbcInsert.executeAndReturnKey(filmValues).intValue();
        film.setId(filmId);

        return getFilmById(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        Integer mpaId = null;

        if (film.getMpa() != null) {
            mpaId = film.getMpa().getId();
        }

        String sql = "UPDATE films " +
                "SET name = ?, description = ?, " +
                "release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(),
                film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), mpaId, film.getId());

        return getFilmById(film.getId());
    }

    @Override
    public void deleteFilm(int id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void checkFilmId(int id) {
        String sql = "SELECT film_id FROM films WHERE film_id = ?";

        try {
            jdbcTemplate.queryForObject(sql, Integer.class, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Film %d is not found", id));
        }
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Film film = Film.builder()
                .id(rs.getInt("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .build();

        if (rs.getInt("mpa_id") != 0) {
            film.setMpa(Mpa.builder()
                    .id(rs.getInt("mpa_id"))
                    .name(rs.getString("mpa_name"))
                    .build());
        }

        return film;
    }
}
