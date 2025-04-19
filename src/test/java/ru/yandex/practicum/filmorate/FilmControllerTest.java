package ru.yandex.practicum.filmorate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void createUserWithEmptyNameShouldReturnBadRequest() throws Exception {
        String userJson = "{ \"login\": \"login\", \"email\": \"test@mail.com\", \"birthday\": \"2000-01-01\" }";
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
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




