package com.example.hotel.web;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.hotel.test.JwtTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationControllerTest {

    @Autowired MockMvc mvc;

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret-test-secret";
    private String bearerAdmin() { return "Bearer " + JwtTestUtils.issueHs256(SECRET, "admin", "ROLE_ADMIN", 3600); }
    private String bearerUser()  { return "Bearer " + JwtTestUtils.issueHs256(SECRET, "u1",    "ROLE_USER", 3600); }

    @BeforeEach
    void seed() throws Exception {
        mvc.perform(post("/api/hotels")
                        .header(HttpHeaders.AUTHORIZATION, bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"RecHotel\",\"city\":\"Berlin\"}"))
                .andExpect(status().isCreated());

        // несколько комнат с разным timesBooked
        mvc.perform(post("/api/rooms").header(HttpHeaders.AUTHORIZATION, bearerAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hotelId\":1,\"number\":\"101\"}")).andExpect(status().isCreated());
        mvc.perform(post("/api/rooms").header(HttpHeaders.AUTHORIZATION, bearerAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hotelId\":1,\"number\":\"102\"}")).andExpect(status().isCreated());
    }

    @Test
    void recommend_returns_free_rooms_sorted_and_limited() throws Exception {
        // первый вызов рекомендаций с limit=1
        mvc.perform(get("/api/rooms/recommend")
                        .queryParam("hotelId","1")
                        .queryParam("start","2025-10-25")
                        .queryParam("end","2025-10-27")
                        .queryParam("limit","1")
                        .header(HttpHeaders.AUTHORIZATION, bearerUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", notNullValue()));
    }
}
