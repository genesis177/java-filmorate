package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@SpringBootTest(classes = ru.yandex.practicum.filmorate.FilmorateApplication.class)
@AutoConfigureMockMvc
class FilmControllerTest {

    public static final String PATH = "/films";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void create() {

    }


    @Test
    void update() {

    }

    @Test
    void getAll() {

    }

    private String getContentFromFile(String filename) {
        try {
            var resource = ResourceUtils.getFile("classpath:" + filename);
            if (!resource.exists()) {
                throw new RuntimeException("Файл не найден: " + filename);
            }
            return Files.readString(resource.toPath(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new RuntimeException("Не открывается файл: " + filename, exception);
        }
    }
}




