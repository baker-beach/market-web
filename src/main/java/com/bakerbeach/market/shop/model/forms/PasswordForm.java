package com.bakerbeach.market.shop.model.forms;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;

public class PasswordForm implements Serializable{

	private String oldPassword;
	private String newPassword;
	private String checkNewPassword;
	
	@Length(min = 5, message = "password.error.oldPassword")
	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	
	@Length(min = 5, message = "password.error.newPassword")
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	@Length(min = 5, message = "password.error.checkNewPassword")
	public String getCheckNewPassword() {
		return checkNewPassword;
	}

	public void setCheckNewPassword(String checkNewPassword) {
		this.checkNewPassword = checkNewPassword;
	}

}
