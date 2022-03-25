package sample;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.NamedFeature;

@SpringBootTest
@AutoConfigureMockMvc
public class HelloWorldControllerIntegrationTests {

	private static final Feature HELLO_WORLD = new NamedFeature("HELLO_WORLD");
	private static final Feature REVERSE_GREETING = new NamedFeature("REVERSE_GREETING");

	@Autowired
	private StateRepository state;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testHelloWorldFeatureDisabled() throws Exception {
		state.setFeatureState(new FeatureState(HELLO_WORLD, false));
		mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(not(containsString("Greetings from Spring Boot!"))));
	}

	@Test
	public void testHelloWorldFeatureEnabled() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(containsString("Greetings from Spring Boot!")));
	}

	@Test
	public void testHelloWorldFeatureAndReverseGreetingEnabled() throws Exception {
		state.setFeatureState(new FeatureState(REVERSE_GREETING, true));
		mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(containsString("!tooB gnirpS morf sgniteerG")));
	}
}
