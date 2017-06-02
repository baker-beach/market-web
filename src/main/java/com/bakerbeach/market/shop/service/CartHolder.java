package com.bakerbeach.market.shop.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.bakerbeach.market.cart.api.service.CartService;
import com.bakerbeach.market.cart.api.service.CartServiceException;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;

public class CartHolder {
	protected static final Logger log = LoggerFactory.getLogger(CartHolder.class);

	private static final String CART_SESSION_ATTRIBUTES_KEY = "cart_request_attribute";

	public static Cart getInstance(CartService cartService, ShopContext shopContext, Customer customer) {
		try {			
			Cart cart = CartHolder.getInstance();
			if (cart == null || !shopContext.getShopCode().equals(cart.getShopCode())) {
				cart = cartService.getInstance(shopContext, customer);
				CartHolder.setInstance(cart);
			}

			return cart;
		} catch (CartServiceException e) {
			log.error(ExceptionUtils.getStackTrace(e));
			return null;
		}
	}

	public static Cart getNewInstance(CartService cartService, ShopContext shopContext, Customer customer) {
		try {
			return cartService.getNewInstance(shopContext, customer);
		} catch (CartServiceException e) {
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
