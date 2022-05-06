package sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HelloWorldControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHelloWorldFeatureWithAdmin() throws Exception {
        mockMvc.perform(get("/")
                .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));
    }

    @Test
    void testHelloWorldFeatureWithUser1() throws Exception {
        mockMvc.perform(get("/")
                .with(user("user1")))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));
    }

    @Test
    void testHelloWorldFeatureWithUser2() throws Exception {
        mockMvc.perform(get("/")
                .with(user("user2")))
                .andExpect(status().isOk())
                .andExpect(content().string("!tooB gnirpS morf sgniteerG"));
    }

    @Test
    void testHelloWorldFeatureWithUser3() throws Exception {
        mockMvc.perform(get("/")
                .with(user("user3")))
                .andExpect(status().isOk())
                .andExpect(content().string("!tooB gnirpS morf sgniteerG"));
    }

    @Test
    void testHelloWorldFeatureWithUser4() throws Exception {
        mockMvc.perform(get("/")
                .with(user("user4")))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));
    }
}
