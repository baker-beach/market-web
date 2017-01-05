package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.DashboardAddressBox")
@Scope("prototype")
public class DashboardAddressBox extends AbstractBox {

	private static final long serialVersionUID = 1L;

	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model) {

		ShopContext shopContext = ShopContextHolder.getInstance();

		getData().put("billingAddress", shopContext.getBillingAddress());
		getData().put("shippingAddress", shopContext.getShippingAddress());

		if (shopContext.getBillingAddress().getId() == null)
			getData().put("useShipping", false);
		else if (shopContext.getBillingAddress().getId().equals(shopContext.getShippingAddress().getId()))
			getData().put("useShipping", false);
		else
			getData().put("useShipping", true);

	}

}
