package sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
public class Application {

    @Configuration
    protected static class ApplicationSecurity {

        @Bean
        protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                    .authorizeHttpRequests()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .csrf()
                    .disable()
                    .formLogin()
                    .and()
                    .logout();
            // @@formatter:on
            return http.build();
        }

        @Bean
        public InMemoryUserDetailsManager userDetailsService() {
            // @formatter:off
            UserDetails admin = User.withDefaultPasswordEncoder()
                                    .username("admin")
                                    .password("pwd")
                                    .roles("ADMIN", "USER")
                                    .build();
            UserDetails user1 = User.withDefaultPasswordEncoder()
                                    .username("user1")
                                    .password("pwd")
                                    .roles("USER")
                                    .build();
            UserDetails user2 = User.withDefaultPasswordEncoder()
                                    .username("user2")
                                    .password("pwd")
                                    .roles("USER")
                                    .build();
            UserDetails user3 = User.withDefaultPasswordEncoder()
                                    .username("user3")
                                    .password("pwd")
                                    .roles("USER")
                                    .build();
            UserDetails user4 = User.withDefaultPasswordEncoder()
                                    .username("user4")
                                    .password("pwd")
                                    .roles("USER")
                                    .build();
            // @formatter:on
            return new InMemoryUserDetailsManager(admin, user1, user2, user3, user4);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
