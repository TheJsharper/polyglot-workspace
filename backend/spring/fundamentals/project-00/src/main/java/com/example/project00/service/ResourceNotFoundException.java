package com.example.project00.service;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String resource, Object key) {
		super(resource + " '" + key + "' not found");
	}

}
