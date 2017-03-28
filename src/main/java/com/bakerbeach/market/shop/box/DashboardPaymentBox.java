package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.model.PaymentInfo;
import com.bakerbeach.market.payment.api.service.PaymentService;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.DashboardPaymentBox")
@Scope("prototype")
public class DashboardPaymentBox extends AbstractBox {
	
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private PaymentService paymentService;
	
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		ShopContext shopContext = ShopContextHolder.getInstance();
		PaymentInfo paymentInfo = paymentService.getPaymentInfo(shopContext);
		getData().put("payment_info", paymentInfo.getPaymentDataMap().get(paymentInfo.getCurrentPaymentMethodCode()));	
		modelMap.put("payment_info", paymentInfo.getPaymentDataMap().get(paymentInfo.getCurrentPaymentMethodCode()));
	}
	
	
}
