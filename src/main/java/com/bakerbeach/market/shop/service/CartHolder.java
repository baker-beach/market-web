package com.bakerbeach.market.shop.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.xcart.api.service.XCartService;
import com.bakerbeach.market.xcart.api.service.XCartServiceException;

public class CartHolder {
	protected static final Logger log = LoggerFactory.getLogger(CartHolder.class);

	private static final String CART_SESSION_ATTRIBUTES_KEY = "cart_request_attribute";

	public static Cart getInstance(XCartService cartService, String shopCode, Customer customer) {
		try {
			Cart cart = CartHolder.getInstance();
			if (cart == null || shopCode.equals(cart.getShopCode())) {
				cart = cartService.getInstance(shopCode, customer);
				CartHolder.setInstance(cart);
			}

			return cart;
		} catch (XCartServiceException e) {
			log.error(ExceptionUtils.getStackTrace(e));
			return null;
		}
	}

	public static Cart getNewInstance(XCartService cartService, String shopCode, Customer customer) {
		try {
			return cartService.getNewInstance(shopCode, customer);
		} catch (XCartServiceException e) {
			log.error(ExceptionUtils.getStackTrace(e));
			return null;
		}
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
