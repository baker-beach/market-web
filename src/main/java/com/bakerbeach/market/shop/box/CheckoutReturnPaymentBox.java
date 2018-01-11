package com.bakerbeach.market.shop.box;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentService;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.shop.service.CheckoutStatusResolver;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.CheckoutReturnPaymentBox")
@Scope("prototype")
public class CheckoutReturnPaymentBox extends AbstractBox implements ProcessableBox {

	private static final long serialVersionUID = 1L;

	@Autowired
	private PaymentService paymentService;
	
	@Autowired(required = false)
	protected CheckoutStatusResolver checkoutStatusResolver = new CheckoutStatusResolver();

	@SuppressWarnings("unchecked")
	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws ProcessableBoxException {

		ShopContext shopContext = ShopContextHolder.getInstance();

		Map<String, String[]> parameters = request.getParameterMap();
		Map<String, String> param = new HashMap<String, String>();
		for (String key : parameters.keySet()) {
			param.put(key, parameters.get(key)[0]);
		}

		param.put("result", (String) shopContext.getRequestData().get("mode"));

		try {
			paymentService.processReturn(shopContext, param);
		} catch (PaymentServiceException e) {
			shopContext.getValidSteps().remove(CheckoutStatusResolver.STEP_PAYMENT);
			if(shopContext.getRequestData().containsKey("iframe_redirect")){
				request.getSession().setAttribute("messages", e.getMessages());
			}else			
				throw new RedirectException(new Redirect(checkoutStatusResolver.nextStepPageId(shopContext), null));
		}

		if (shopContext.getRequestData().containsKey("redirect"))
			throw new RedirectException(new Redirect((String) shopContext.getRequestData().get("redirect"), null));
		else if (shopContext.getRequestData().containsKey("iframe_redirect"))
			shopContext.getRequestData().put("page_id", (String) shopContext.getRequestData().get("iframe_redirect"));
		else
			throw new RedirectException(new Redirect(checkoutStatusResolver.nextStepPageId(shopContext), null));

	}

}
