package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 0;

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(int id) {
        return films.get(id);
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(++idCounter);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void deleteFilm(int id) {
        films.remove(id);
    }

    @Override
    public void checkFilmId(int id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException(String.format("Film %d is not found", id));
        }
    }
}
