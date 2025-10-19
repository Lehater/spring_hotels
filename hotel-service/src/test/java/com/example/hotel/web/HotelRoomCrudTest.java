package com.example.hotel.web;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.hotel.test.JwtTestUtils;
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
class HotelRoomCrudTest {

    @Autowired MockMvc mvc;

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret-test-secret";
    private String bearerAdmin() {
        return "Bearer " + JwtTestUtils.issueHs256(SECRET, "admin", "ROLE_ADMIN", 3600);
    }
    private String bearerUser() {
        return "Bearer " + JwtTestUtils.issueHs256(SECRET, "u1", "ROLE_USER", 3600);
    }

    @Test
    void admin_can_create_hotel_and_room_user_can_read_lists() throws Exception {
        // create hotel
        mvc.perform(post("/api/hotels")
                        .header(HttpHeaders.AUTHORIZATION, bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"TestHotel\",\"city\":\"Berlin\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        // create room
        mvc.perform(post("/api/rooms")
                        .header(HttpHeaders.AUTHORIZATION, bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hotelId\":1,\"number\":\"101\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.available").value(true));

        // user can list hotels
        mvc.perform(get("/api/hotels").header(HttpHeaders.AUTHORIZATION, bearerUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // user can list rooms by hotel
        mvc.perform(get("/api/rooms").queryParam("hotelId", "1")
                        .header(HttpHeaders.AUTHORIZATION, bearerUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hotelId").value(1));
    }

}
