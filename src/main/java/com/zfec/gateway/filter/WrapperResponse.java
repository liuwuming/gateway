package com.zfec.gateway.filter;

public class WrapperResponse {
	int statusCode;
	String message;
	
	public WrapperResponse(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}
}
