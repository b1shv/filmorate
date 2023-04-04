package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    @Autowired
    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getGenres() {
        return genreStorage.getGenres().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toList());
    }

    public Genre getGenreById(int id) {
        return genreStorage.getGenreById(id);
    }
}
