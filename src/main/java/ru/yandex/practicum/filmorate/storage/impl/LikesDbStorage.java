package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikesStorage;

import java.util.Collections;
import java.util.Set;

@Component
public class LikesDbStorage implements LikesStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikesDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        if (film.getLikes() != null) {
            String insertLikeSql = "INSERT INTO films_likes (film_id, user_id) VALUES (?, ?)";

            for (Integer like : film.getLikes()) {
                jdbcTemplate.update(insertLikeSql, film.getId(), like);
            }
        }
    }

    @Override
    public void deleteFilmLikes(int filmId) {
        String sql = "DELETE FROM films_likes WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public void addLike(int filmId, int userId) {
        Integer likeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM films_likes WHERE film_id = ? AND user_id = ?",
                Integer.class, filmId, userId);

        if (likeCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO films_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        }
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        jdbcTemplate.update(
                "DELETE FROM films_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }
}
