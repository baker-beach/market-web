package com.bakerbeach.market.shop.box;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.bakerbeach.market.address.api.service.CustomerAddressService;
import com.bakerbeach.market.address.api.service.CustomerAdressServiceException;
import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.CartItemQualifier;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.CustomerAddress;
import com.bakerbeach.market.core.api.model.Messages;
import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.order.api.service.OrderService;
import com.bakerbeach.market.order.api.service.OrderServiceException;
import com.bakerbeach.market.payment.api.model.PaymentInfo;
import com.bakerbeach.market.payment.api.service.PaymentService;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.api.service.PaymentServiceException.PaymentRedirectException;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CheckoutStatusResolver;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.xcart.api.service.XCartService;

@Component("com.bakerbeach.market.shop.box.CheckoutBox")
@Scope("prototype")
public class CheckoutBox extends AbstractBox implements ProcessableBox {

	private static final long serialVersionUID = 1L;
	
	@Autowired(required = false)
	protected CheckoutStatusResolver checkoutStatusResolver = new CheckoutStatusResolver();

	protected static Logger log = Logger.getLogger(CheckoutBox.class.getName());

	@Autowired
	private XCartService cartService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private CustomerAddressService addressService;

	@Autowired
	private OrderService orderService;

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws ProcessableBoxException {
		ShopContext shopContext = ShopContextHolder.getInstance();
		String shopCode = shopContext.getShopCode();
		Customer customer = CustomerHelper.getCustomer();
		
		Cart cart = CartHolder.getInstance(cartService, shopCode, customer);
		
		if (shopContext.getRequestData().containsKey("doOrder")) {
			shopContext.getValidSteps().add(CheckoutStatusResolver.STEP_SUMMARY);
			if (checkoutStatusResolver.nextStepID(shopContext) == CheckoutStatusResolver.STEP_ORDER)
				doOrder(request);
				throw new RedirectException(new Redirect(getNextCheckoutStep(shopContext, cart), null));
		} else {
			shopContext.getValidSteps().remove(CheckoutStatusResolver.STEP_SUMMARY);
			FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
			flashMap.put("checkout", 1);
			flashMap.put("messages", model.get("messages"));
			if (cart.findItemsByQualifier(CartItemQualifier.PRODUCT, CartItemQualifier.VPRODUCT).size() < 1)
				throw new RedirectException(new Redirect("cart", null));
			else
				throw new RedirectException(new Redirect(getNextCheckoutStep(shopContext, cart), null));
		}

	}

	private void doOrder(HttpServletRequest request) throws ProcessableBoxException {
		ShopContext shopContext = ShopContextHolder.getInstance();
		String shopCode = shopContext.getShopCode();
		Customer customer = CustomerHelper.getCustomer();
		
		Cart cart = CartHolder.getInstance(cartService, shopCode, customer);

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);

		try {
			paymentService.doPreOrder(cart, shopContext);
		} catch (PaymentRedirectException pre) {

			throw new RedirectException(new Redirect(pre.getUrl(), null, Redirect.RAW));

		} catch (PaymentServiceException pe) {
			throw new RedirectException(new Redirect("checkout-summary", null));
		}
 
		try {
			Order order = orderService.order(cart, customer, shopContext);
			shopContext.getValidSteps().add(CheckoutStatusResolver.STEP_ORDER);
			Redirect redirect = new Redirect(getNextCheckoutStep(shopContext, cart), null);
			shopContext.setOrderId(null);
			shopContext.getValidSteps().clear();
			cartService.clear(cart);
			
			flashMap.put("order", order);

			throw new RedirectException(redirect);
		} catch (OrderServiceException e) {
			
			Messages messages = e.getMessages();
			if (messages != null && !messages.isEmpty()) {
				flashMap.put("messages", messages);				
			}
			
			shopContext.getValidSteps().remove(CheckoutStatusResolver.STEP_SUMMARY);
			throw new RedirectException(new Redirect(getNextCheckoutStep(shopContext, cart), null));
		}
	}



	private void initCheckout(ShopContext shopContext, Customer customer, Cart cart) {

		try {
			Map<String, CustomerAddress> adddresses = addressService.findDefaultsByCustomer(customer);
			if (!adddresses.isEmpty() && shopContext
					.isCountryValid(adddresses.get(CustomerAddress.TAG_DEFAULT_SHIPPING_ADDRESS).getCountryCode())) {

				shopContext.setBillingAddress(adddresses.get(CustomerAddress.TAG_DEFAULT_BILLING_ADDRESS));
				shopContext.setShippingAddress(adddresses.get(CustomerAddress.TAG_DEFAULT_SHIPPING_ADDRESS));
				shopContext.getValidSteps().add(CheckoutStatusResolver.STEP_ADDRESS);
			} else {
				shopContext.getValidSteps().remove(CheckoutStatusResolver.STEP_ADDRESS);
			}
		} catch (CustomerAdressServiceException e) {
			shopContext.getValidSteps().remove(CheckoutStatusResolver.STEP_ADDRESS);
		}

		try {
			PaymentInfo paymentInfo = paymentService.initPayment(shopContext, customer, cart);
			if (paymentInfo.isPaymentValid())
				shopContext.getValidSteps().add(CheckoutStatusResolver.STEP_PAYMENT);
			else
				shopContext.getValidSteps().remove(CheckoutStatusResolver.STEP_PAYMENT);
		} catch (PaymentServiceException e) {
			shopContext.getValidSteps().remove(CheckoutStatusResolver.STEP_PAYMENT);
		}

		shopContext.getValidSteps().add(CheckoutStatusResolver.STEP_INIT);
	}

	private String getNextCheckoutStep(ShopContext shopContext, Cart cart) {
		Customer customer = CustomerHelper.getCustomer();

		if (shopContext.getOrderId() == null) {
			try {
				shopContext.setOrderId(orderService.getNextOrderId(shopContext));
			} catch (OrderServiceException e) {
			}
		}

		if (!shopContext.getValidSteps().contains(CheckoutStatusResolver.STEP_INIT)) {
			initCheckout(shopContext, customer, cart);

			// calculating the cart after initCheckout seems to be a good idea
			// to be aware of e.g shipping fees.
			cartService.calculate(shopContext, cart, customer);
		} else {
			PaymentInfo paymentInfo = paymentService.getPaymentInfo(shopContext);
			if (paymentInfo.isPaymentValid())
				shopContext.getValidSteps().add(CheckoutStatusResolver.STEP_PAYMENT);

		}

		if (shopContext.getShippingAddress() != null) {
			if (!shopContext.isCountryValid(shopContext.getShippingAddress().getCountryCode())) {
				shopContext.setShippingAddress(null);
				shopContext.getValidSteps().remove(CheckoutStatusResolver.STEP_ADDRESS);
			}
		}

		return checkoutStatusResolver.nextStepPageId(shopContext);
	}

}
