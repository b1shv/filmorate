package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;

public interface GenreStorage {
    List<Genre> getGenres();

    Genre getGenreById(int id);

    Map<Integer, List<Genre>> getAllFilmsGenres();

    List<Genre> getGenresByFilmId(int filmId);

    Map<Integer, List<Genre>> getGenresByFilmsIds(List<Integer> filmsIds);

    void updateFilmGenres(Film film);

    void deleteFilmGenres(int filmId);
}
