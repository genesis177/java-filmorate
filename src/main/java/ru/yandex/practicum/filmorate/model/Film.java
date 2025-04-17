package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {


  private Long id;

  @NotBlank
  private String name;

  @Size(min = 1, max = 255)
  private String description;

  @Min(1)
  private Long duration;

  @NotNull
  private LocalDate releaseDate;

}
