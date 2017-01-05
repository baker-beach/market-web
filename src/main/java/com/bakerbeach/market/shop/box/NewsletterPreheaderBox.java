package com.bakerbeach.market.shop.box;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.customer.model.AnonymousCustomer;
import com.bakerbeach.market.shop.service.CookieHelper;
import com.bakerbeach.market.shop.service.CustomerHelper;

@Component
@Scope("prototype")
public class NewsletterPreheaderBox extends AbstractBox {
	private static final long serialVersionUID = 1L;

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		Cookie cookie = CookieHelper.getCookie(request, "newsletterPopup");		
		if (cookie == null) {		
			Customer customer = CustomerHelper.getCustomer();
			if (customer instanceof AnonymousCustomer) {
				getData().put("show", true);
		
				cookie = CookieHelper.createCookie("newsletterPopup", "1");
				cookie.setMaxAge(365*24*60*60);
				response.addCookie(cookie);
			}
		}

		super.handleRenderRequest(request, response, modelMap);
	}
}
