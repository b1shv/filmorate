package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikesStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class LikesDbStorage implements LikesStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikesDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Integer, Set<Integer>> getAllFilmsLikes() {
        String sql = "SELECT * FROM films_likes";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        return getFilmsLikes(rs);
    }

    @Override
    public Map<Integer, Set<Integer>> getMostPopularFilmsLikes(int count) {
        String sql = "SELECT film_id, user_id " +
                "FROM films_likes " +
                "WHERE film_id IN (" +
                "SELECT film_id " +
                "FROM films_likes " +
                "GROUP BY film_id " +
                "ORDER BY COUNT(*) DESC " +
                "LIMIT ?)";

        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, count);
        return getFilmsLikes(rs);
    }

    @Override
    public Set<Integer> getLikesByFilmId(int id) {
        String sql = "SELECT user_id FROM films_likes WHERE film_id = ?";

        try {
            return Set.copyOf(jdbcTemplate.queryForList(sql, Integer.class, id));
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public void updateFilmLikes(Film film) {
        deleteFilmLikes(film.getId());

        if (film.getLikes() == null) {
            return;
        }

        List<Object[]> batch = new ArrayList<>();

        for (Integer like : film.getLikes()) {
            Object[] values = new Object[]{film.getId(), like};
            batch.add(values);
        }

        jdbcTemplate.batchUpdate("INSERT INTO films_likes VALUES(?, ?)", batch);
    }

    @Override
    public void deleteFilmLikes(int filmId) {
        String sql = "DELETE FROM films_likes WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
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

    private Map<Integer, Set<Integer>> getFilmsLikes(SqlRowSet rs) {
        Map<Integer, Set<Integer>> filmsLikes = new HashMap<>();

        while (rs.next()) {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");

            if (!filmsLikes.containsKey(filmId)) {
                Set<Integer> likes = new HashSet<>();
                likes.add(userId);

                filmsLikes.put(filmId, likes);
            } else {
                filmsLikes.get(filmId).add(userId);
            }
        }

        return filmsLikes;
    }
}
