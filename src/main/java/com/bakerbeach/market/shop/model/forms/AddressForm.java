package com.bakerbeach.market.shop.model.forms;

import java.io.Serializable;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.bakerbeach.market.core.api.model.Address;

public class AddressForm implements Serializable{

	@Valid
	public AddressFormAddress shippingAddress;

	@Valid
	public AddressFormAddress billingAddress;

	boolean useShipping = false;
	
	public AddressForm() {
		setBillingAddress(new AddressFormAddress());
		setShippingAddress(new AddressFormAddress());
	}

	public AddressFormAddress getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(AddressFormAddress shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public AddressFormAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(AddressFormAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	public boolean isUseShipping() {
		return useShipping;
	}
	
	public void setUseShipping(boolean useShipping) {
		this.useShipping = useShipping;
	}
	
	public static class AddressFormAddress{
		public String id;
		public String prefix;
		public String firstName;
		public String lastName;
		public String middleName;
		public String street;
		public String street2 = "";
		public String city;
		public String region;
		public String postcode;
		public String countryCode;
		public String company;
		public String telephone;

		public AddressFormAddress() {
		}

		public AddressFormAddress(Address address) {
			setId(address.getId());
			setFirstName(address.getFirstName());
			setMiddleName(address.getMiddleName());
			setLastName(address.getLastName());
			setPrefix(address.getPrefix());
			setCity(address.getCity());
			setPostcode(address.getPostcode());
			setStreet(address.getStreet1());
			setStreet2(address.getStreet2());
			setCountryCode(address.getCountryCode());
			setCompany(address.getCompany());
			setTelephone(address.getTelephone());
			setRegion(address.getRegion());
		}

		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
//		@NotEmpty(message = "address.error.telephone")
		public String getTelephone() {
			return telephone;
		}

		public void setTelephone(String telephone) {
			this.telephone = telephone;
		}

		// @NotEmpty(message = "address.error.prefix")
		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		@NotEmpty(message = "address.error.firstname")
		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		@NotEmpty(message = "address.error.lastname")
		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getMiddleName() {
			return middleName;
		}

		public void setMiddleName(String middleName) {
			this.middleName = middleName;
			if (this.middleName == null)
				this.middleName = "";
		}

		@NotEmpty(message = "address.error.street")
		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getStreet2() {
			return street2;
		}

		public void setStreet2(String street2) {
			this.street2 = street2;
		}

		@NotEmpty(message = "address.error.postcode")
		public String getPostcode() {
			return postcode;
		}

		public void setPostcode(String postcode) {
			this.postcode = postcode;
		}

		@NotEmpty(message = "address.error.city")
		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}
		
		public String getRegion() {
			return region;
		}
		
		public void setRegion(String region) {
			this.region = region;
		}

		// @NotEmpty(message = "address.error.country")
		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public String getCompany() {
			return company;
		}

		public void setCompany(String company) {
			this.company = company;
		}
	}

}
