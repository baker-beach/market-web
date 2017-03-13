package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.xcart.api.service.XCartService;

@Component("com.bakerbeach.market.shop.box.CurrencyBox")
@Scope("prototype")
public class CurrencyBox extends AbstractBox implements ProcessableBox{	
	private static final long serialVersionUID = 1L;

	@Autowired
	private XCartService cartService;

	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws RedirectException{
		ShopContext shopContext = ShopContextHolder.getInstance();
		String shopCode = shopContext.getShopCode();

		Customer customer = CustomerHelper.getCustomer();

		Cart cart = CartHolder.getInstance(cartService, shopCode, customer);
		
		if(shopContext.getRequestData().containsKey("currency")){
			String currency = (String) shopContext.getRequestData().get("currency");
			shopContext.setCurrency(currency);
			cartService.setCurrency(shopContext, cart, customer);
			
			throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
		}
	}
}
