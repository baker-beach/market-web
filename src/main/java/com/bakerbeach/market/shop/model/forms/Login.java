package com.bakerbeach.market.shop.model.forms;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

public class Login {
	private String loginEmail;
	private String loginPassword;
	private Boolean subscription = false;
	private Boolean identified = false;
	private String targetUrlId;

	public Boolean getSubscription() {
		return subscription;
	}

	public void setSubscription(Boolean subscription) {
		this.subscription = subscription;
	}

	public Boolean isIdentified() {
		return identified;
	}

	public void setIdentified(Boolean identified) {
		this.identified = identified;
	}

    @NotEmpty(message = "login.error.email")
	public String getLoginEmail() {
		return loginEmail;
	}

	public void setLoginEmail(String loginEmail) {
		this.loginEmail = loginEmail;
	}

	@Length(min = 5, message = "login.error.password.minLength")
	public String getLoginPassword() {
		return loginPassword;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	
	public String getTargetUrlId() {
		return targetUrlId;
	}
	
	public void setTargetUrlId(String targetUrlId) {
		this.targetUrlId = targetUrlId;
	}

}