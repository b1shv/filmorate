package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;
import java.util.Set;

public interface LikesStorage {
    Map<Integer, Set<Integer>> getAllFilmsLikes();

    Map<Integer, Set<Integer>> getMostPopularFilmsLikes(int count);

    Set<Integer> getLikesByFilmId(int id);

    void updateFilmLikes(Film film);

    void deleteFilmLikes(int filmId);

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);
}
