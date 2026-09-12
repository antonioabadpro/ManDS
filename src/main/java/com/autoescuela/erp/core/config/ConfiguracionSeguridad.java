package com.autoescuela.erp.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad
{
        @Bean
        public PasswordEncoder passwordEncoder()
        {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain cadenaFiltroSeguridad(HttpSecurity http) throws Exception
        {
                http.authorizeHttpRequests(auth -> auth
                                // Recursos estáticos públicos
                                .requestMatchers("/css/**", "/js/**", "/imagenes/**", "/webjars/**", "/favicon.ico").permitAll()

                                // Rutas públicas (Login, registro, recuperación)
                                .requestMatchers("/", "/login", "/registro", "/recuperar-password").permitAll()

                                // Consola H2 para desarrollo y pruebas locales
                                .requestMatchers("/h2-console/**").permitAll()

                                // Rutas protegidas por Roles
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/profesor/**").hasRole("PROFESOR")
                                .requestMatchers("/alumno/**").hasRole("ALUMNO")

                                // Cualquier otra petición requiere autenticación
                                .anyRequest().authenticated())
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/h2-console/**"))
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()))
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());
                return http.build();
        }

}
