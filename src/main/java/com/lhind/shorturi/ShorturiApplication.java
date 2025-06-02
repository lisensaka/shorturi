package com.lhind.shorturi;

import com.lhind.shorturi.config.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
@EnableScheduling
public class ShorturiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShorturiApplication.class, args);
    }

	@GetMapping("/hello")
	public String shorturi(Principal principal) {
		return "Hello ShorturiApplication from: " + principal.getName();
	}

}
