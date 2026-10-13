package com.example.project00.service;

/**
 * Thrown when a resource looked up by its key does not exist; mapped to HTTP 404 by the web layer.
 */
public class ResourceNotFoundException extends RuntimeException {

	/**
	 * @param resource the resource type, e.g. {@code "Course"}
	 * @param key the key that was looked up
	 */
	public ResourceNotFoundException(String resource, Object key) {
		super(resource + " '" + key + "' not found");
	}

}
