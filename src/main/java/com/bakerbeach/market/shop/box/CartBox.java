package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cart.api.service.CartService;
import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.CartBox")
@Scope("prototype")
public class CartBox extends AbstractBox {
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EMPTY_CART_TMPL = "/cartEmpty";
	String emptyCartTemplate = DEFAULT_EMPTY_CART_TMPL;

	@Autowired
	private CartService cartService; 

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		ShopContext shopContext = ShopContextHolder.getInstance();

		Customer customer = CustomerHelper.getCustomer();
		Cart cart = CartHolder.getInstance(cartService, shopContext, customer);
		getData().put("cart", cart);
	}

}
