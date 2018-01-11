package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.order.api.model.OrderList;
import com.bakerbeach.market.order.api.service.OrderService;
import com.bakerbeach.market.order.api.service.OrderServiceException;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.AccountDashboardOrderBox")
@Scope("prototype")
public class AccountDashboardOrderBox extends AbstractBox  implements ProcessableBox {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private OrderService orderService;

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {
		
		ShopContext shopContext = ShopContextHolder.getInstance();
		
		Customer customer = CustomerHelper.getCustomer();
		
		try {
			OrderList orderList = orderService.findOrderByCustomerIdAndShopCode(customer.getId(), shopContext.getShopCode(), "createdAt+", 5, 0);
			getData().put("orderList", orderList);
		} catch (OrderServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
