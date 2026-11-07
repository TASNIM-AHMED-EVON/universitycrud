package com.kuet.ums.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * INTEGRATION TESTS 1-3: the whole application (Spring Security + controllers + services +
 * JPA + H2 database) is started, and real HTTP-style requests go through MockMvc.
 * File name ends with IT so Maven Failsafe runs it in the "verify" phase.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UniversityApiIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    /** Logs in through the real /api/auth/login endpoint and returns the resulting session. */
    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String json(Map<String, ?> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void login_succeedsForSeededAdmin_andFailsWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "admin", "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_createsStudent_thenFetchesIt_andDuplicateRollIsRejected() throws Exception {
        MockHttpSession admin = login("admin", "admin123");
        Map<String, Object> student = Map.of(
                "username", "rafi", "password", "secret123", "fullName", "Rafi Ahmed",
                "email", "rafi@kuet.ac.bd", "studentRoll", "2207001", "department", "CSE", "semester", 6);

        // Create -> 201
        String body = mockMvc.perform(post("/api/students").session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(student)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentRoll").value("2207001"))
                .andExpect(jsonPath("$.password").doesNotExist())   // password is never returned
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("id").asLong();

        // Read back from the database -> 200
        mockMvc.perform(get("/api/students/" + id).session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Rafi Ahmed"));

        // Same roll number again (different username/email) -> 409 Conflict
        Map<String, Object> duplicate = Map.of(
                "username", "rafi2", "password", "secret123", "fullName", "Another Rafi",
                "email", "rafi2@kuet.ac.bd", "studentRoll", "2207001", "department", "CSE", "semester", 6);
        mockMvc.perform(post("/api/students").session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void rolesAreEnforced_studentCannotReachTeachers_andAnonymousGets401() throws Exception {
        MockHttpSession admin = login("admin", "admin123");
        mockMvc.perform(post("/api/students").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "sara", "password", "secret123", "fullName", "Sara Khan",
                                "email", "sara@kuet.ac.bd", "studentRoll", "2207002", "semester", 3))))
                .andExpect(status().isCreated());

        MockHttpSession student = login("sara", "secret123");

        mockMvc.perform(get("/api/teachers").session(student)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/students").session(student)).andExpect(status().isOk());
        mockMvc.perform(get("/api/teachers")).andExpect(status().isUnauthorized());
    }
}
