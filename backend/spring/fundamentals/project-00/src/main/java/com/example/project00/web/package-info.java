/**
 * Web layer: REST controllers under {@code /api}, documented with OpenAPI annotations.
 * <p>
 * Controllers validate requests, delegate to the service layer and map entities to DTOs inside the
 * transaction. Errors are returned as {@code application/problem+json} by
 * {@code ApiExceptionHandler}. Swagger UI is served at {@code /swagger-ui.html}.
 * <p>
 * Access rules are declared next to each endpoint with {@code @PreAuthorize}; ownership checks use the
 * {@code @authz} bean ({@link com.example.project00.security.AuthorizationRules}).
 */
package com.example.project00.web;
