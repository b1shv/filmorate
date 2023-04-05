package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class Film {
    private int id;
    private List<Genre> genres;

    @NotNull(message = "field mpa should not be empty")
    private Mpa mpa;

    @NotBlank(message = "field name should not be empty")
    private String name;

    @Size(max = 200, message = "field description should be 200 characters or less")
    private String description;

    @NotNull(message = "field releaseDate should not be empty")
    private LocalDate releaseDate;

    @Positive(message = "field duration should be positive")
    private int duration;
}
