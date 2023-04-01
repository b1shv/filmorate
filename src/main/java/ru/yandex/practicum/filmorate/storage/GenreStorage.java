package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    List<Genre> getGenres();

    Genre getGenreById(int id);

    List<Genre> getGenresByFilmId(int filmId);

    void updateFilmGenres(Film film);

    void deleteFilmGenres(int filmId);
}
