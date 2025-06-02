package com.lhind.shorturi.service;

import com.lhind.shorturi.config.security.JwtService;
import com.lhind.shorturi.entity.dto.request.AuthRequestDto;
import com.lhind.shorturi.entity.dto.request.RegisterRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    public String registerAndGenerateToken(RegisterRequestDto request) throws Exception {
        try {
            var requestEncoded = new RegisterRequestDto(request.username(), passwordEncoder.encode(request.password()));

            var registeredUser = userService.save(requestEncoded);
            return jwtService.generateToken(registeredUser);
        }
        catch (SQLException e){
            throw new Exception(e.getMessage());
        }
    }

    public String authenticateAndGenerateToken(AuthRequestDto request){

        var registeredUser = userService.findByUsername(request.username());
        return jwtService.generateToken(registeredUser);
    }


}
