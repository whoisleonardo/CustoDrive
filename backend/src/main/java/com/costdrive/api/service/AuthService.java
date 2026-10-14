package com.costdrive.api.service;

import com.costdrive.api.dto.AuthDtos.AuthResponse;
import com.costdrive.api.dto.AuthDtos.LoginRequest;
import com.costdrive.api.dto.AuthDtos.RegisterRequest;
import com.costdrive.api.dto.AuthDtos.UserResponse;
import com.costdrive.api.model.UserAccount;
import com.costdrive.api.repository.CarRepository;
import com.costdrive.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final CarRepository cars;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, CarRepository cars, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.cars = cars;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já possui uma conta");
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setDisplayName(request.name().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(OffsetDateTime.now());
        user = users.saveAndFlush(user);
        // Na migração de uma instalação antiga, a seleção única anterior passa ao primeiro usuário cadastrado.
        cars.assignUnownedCarsTo(user.getId());
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = users.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos");
        }
        return response(user);
    }

    @Transactional(readOnly = true)
    public UserResponse current(String email) {
        return users.findByEmail(normalizeEmail(email)).map(this::toUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida"));
    }

    private AuthResponse response(UserAccount user) {
        return new AuthResponse(jwtService.createToken(user.getEmail()), "Bearer", toUser(user));
    }

    private UserResponse toUser(UserAccount user) { return new UserResponse(user.getId(), user.getDisplayName(), user.getEmail()); }
    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
