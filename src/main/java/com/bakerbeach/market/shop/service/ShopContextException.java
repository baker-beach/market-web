package com.bakerbeach.market.shop.service;

public class ShopContextException extends Exception {
	private static final long serialVersionUID = 1L;

	public ShopContextException() {
		super();
	}

	public ShopContextException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ShopContextException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShopContextException(String message) {
		super(message);
	}

	public ShopContextException(Throwable cause) {
		super(cause);
	}
	
}
