package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {
    List<Mpa> getAllMpa();

    Mpa getMpaById(int id);

    Mpa getMpaByFilmId(int id);

    void updateFilmMpa(Film film);

    void deleteFilmMpa(int filmId);
}
