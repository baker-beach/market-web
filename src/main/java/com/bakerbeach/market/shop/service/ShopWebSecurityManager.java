package com.bakerbeach.market.shop.service;

import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

public class ShopWebSecurityManager extends DefaultWebSecurityManager {
	
	@Override
	protected SubjectContext resolvePrincipals(SubjectContext context) {
		return super.resolvePrincipals(context);
	}
}
