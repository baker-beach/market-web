package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.payment.api.model.PaymentInfo;


@Component("com.bakerbeach.market.shop.box.PaymentMethodBox")
@Scope("prototype")
public class PaymentMethodBox extends AbstractBox implements ProcessableBox {

	private static final long serialVersionUID = 1L;

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {
		// TODO Auto-generated method stub

	}
	
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		String id = (String) getData().get("paymentMethodId");
		
		PaymentInfo paymentInfo = (PaymentInfo)modelMap.get("paymentInfo");
		
		if(!paymentInfo.getPaymentDataMap().containsKey(id)){
			setTemplate("/empty");
		}else{
			this.getData().put("payment_data", paymentInfo.getPaymentDataMap().get(id));
		}
	
	}

}
