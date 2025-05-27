package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.start.FinalProjectApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FinalProjectApplication.class)
@AutoConfigureMockMvc
public class MpaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    //проверка, что при запросе /mpa возвращается список MPAA
    @Test
    public void getAllMpa_ShouldReturnList() throws Exception {
        mvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("G"));
    }

    //проверка получения MPAA по валидным ID (1, 2, 3)
    @Test
    public void getMpa_ByValidId_ShouldReturnMpa() throws Exception {
        mvc.perform(get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G"));

        mvc.perform(get("/mpa/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("PG"));

        mvc.perform(get("/mpa/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("PG-13"));
    }

    //получение MPAA по несуществующему ID, ожидается 404 Not Found
    @Test
    public void getMpa_ByInvalidId_ShouldReturn404() throws Exception {
        mvc.perform(get("/mpa/9999"))
                .andExpect(status().isNotFound());
    }
}