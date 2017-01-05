package com.bakerbeach.market.shop.model.forms;

import org.hibernate.validator.constraints.NotEmpty;

public class SimpleNameRegistrationForm extends AbstractRegistrationForm {
	
	@NotEmpty(message = "register.error.first_name")
	public String getRegisterFirstName() {
		return registerFirstName;
	}
	
	@NotEmpty(message = "register.error.last_name")
	public String getRegisterLastName() {
		return registerLastName;
	}

}
