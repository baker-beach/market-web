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
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CustomerHelper;

@Component("com.bakerbeach.market.shop.box.HeaderBox")
@Scope("prototype")
public class HeaderBox extends AbstractBox{

	private static final long serialVersionUID = 1L;
		
	@Autowired
	private CartService cartService;
	
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		Customer customer = CustomerHelper.getCustomer();
		getData().put("customer", customer);

		Boolean authenticated = CustomerHelper.isCustomer();
		getData().put("authenticated", authenticated);
		
		Cart cart = CartHolder.getInstance(cartService, customer);
		getData().put("cart", cart);
	}
}





//		Helper helper = (Helper) modelMap.get("helper");

//		String customerStatus = "anonymous";
//		Boolean isAuthenticated = helper.isAuthenticated();
//		Boolean isIdentified = helper.isIdentified();
//
//		String customerStatus = "anonymous";
//
//		Customer customer = CustomerHelper.getCustomer();
//		if(customer != null) {
//			customerStatus = "recognized";
//			if (customer.is)
//		}

//	public Boolean isIdentified() {
//		Object principal = SecurityUtils.getSubject().getPrincipal();
//		return principal != null && !(principal instanceof AnonymousCustomer);
//	}
//
//		Boolean isAuthenticated = SecurityUtils.getSubject().isAuthenticated();
//
//
//
//
//		String customerName = "";
//
//		if (customer.getFirstName() != null)
//			customerName = customer.getFirstName();
//
//		if (customer.getLastName() != null)
//			customerName = customerName + " " + customer.getLastName();
//
//		if (customerName.equals(""))
//			customerName = "Lieber Kunde";
//
//		getData().put("customerName", customerName);
