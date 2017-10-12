package com.bakerbeach.market.shop.model.forms;

import java.io.Serializable;

import javax.validation.Valid;

public class SimpleBillingAddressForm implements Serializable{

	@Valid
	public SimpleAddressForm billingAddress;

	public SimpleAddressForm getBillingAddress() {
		return billingAddress;
	}
	
	public void setBillingAddress(SimpleAddressForm billingAddress) {
		this.billingAddress = billingAddress;
	}
	
}
