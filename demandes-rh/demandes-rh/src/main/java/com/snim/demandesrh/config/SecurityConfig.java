package com.snim.demandesrh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.snim.demandesrh.handlers.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartResolver;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

        @Bean(name = "globalExceptionHandler") // Ensure no duplicate name
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        ar -> ar.requestMatchers(
                                "/auth/authenticate",
                                  "/auth/logout",

                                        "/auth/register",
                                        "/api/test/hello",
                                        // -- swagger ui
                                        "/swagger-ui.html",
                                        //"/sap-users/export",
                                        "/v2/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**",
                                        "/swagger-ui/**"
                                //
                )
                .permitAll())
                .authorizeHttpRequests(
                        ar -> ar.requestMatchers("/api/users/all").hasRole("ADMIN")
                                .requestMatchers("/api/employee/current").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/demandes/save").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/employee/username/{id}").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/users/admins").hasRole("ADMIN")
                                .requestMatchers("/api/demandes/{demande-id}").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/demandes/countDemandeurs").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/users/res").hasRole("ADMIN")
                                .requestMatchers("/api/users/check-role").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/api/users/current-id").hasAnyRole("ADMIN","RES","USER")
                                .requestMatchers("/api/users/{userId}/role").hasRole("ADMIN")
                                .requestMatchers("/api/users/mat/{matricule}").hasRole("ADMIN")
                                .requestMatchers("/api/users/{userId}/status").hasRole("ADMIN")
                                .requestMatchers("/api/users/{userId}/both").hasRole("ADMIN")
                                .requestMatchers("/api/users/change-password").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/users/{userId}/active").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/notifications/generateMessage").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/demandes/create").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/countMat").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/count").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/categories/count").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/{demandeId}/employee-id").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/{demandeId}/matricule").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/{demandeId}").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/demandes/dem-top").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/rec-top").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/users/photo").hasAnyRole("ADMIN", "RES", "USER", "OBS")
                                .requestMatchers("/api/historiques/demande/{demandeId}").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/users/{matricule}/change-role").hasRole("ADMIN")
                                .requestMatchers("/api/users/{userId}/roles").hasRole("ADMIN")
                                .requestMatchers("/api/users/{matricule}/active").hasRole("ADMIN")
                                .requestMatchers("/api/demandes/{demande-id}/desc").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/list").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/demandes/archives").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/u/{demande-id}").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/d/{demande-id}").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/employee/matricules").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/api/employee/current-employee").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/api/demandes/modified").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/{demandeId}/upload").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/messages/demande/{demandeId}").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/users/change-password").hasAnyRole("ADMIN", "RES","USER","OBS")
                                .requestMatchers("/api/users/role").hasAnyRole("ADMIN", "RES","USER","OBS")
                                .requestMatchers("/api/messages").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/messages/messages").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/messages/between").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/messages/for-demande").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/employee/c").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/employee/{id}").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/employee/d/{id}").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/employee/").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/employee/{id}").hasAnyRole("ADMIN", "RES","USER")

                                .requestMatchers("/api/messages/receiverId/{demandeId}").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/messages/senderId/{demandeId}").hasAnyRole("ADMIN", "RES","USER")
                                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/users/info").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/users/{id}").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/users/{userId}/employee").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/users/current").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/auth/current-user").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/count-demandeurs").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/createWithFile").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/{demandeId}/piece-jointe").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/api/documents").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/api/documents/get/{id}").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/api/documents/getByDemandeId/{demandeId}").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/documents/getByReclamationId/{reclamationId}").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/demandes/user-list").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/employee/update-photo").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/employee/username").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/employee/profile-photo").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/demandes/demandes").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/demandes/demandes/count").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/messages/conversation").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/messages/send").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/messages/getFilesByDemandeId/{demandeId}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/messages/file").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/messages/files/{filename:.+}").hasAnyRole("ADMIN", "RES", "USER", "OBS")
                                .requestMatchers("/api/messages/sendFile").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/messages/send/{employeeId}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/messages/demande/{demandeId}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/reclamations/save").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/reclamations/list").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/api/reclamations/{reclamation-id}").hasAnyRole("ADMIN", "RES", "USER")
                                .requestMatchers("/api/reclamations/{reclamation-id}/update").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/reclamations/up/{reclamation-id}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/reclamations/del/{reclamation-id}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/reclamations/{reclamation-id}/delete").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/reclamations/count").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/reclamations/reclamations/count").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/reclamations/categories/count").hasAnyRole("ADMIN", "RES")
                                .requestMatchers("/api/reclamations/paginated").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/notifications/{notificationId}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/notifications/{username}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/notifications/user/{userId}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/chat-socket/**").permitAll()
                                .requestMatchers("/api/messages/{roomId}").permitAll()
                                .requestMatchers("/chat-socket/chat/{roomId}").permitAll()
                                .requestMatchers("/chat-socket/topic/{roomId}").permitAll()
                                .requestMatchers("/api/demandes/userMatricule/{demandeId}").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/feedbacks").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .requestMatchers("/api/feedbacks/level").hasAnyRole("ADMIN", "RES","USER", "OBS")
                                .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8085"));
        configuration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;

    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }


}