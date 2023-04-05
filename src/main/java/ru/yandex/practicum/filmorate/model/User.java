package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Builder
public class User {
    private int id;
    private String name;

    @NotBlank(message = "field email should not be empty")
    @Email(message = "wrong email format")
    private String email;

    @NotBlank(message = "field login should not be empty")
    @Pattern(regexp = "^\\S+$", message = "login should consist of letters")
    private String login;

    @Past(message = "birthday can't be in the future")
    @NotNull(message = "field birthday should not be empty")
    private LocalDate birthday;
}
