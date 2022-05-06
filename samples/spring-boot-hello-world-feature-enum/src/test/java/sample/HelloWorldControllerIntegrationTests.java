package sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HelloWorldControllerIntegrationTests {
	
	@Autowired
	private StateRepository state;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHelloWorldFeatureDisabled() throws Exception {
        state.setFeatureState(new FeatureState(Features.HELLO_WORLD, false));
        mockMvc.perform(get("/")).andExpect(status().isNotFound());
    }

    @Test
    void testHelloWorldFeatureEnabled() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));
    }

    @Test
    void testHelloWorldFeatureAndReverseGreetingEnabled() throws Exception {
        state.setFeatureState(new FeatureState(Features.REVERSE_GREETING, true));
        mockMvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string("!tooB gnirpS morf sgniteerG"));
    }
}
