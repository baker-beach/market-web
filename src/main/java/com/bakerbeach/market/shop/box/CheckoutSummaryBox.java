package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cart.api.service.CartService;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CheckoutStatusResolver;
import com.bakerbeach.market.shop.service.CustomerHelper;

@Component("com.bakerbeach.market.shop.box.CheckoutSummaryBox")
@Scope("prototype")
public class CheckoutSummaryBox extends AbstractCheckoutStepBox {
	private static final long serialVersionUID = 1L;

	@Autowired
	private CartService cartService;

	@Override
	protected void handleActionRequestForward(HttpServletRequest request, HttpServletResponse response,
			ModelMap modelMap) throws ProcessableBoxException {
	}
	
	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		Customer customer = CustomerHelper.getCustomer();
		Cart cart = CartHolder.getInstance(cartService, customer);
		getData().put("cart", cart);
	}

	@Override
	public Integer getStep() {
		// TODO Auto-generated method stub
		return CheckoutStatusResolver.STEP_SUMMARY;
	}
	
	@Override
	protected boolean preHandleActionRequestForward(HttpServletRequest request, HttpServletResponse response,
			ModelMap modelMap) throws ProcessableBoxException {
		return true;
	}
	
}
