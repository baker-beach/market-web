package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;

@Component("com.bakerbeach.market.shop.box.CheckoutConfirmationBox")
@Scope("prototype")
public class CheckoutConfirmationBox extends AbstractBox {
	private static final long serialVersionUID = 1L;

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		super.handleRenderRequest(request, response, model);
	}

}