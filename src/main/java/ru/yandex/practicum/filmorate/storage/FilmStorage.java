package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getFilms();

    List<Film> getMostPopularFilms(int count);

    Film getFilmById(int id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(int id);

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    boolean filmExists(int id);
}
