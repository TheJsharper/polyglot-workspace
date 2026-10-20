package com.example.project00.service;

public class CreationLimitExceededException extends RuntimeException {

	public CreationLimitExceededException(String resource) {
		super("The maximum of 10 created " + resource.toLowerCase() + " records has been reached");
	}
}
