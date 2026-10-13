/**
 * Security layer: stateless JWT authentication and role/ownership authorization.
 * <ul>
 * <li>{@code SecurityConfig} – filter chain, HS256 JWT encoder/decoder, BCrypt and the authentication manager.</li>
 * <li>{@link com.example.project00.security.TokenService} – issues access tokens at login.</li>
 * <li>{@code AccountUserDetailsService} – resolves the configured admin and the stored student/professor
 * accounts.</li>
 * <li>{@link com.example.project00.security.AuthorizationRules} – the {@code @authz} bean used in
 * {@code @PreAuthorize} expressions for ownership rules.</li>
 * </ul>
 * Roles: {@code STUDENT} (self-registered), {@code PROFESSOR} (created by the admin) and {@code ADMIN} (the
 * superior, defined in {@code app.security.admin.*}).
 */
package com.example.project00.security;
