package com.bakerbeach.market.shop.service;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.bakerbeach.market.cms.model.CmsContext;
import com.bakerbeach.market.cms.service.CmsContextHolder;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.customer.api.service.CustomerService;

public class CustomerServiceRealm extends AuthorizingRealm {

	private CustomerService customerService;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		authorizationInfo.addRole("ROLE_CUSTOMER");

		return authorizationInfo;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		try {
			CmsContext cmsContext = CmsContextHolder.getInstance();
			if (token instanceof UsernamePasswordToken) {
				UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;

				Customer customer = customerService.findByEmail(usernamePasswordToken.getUsername(),cmsContext.getAppCode());
				if (customer != null) {
					String password = new String(usernamePasswordToken.getPassword());
					Boolean test = customerService.checkPassword(customer, password);
					if (test) {
						SimpleAccount account = new SimpleAccount(customer, password, getName());
						return account;
					} else {
						throw new IncorrectCredentialsException();
					}
				} else {
					throw new UnknownAccountException();
				}
			} else {
				throw new UnsupportedTokenException();
			}
		} catch (Exception e) {
			throw new AuthenticationException();
		}
	}

	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}

}