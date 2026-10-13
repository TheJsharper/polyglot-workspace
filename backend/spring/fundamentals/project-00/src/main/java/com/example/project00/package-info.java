/**
 * University API, a layered Spring Boot application.
 * <p>
 * Dependencies point downwards only:
 * <ol>
 * <li>{@link com.example.project00.web} - REST controllers, DTOs, error mapping and OpenAPI docs</li>
 * <li>{@link com.example.project00.service} - transactional use cases and business rules</li>
 * <li>{@link com.example.project00.repository} - Spring Data JPA persistence</li>
 * <li>{@link com.example.project00.domain} - JPA entities with their invariants</li>
 * </ol>
 */
package com.example.project00;
