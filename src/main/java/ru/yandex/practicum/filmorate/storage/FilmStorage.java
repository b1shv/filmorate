package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getFilms();

    Film getFilmById(int id);

    List<Film> getFilmsByIds(List<Integer> filmIds);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(int id);

    void checkFilmId(int id);
}
