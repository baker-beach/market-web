package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.TrackingBox")
@Scope("prototype")
public class TrackingBox extends AbstractBox {
	private static final long serialVersionUID = 1L;

	private static final String ACCOUNT_ID = "ACCOUNT_ID";
	private static final String ORDER = "order";

	public String getAccountId() {
		return (String) getData().get(ACCOUNT_ID);
	}

	public Order getOrder() {
		return (Order) getData().get(ORDER);
	}

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		ShopContext cmsContext = ShopContextHolder.getInstance();
		getData().put(ACCOUNT_ID, cmsContext.getGtmId());

		Order order = null;
		if(modelMap.containsKey(ORDER)){
			order = (Order) modelMap.get(ORDER);
		}
		if(order != null)
			getData().put(ORDER, order);

		ProductBox productBox = (ProductBox) modelMap.get("productBox0");
		if(productBox != null){
			getData().put("product", productBox.getData().get("product"));
			getData().put("deal", productBox.getData().get("deal"));
		}

		Customer customer = CustomerHelper.getCustomer();
		if(customer != null)
			getData().put("customerId", customer.getId());

		super.handleRenderRequest(request, response, modelMap);
	}

	@Override
	public String toString() {
		return "";
	}

}
