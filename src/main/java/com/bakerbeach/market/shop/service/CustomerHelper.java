package com.bakerbeach.market.shop.service;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.customer.model.AnonymousCustomer;

public class CustomerHelper {

	public static Customer getCustomer() {
		Subject subject = SecurityUtils.getSubject();
		Object principal = subject.getPrincipal();
		if (principal != null && principal instanceof Customer) {
			return (Customer) principal;			
		} else {
			return new AnonymousCustomer();
		}		
	}
	
	public static Boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();	
	}	
	
	public static Boolean isCustomer() {
		Subject subject = SecurityUtils.getSubject();
		Object principal = subject.getPrincipal();
		if(principal == null || principal instanceof AnonymousCustomer)
			return false;	
		else
			return true;
	}	
	
}
