package sample;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    private static final String GREETING = "Greetings from Spring Boot!";

    @RequestMapping("/")
    public ResponseEntity<?> index() {
        return ResponseEntity.ok().body(GREETING);
    }
}
