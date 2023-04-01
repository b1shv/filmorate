package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa";
        return jdbcTemplate.query(sql, this::makeMpa);
    }

    @Override
    public Mpa getMpaById(int id) {
        String sql = "SELECT * FROM mpa WHERE mpa_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, this::makeMpa, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Mpa %d is not found", id));
        }
    }

    @Override
    public Mpa getMpaByFilmId(int id) {
        String sql =
                "SELECT m.* FROM mpa AS m JOIN films_mpa AS fm ON m.mpa_id = fm.mpa_id WHERE fm.film_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::makeMpa, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updateFilmMpa(Film film) {
        deleteFilmMpa(film.getId());

        if (film.getMpa() != null) {
            String insertSql = "INSERT INTO films_mpa (film_id, mpa_id) VALUES (?, ?)";
            jdbcTemplate.update(insertSql, film.getId(), film.getMpa().getId());
        }
    }

    @Override
    public void deleteFilmMpa(int filmId) {
        String sql = "DELETE FROM films_mpa WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getInt("mpa_id"))
                .name(rs.getString("name"))
                .build();
    }
}
