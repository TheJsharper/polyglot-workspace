/**
 * Service layer: transactional use cases addressed by business keys (course code, employee id, student id) or
 * ids.
 * <p>
 * Failures are signalled with {@link com.example.project00.service.ResourceNotFoundException},
 * {@link com.example.project00.service.DuplicateResourceException}, {@link IllegalStateException} (operation
 * not allowed in the current state) and {@link IllegalArgumentException} (invalid input).
 */
package com.example.project00.service;
