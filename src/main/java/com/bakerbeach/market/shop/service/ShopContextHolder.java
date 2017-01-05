package com.bakerbeach.market.shop.service;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.bakerbeach.market.cms.service.CmsContextHolder;
import com.bakerbeach.market.core.api.model.ShopContext;

public class ShopContextHolder {

	public static ShopContext getInstance() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		ShopContext shopContext = (ShopContext) requestAttributes.getAttribute(CmsContextHolder.CMS_CONTEXT_REQUEST_ATTRIBUTES_KEY, RequestAttributes.SCOPE_SESSION);
		return shopContext;
	}

	public static void setInstance(ShopContext shopContext) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		requestAttributes.setAttribute(CmsContextHolder.CMS_CONTEXT_REQUEST_ATTRIBUTES_KEY, shopContext, RequestAttributes.SCOPE_SESSION);
	}

}
