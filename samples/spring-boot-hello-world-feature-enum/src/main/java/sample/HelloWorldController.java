package sample;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

	@RequestMapping("/")
    public ResponseEntity<?> index() {
        if (Features.HELLO_WORLD.isActive()) {
            StringBuilder sb = new StringBuilder("Greetings from Spring Boot!");
            if (Features.REVERSE_GREETING.isActive()) {
                sb.reverse();
            }
            return ResponseEntity.ok().body(sb.toString());
        }
        return ResponseEntity.notFound().build();
    }
}
