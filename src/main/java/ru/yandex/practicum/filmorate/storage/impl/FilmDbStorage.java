package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikesStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

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
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LikesStorage likesStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaStorage mpaStorage,
                         GenreStorage genreStorage, LikesDbStorage likesDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.likesStorage = likesDbStorage;
    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
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

        int filmId = simpleJdbcInsert.executeAndReturnKey(filmValues).intValue();
        film.setId(filmId);

        genreStorage.updateFilmGenres(film);
        mpaStorage.updateFilmMpa(film);
        likesStorage.updateFilmLikes(film);

        return getFilmById(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films " +
                "SET name = ?, description = ?, " +
                "release_date = ?, duration = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(),
                film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getId());

        genreStorage.updateFilmGenres(film);
        mpaStorage.updateFilmMpa(film);
        likesStorage.updateFilmLikes(film);

        return getFilmById(film.getId());
    }

    @Override
    public void deleteFilm(int id) {
        genreStorage.deleteFilmGenres(id);
        mpaStorage.deleteFilmMpa(id);
        likesStorage.deleteFilmLikes(id);

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

    @Override
    public void addLike(int filmId, int userId) {
        likesStorage.addLike(filmId, userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        likesStorage.deleteLike(filmId, userId);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int filmId = rs.getInt("film_id");

        return Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpaStorage.getMpaByFilmId(filmId))
                .genres(genreStorage.getGenresByFilmId(filmId))
                .likes(likesStorage.getLikesByFilmId(filmId))
                .build();
    }
}
