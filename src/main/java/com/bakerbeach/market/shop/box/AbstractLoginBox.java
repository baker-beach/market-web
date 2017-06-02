package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.bakerbeach.market.cart.api.service.CartService;
import com.bakerbeach.market.cart.api.service.CartServiceException;
import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.cms.service.Helper;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.ShopContextHolder;

public abstract class AbstractLoginBox extends AbstractBox implements ProcessableBox {
	private static final long serialVersionUID = 1L;

	@Autowired
	private CartService cartService;

	protected Redirect onSuccessfulAuthentication(HttpServletRequest request, Helper helper) {
		ShopContext shopContext = ShopContextHolder.getInstance();

		Subject subject = SecurityUtils.getSubject();
		Customer customer = (Customer) subject.getPrincipal();

		Cart cart = CartHolder.getInstance(cartService, shopContext, customer);
		try {
			if (cart != null && !customer.getId().equals(cart.getCustomerId())) {
				Cart lastActiveCart = cartService.loadActiveCart(shopContext, customer);
				if (lastActiveCart != null) {
					cartService.merge(cart, lastActiveCart);
					CartHolder.setInstance(lastActiveCart);
					cartService.saveCart(customer, lastActiveCart);
					cartService.calculate(shopContext, lastActiveCart, customer);
				} else {
					cart.setCustomerId(customer.getId());
					cartService.saveCart(customer, cart);
				}
			}
		} catch (CartServiceException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}

		return getRedirect(request);
	}

	private Redirect getRedirect(HttpServletRequest request) {
		SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
		if (savedRequest != null) {
			String savedUrl = savedRequest.getRequestUrl();
			String contextPath = request.getContextPath();
			String url = savedUrl.replaceAll("^" + contextPath, "");
			return new Redirect(url, null, Redirect.RAW);
		} else {
			return new Redirect("index", null, Redirect.URL_ID);
		}
	}

	protected void doLogin(String email, String password, boolean rememberMe) throws AuthenticationException {
		Subject subject = SecurityUtils.getSubject();

		UsernamePasswordToken token = new UsernamePasswordToken(email, password);
		token.setRememberMe(rememberMe);
		subject.login(token);
	}

}
