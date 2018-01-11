package com.bakerbeach.market.shop.box;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cart.api.service.CartService;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.model.PaymentInfo;
import com.bakerbeach.market.payment.api.service.PaymentService;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CheckoutStatusResolver;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.CheckoutPaymentBox")
@Scope("prototype")
public class CheckoutPaymentBox extends AbstractCheckoutStepBox {

	private static final long serialVersionUID = 1L;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private CartService cartService;

	@SuppressWarnings("unchecked")
	@Override
	protected void handleActionRequestForward(HttpServletRequest request, HttpServletResponse response,
			ModelMap modelMap) throws ProcessableBoxException {
		Customer customer = CustomerHelper.getCustomer();
		ShopContext shopContext = ShopContextHolder.getInstance();

		Cart cart = CartHolder.getInstance(cartService, shopContext, customer);

		if (request.getMethod().equals("GET")) {
			try {
				PaymentInfo paymentInfo = paymentService.initPayment(shopContext, customer, cart);
				modelMap.put("paymentInfo", paymentInfo);
			} catch (PaymentServiceException e) {
			}
		} else {
			try {
				Map<String, String[]> parameters = request.getParameterMap();
				Map<String, String> param = new HashMap<String, String>();
				for (String key : parameters.keySet()) {
					param.put(key, parameters.get(key)[0]);
				}
				PaymentInfo paymentInfo = paymentService.configPaymentMethod(shopContext, param);
				if (paymentInfo.isPaymentValid()) {
					shopContext.getValidSteps().add(CheckoutStatusResolver.STEP_PAYMENT);
					throw new RedirectException(new Redirect(checkoutStatusResolver.nextStepPageId(shopContext), null));
				}
				throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
			} catch (PaymentServiceException e) {
			}
		}

	}

	@Override
	public Integer getStep() {
		return CheckoutStatusResolver.STEP_PAYMENT;
	}
}
