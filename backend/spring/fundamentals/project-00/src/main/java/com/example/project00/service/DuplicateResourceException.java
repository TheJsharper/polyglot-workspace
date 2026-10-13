package com.example.project00.service;

/**
 * Thrown when creating a resource whose business key is already taken; mapped to HTTP 409 by the web layer.
 */
public class DuplicateResourceException extends RuntimeException {

	/**
	 * @param resource the resource type, e.g. {@code "Course"}
	 * @param key the duplicate key
	 */
	public DuplicateResourceException(String resource, Object key) {
		super(resource + " '" + key + "' already exists");
	}

}
