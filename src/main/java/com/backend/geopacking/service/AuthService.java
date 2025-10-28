package com.backend.geopacking.service;


import com.backend.geopacking.auth.AuthRequest;
import com.backend.geopacking.auth.jwt.JwtUtil;
import com.backend.geopacking.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    public String login(@RequestBody AuthRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getDni(), request.getPassword())
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getDni());
        final String jwt = jwtUtil.generateToken(userDetails);

        return jwt;
    }

}
