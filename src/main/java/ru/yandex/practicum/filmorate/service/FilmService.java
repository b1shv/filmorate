package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final UserStorage userStorage;
    private static final LocalDate FIRST_FILM_RELEASE_DATE = (LocalDate.of(1895, 12, 28));

    @Autowired
    public FilmService(FilmStorage filmStorage, GenreStorage genreStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getFilms() {
        Map<Integer, List<Genre>> filmsGenres = genreStorage.getAllFilmsGenres();

        return filmStorage.getFilms().stream()
                .map(film -> {
                    film.setGenres(new ArrayList<>());

                    if (filmsGenres.containsKey(film.getId())) {
                        film.setGenres(filmsGenres.get(film.getId()));
                    }
                    return film;
                })
                .collect(Collectors.toList());
    }

    public List<Film> getMostPopularFilms(int count) {
        List<Film> films = filmStorage.getMostPopularFilms(count);
        Map<Integer, List<Genre>> genres = genreStorage.getGenresByFilmsIds(
                films.stream().map(Film::getId).collect(Collectors.toList()));

        return films.stream()
                .map(film -> {
                    film.setGenres(new ArrayList<>());

                    if (genres.containsKey(film.getId())) {
                        film.setGenres(genres.get(film.getId()));
                    }
                    return film;
                })
                .collect(Collectors.toList());
    }

    public Film getFilmById(int id) {
        if (!filmStorage.filmExists(id)) {
            throw new NotFoundException(String.format("Film %d is not found", id));
        }

        Film film = filmStorage.getFilmById(id);
        film.setGenres(genreStorage.getGenresByFilmId(id));

        return film;
    }

    public Film addFilm(Film film) {
        validateReleaseDate(film);

        Film filmReturned = filmStorage.addFilm(film);

        genreStorage.updateFilmGenres(film);

        filmReturned.setGenres(genreStorage.getGenresByFilmId(filmReturned.getId()));

        log.debug("POST request handled: new film added");
        return filmReturned;
    }

    public Film updateFilm(Film film) {
        validateReleaseDate(film);

        if (!filmStorage.filmExists(film.getId())) {
            throw new NotFoundException(String.format("Film %d is not found", film.getId()));
        }

        Film filmReturned = filmStorage.updateFilm(film);

        genreStorage.updateFilmGenres(film);

        filmReturned.setGenres(genreStorage.getGenresByFilmId(filmReturned.getId()));

        log.debug(String.format("PUT request handled: film %d updated", film.getId()));
        return filmReturned;
    }

    public void deleteFilm(int id) {
        if (!filmStorage.filmExists(id)) {
            throw new NotFoundException(String.format("Film %d is not found", id));
        }

        filmStorage.deleteFilm(id);
        log.debug(String.format("DELETE request handled: film %d deleted", id));
    }

    public void addLike(int filmId, int userId) {
        if (!filmStorage.filmExists(filmId)) {
            throw new NotFoundException(String.format("Film %d is not found", filmId));
        }
        if (!userStorage.userExists(userId)) {
            throw new NotFoundException(String.format("User %d is not found", userId));
        }

        filmStorage.addLike(filmId, userId);
        log.debug(String.format("PUT request handled: like from user %d added to film %d", userId, filmId));
    }

    public void deleteLike(int filmId, int userId) {
        if (!filmStorage.filmExists(filmId)) {
            throw new NotFoundException(String.format("Film %d is not found", filmId));
        }
        if (!userStorage.userExists(userId)) {
            throw new NotFoundException(String.format("User %d is not found", userId));
        }

        filmStorage.deleteLike(filmId, userId);
        log.debug(String.format("DELETE request handled: like from user %d deleted from film %d", userId, filmId));
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            throw new ValidationException("Validation failed: Incorrect release date");
        }
    }
}
