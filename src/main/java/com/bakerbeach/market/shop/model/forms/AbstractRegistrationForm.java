package com.bakerbeach.market.shop.model.forms;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

public class AbstractRegistrationForm implements RegisterForm{
	
	protected String registerPassword;
	protected String registerPasswordConfirm;
	protected String registerEmail;	
	protected String registerPrefix;
	protected String registerFirstName;
	protected String registerMiddleName;
	protected String registerLastName;
	protected String registerSuffix;
	
	@NotEmpty(message = "register.error.password")
	@Size(min = 6, message = "register.error.password")
	public String getRegisterPassword() {
		return registerPassword;
	}
	
	public void setRegisterPassword(String registerPassword) {
		this.registerPassword = registerPassword;
	}
	
	@NotEmpty(message = "register.error.confirmPassword")
	@Size(min = 6, message = "register.error.confirmPassword")
	public String getRegisterPasswordConfirm() {
		return registerPasswordConfirm;
	}
	
	public void setRegisterPasswordConfirm(String registerPasswordConfirm) {
		this.registerPasswordConfirm = registerPasswordConfirm;
	}
	
	@NotEmpty(message = "register.error.email")
	@Pattern(regexp = "^[-+.\\w]{1,64}@[-.\\w]{1,64}\\.[-.\\w]{2,6}$", message = "register.error.email")
	public String getRegisterEmail() {
		return registerEmail;
	}
	
	public void setRegisterEmail(String registerEmail) {
		this.registerEmail = registerEmail;
	}
	
	public String getRegisterPrefix() {
		return registerPrefix;
	}
	
	public void setRegisterPrefix(String registerPrefix) {
		this.registerPrefix = registerPrefix;
	}
	
	public String getRegisterFirstName() {
		return registerFirstName;
	}
	
	public void setRegisterFirstName(String registerFirstName) {
		this.registerFirstName = registerFirstName;
	}
	
	public String getRegisterMiddleName() {
		return registerMiddleName;
	}
	
	public void setRegisterMiddleName(String registerMiddleName) {
		this.registerMiddleName = registerMiddleName;
	}
	
	public String getRegisterLastName() {
		return registerLastName;
	}
	
	public void setRegisterLastName(String registerLastName) {
		this.registerLastName = registerLastName;
	}
	
	public String getRegisterSuffix() {
		return registerSuffix;
	}
	
	public void setRegisterSuffix(String registerSuffix) {
		this.registerSuffix = registerSuffix;
	}

}
