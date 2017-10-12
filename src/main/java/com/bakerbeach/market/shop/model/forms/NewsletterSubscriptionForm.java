package com.bakerbeach.market.shop.model.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NewsletterSubscriptionForm implements Serializable{
	
	List<FormEntry> newsletter = new ArrayList<>();
	
	public List<FormEntry> getNewsletter() {
		return newsletter;
	}
	
	public void setNewsletter(List<FormEntry> newsletter) {
		this.newsletter = newsletter;
	}
	
	public static class FormEntry {
		private String name;
		private Boolean isChecked = false;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Boolean isChecked() {
			return isChecked;
		}
		
		public void setIsChecked(Boolean isChecked) {
			this.isChecked = isChecked;
		}
		
	}
}
