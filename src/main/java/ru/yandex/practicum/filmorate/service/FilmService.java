package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikesStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final LikesStorage likesStorage;
    private final UserService userService;
    private static final LocalDate FIRST_FILM_RELEASE_DATE = (LocalDate.of(1895, 12, 28));

    @Autowired
    public FilmService(FilmStorage filmStorage, GenreStorage genreStorage,
                       LikesStorage likesStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.likesStorage = likesStorage;
        this.userService = userService;
    }

    public List<Film> getFilms() {
        Map<Integer, List<Genre>> filmsGenres = genreStorage.getAllFilmsGenres();
        Map<Integer, Set<Integer>> filmsLikes = likesStorage.getAllFilmsLikes();

        return filmStorage.getFilms().stream()
                .map(film -> {
                    film.setGenres(new ArrayList<>());
                    film.setLikes(new HashSet<>());

                    if (filmsGenres.containsKey(film.getId())) {
                        film.setGenres(filmsGenres.get(film.getId()));
                    }
                    if (filmsLikes.containsKey(film.getId())) {
                        film.setLikes(filmsLikes.get(film.getId()));
                    }
                    return film;
                })
                .collect(Collectors.toList());
    }

    public List<Film> getMostPopularFilms(int count) {
        Map<Integer, Set<Integer>> filmsLikes = likesStorage.getMostPopularFilmsLikes(count);
        List<Integer> filmsIds = new ArrayList<>(filmsLikes.keySet());
        Map<Integer, List<Genre>> filmsGenres = genreStorage.getGenresByFilmsIds(filmsIds);

        List<Film> popularFilms = filmStorage.getFilmsByIds(filmsIds);

        if (popularFilms.isEmpty()) {
            return getFilms().stream()
                    .limit(count)
                    .collect(Collectors.toList());
        }

        return popularFilms.stream()
                .map(film -> {
                    film.setGenres(new ArrayList<>());
                    film.setLikes(new HashSet<>());

                    if (filmsGenres.containsKey(film.getId())) {
                        film.setGenres(filmsGenres.get(film.getId()));
                    }
                    if (filmsLikes.containsKey(film.getId())) {
                        film.setLikes(filmsLikes.get(film.getId()));
                    }
                    return film;
                })
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    public Film getFilmById(int id) {
        filmStorage.checkFilmId(id);
        Film film = filmStorage.getFilmById(id);

        film.setLikes(likesStorage.getLikesByFilmId(id));
        film.setGenres(genreStorage.getGenresByFilmId(id));

        return film;
    }

    public Film addFilm(Film film) {
        validateReleaseDate(film);

        Film filmReturned = filmStorage.addFilm(film);

        genreStorage.updateFilmGenres(film);
        likesStorage.updateFilmLikes(film);

        filmReturned.setGenres(genreStorage.getGenresByFilmId(filmReturned.getId()));
        filmReturned.setLikes(likesStorage.getLikesByFilmId(filmReturned.getId()));

        log.debug("POST request handled: new film added");
        return filmReturned;
    }

    public Film updateFilm(Film film) {
        validateReleaseDate(film);
        filmStorage.checkFilmId(film.getId());

        Film filmReturned = filmStorage.updateFilm(film);

        genreStorage.updateFilmGenres(film);
        likesStorage.updateFilmLikes(film);

        filmReturned.setGenres(genreStorage.getGenresByFilmId(filmReturned.getId()));
        filmReturned.setLikes(likesStorage.getLikesByFilmId(filmReturned.getId()));

        log.debug(String.format("PUT request handled: film %d updated", film.getId()));
        return filmReturned;
    }

    public void deleteFilm(int id) {
        filmStorage.checkFilmId(id);
        filmStorage.deleteFilm(id);
        log.debug(String.format("DELETE request handled: film %d deleted", id));
    }

    public void addLike(int filmId, int userId) {
        filmStorage.checkFilmId(filmId);
        userService.checkUserId(userId);

        likesStorage.addLike(filmId, userId);
        log.debug(String.format("PUT request handled: like from user %d added to film %d", userId, filmId));
    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.checkFilmId(filmId);
        userService.checkUserId(userId);

        likesStorage.deleteLike(filmId, userId);
        log.debug(String.format("DELETE request handled: like from user %d deleted from film %d", userId, filmId));
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            throw new ValidationException("Validation failed: Incorrect release date");
        }
    }
}
