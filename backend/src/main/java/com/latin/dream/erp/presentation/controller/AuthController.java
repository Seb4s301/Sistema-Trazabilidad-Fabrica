package com.latin.dream.erp.presentation.controller;

import com.latin.dream.erp.application.dto.AuthRequest;
import com.latin.dream.erp.application.dto.AuthResponse;
import com.latin.dream.erp.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de presentación: autenticación.
 * POST /api/v1/auth/login
 */
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Si las credenciales son inválidas, el GlobalExceptionHandler responde
    // { "codigo": "...", "mensaje": "..." } con HTTP 400.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.autenticar(request.getCorreo(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
