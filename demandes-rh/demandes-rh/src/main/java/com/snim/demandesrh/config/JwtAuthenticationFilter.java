package com.snim.demandesrh.config;

import com.snim.demandesrh.repository.UserRepository;
import com.snim.demandesrh.service.auth.TokenBlacklistService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistServicee tokenBlacklistService;
    private static final String BEARER = "Bearer";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Utilisation de resolveToken pour extraire le token de la requête
        String jwt = jwtUtils.resolveToken(request);

        // Si le jeton est absent ou dans la liste noire
        if (jwt == null || tokenBlacklistService.contains(jwt)) {
            // Optionnel : Ajoutez un log pour auditer cette situation
            System.out.println("JWT est manquant ou est sur la liste noire.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String userEmail = jwtUtils.extractUsername(jwt);

            // Si l'utilisateur n'est pas déjà authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new EntityNotFoundException("User with email " + userEmail + " not found while validating JWT"));

                // Validation du token
                if (jwtUtils.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Authentification de l'utilisateur
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    // Si le token est invalide ou expiré
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401 Unauthorized
                    response.getWriter().write("Invalid or expired JWT token");
                    return;  // Arrête le filtre ici si le token n'est pas valide
                }
            }

        } catch (Exception e) {
            // En cas d'erreur, renvoyer une réponse 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Error processing JWT token: " + e.getMessage());
            return;
        }

        // Continuez le traitement de la requête si tout est valide
        filterChain.doFilter(request, response);
    }



}