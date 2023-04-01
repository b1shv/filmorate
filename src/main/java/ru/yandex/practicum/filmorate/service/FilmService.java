package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private static final LocalDate FIRST_FILM_RELEASE_DATE = (LocalDate.of(1895, 12, 28));

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilmById(int id) {
        filmStorage.checkFilmId(id);
        return filmStorage.getFilmById(id);
    }

    public Film addFilm(Film film) {
        validateReleaseDate(film);
        log.debug("POST request handled: new film added");
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateReleaseDate(film);
        filmStorage.checkFilmId(film.getId());

        log.debug(String.format("PUT request handled: film %d updated", film.getId()));
        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(int id) {
        filmStorage.checkFilmId(id);
        filmStorage.deleteFilm(id);
        log.debug(String.format("DELETE request handled: film %d deleted", id));
    }

    public void addLike(int filmId, int userId) {
        filmStorage.checkFilmId(filmId);
        userService.checkUserId(userId);

        filmStorage.addLike(filmId, userId);
        log.debug(String.format("PUT request handled: like from user %d added to film %d", userId, filmId));
    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.checkFilmId(filmId);
        userService.checkUserId(userId);

        filmStorage.deleteLike(filmId, userId);
        log.debug(String.format("DELETE request handled: like from user %d deleted from film %d", userId, filmId));
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            throw new ValidationException("Validation failed: Incorrect release date");
        }
    }
}
