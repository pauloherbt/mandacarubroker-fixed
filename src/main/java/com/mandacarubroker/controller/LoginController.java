package com.mandacarubroker.controller;

import com.mandacarubroker.domain.user.User;
import com.mandacarubroker.dtos.RequestLoginDTO;
import com.mandacarubroker.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping
    public ResponseEntity login(@RequestBody RequestLoginDTO req){
        Authentication auth = UsernamePasswordAuthenticationToken.unauthenticated(req.username(),req.password());
        auth = authenticationManager.authenticate(auth);
        if(auth.isAuthenticated())
            return ResponseEntity.ok().body(tokenService.generateToken((User)auth.getPrincipal()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to login");
    }
}
