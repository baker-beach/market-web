package com.bakerbeach.market.shop.model;

import com.bakerbeach.market.commons.SessionContext;
import com.bakerbeach.market.core.api.model.Address;
import com.bakerbeach.market.payment.api.model.PaymentInfo;

public class ShopSessionContext implements SessionContext {
	
	private Address billingAddress;
	private Address shippingAddress;
	private String orderId;
	private PaymentInfo paymentInfo;
	private String currentPriceGroup;

}
