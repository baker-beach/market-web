package com.bakerbeach.market.shop.service;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.xcart.api.service.XCartService;

public class CartHolder {
	private static final String CART_SESSION_ATTRIBUTES_KEY = "cart_request_attribute";

	public static Cart getInstance(XCartService cartService, Customer customer) {
		Cart cart = CartHolder.getInstance();
		if (cart == null) {
			cart = cartService.getInstance(customer);
			CartHolder.setInstance(cart);
		}

		return cart;
	}

	public static Cart getNewInstance(XCartService cartService, Customer customer) {
		return cartService.getNewInstance(customer);
	}

	public static Cart getInstance() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Cart cart = (Cart) requestAttributes.getAttribute(CART_SESSION_ATTRIBUTES_KEY, RequestAttributes.SCOPE_SESSION);
		return cart;
	}

	public static void setInstance(Cart cart) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		requestAttributes.setAttribute(CART_SESSION_ATTRIBUTES_KEY, cart, RequestAttributes.SCOPE_SESSION);
	}

}
