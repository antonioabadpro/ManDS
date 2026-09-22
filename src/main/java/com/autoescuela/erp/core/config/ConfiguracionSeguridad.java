package com.autoescuela.erp.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.autoescuela.erp.core.security.HtmxAuthenticationEntryPoint;
import com.autoescuela.erp.core.security.RedireccionPorRolSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class ConfiguracionSeguridad
{
    private final RedireccionPorRolSuccessHandler redireccionPorRolSuccessHandler;
    private final HtmxAuthenticationEntryPoint htmxAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    /**
     * Publicador de eventos de sesión HTTP obligatorio para que Spring Security
     * controle con precisión la concurrencia de sesiones (1 sesión activa por usuario).
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher()
    {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http.authorizeHttpRequests(auth -> auth
                // Recursos estáticos públicos
                .requestMatchers("/css/**", "/js/**", "/imagenes/**", "/webjars/**", "/favicon.ico").permitAll()

                // Rutas públicas (Login, registro, recuperación de contraseña, activación de cuenta)
                .requestMatchers("/", "/login", "/registro/**", "/recuperar-password/**", "/activar-cuenta/**").permitAll()

                // Webhook de Stripe (verificado criptográficamente por firma en el controlador)
                .requestMatchers("/pagos/webhook/**").permitAll()

                // Consola H2 para desarrollo y pruebas locales
                .requestMatchers("/h2-console/**").permitAll()

                // Rutas protegidas por Roles
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/profesor/**").hasRole("PROFESOR")
                .requestMatchers("/alumno/**").hasRole("ALUMNO")

                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated())

                // Configuración de CSRF: habilitado globalmente, excluyendo consola H2 y Webhook de Stripe
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/pagos/webhook/**"))

                // Cabeceras de seguridad
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))

                // Manejo de excepciones con soporte específico para peticiones HTMX
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(this.htmxAuthenticationEntryPoint))

                // Formulario de login con soporte dual (username o correo) y redirección dinámica por rol
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(this.redireccionPorRolSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll())

                // Cierre de sesión seguro con invalidación total de estado
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                // Gestión de sesiones: prevención de fijación de sesión y limitación estricta a 1 sesión por usuario
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.migrateSession())
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expirada=true"));

        return http.build();
    }
}
