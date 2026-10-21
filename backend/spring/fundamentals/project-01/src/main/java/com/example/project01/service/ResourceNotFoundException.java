package com.example.project01.service;

/** Thrown when a requested resource does not exist. */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String what, Object id) {
		super(what + " " + id + " not found");
	}

}
