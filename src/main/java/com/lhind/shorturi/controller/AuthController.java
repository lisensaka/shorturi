package com.lhind.shorturi.controller;

import com.lhind.shorturi.entity.dto.request.AuthRequestDto;
import com.lhind.shorturi.entity.dto.request.RegisterRequestDto;
import com.lhind.shorturi.entity.dto.response.AuthResponseDto;
import com.lhind.shorturi.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
//@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final AuthService authService;


    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }


    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid RegisterRequestDto request) throws Exception {
        logger.info("Register Api request: {} begin", request);
        var jwtToken = authService.registerAndGenerateToken(request);
        logger.info("Register Api request: {} executed successfully", request);
        return ResponseEntity.ok(new AuthResponseDto(jwtToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> authenticate(@RequestBody AuthRequestDto request) {
        logger.info("Authenticate Api request: {} begin", request);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        var jwtToken = authService.authenticateAndGenerateToken(request);
        logger.info("Authenticate Api request: {} executed successfully", request);

        return ResponseEntity.ok(new AuthResponseDto(jwtToken));
    }
}

