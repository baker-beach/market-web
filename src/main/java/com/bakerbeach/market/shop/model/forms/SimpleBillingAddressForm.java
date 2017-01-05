package com.bakerbeach.market.shop.model.forms;

import javax.validation.Valid;

public class SimpleBillingAddressForm {

	@Valid
	public SimpleAddressForm billingAddress;

	public SimpleAddressForm getBillingAddress() {
		return billingAddress;
	}
	
	public void setBillingAddress(SimpleAddressForm billingAddress) {
		this.billingAddress = billingAddress;
	}
	
}
