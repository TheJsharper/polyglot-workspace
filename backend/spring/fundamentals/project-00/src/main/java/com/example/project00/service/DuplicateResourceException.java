package com.example.project00.service;

public class DuplicateResourceException extends RuntimeException {

	public DuplicateResourceException(String resource, Object key) {
		super(resource + " '" + key + "' already exists");
	}

}
