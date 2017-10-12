package com.bakerbeach.market.shop.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bakerbeach.market.cms.service.UrlHelper;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.model.ShopContextImpl;

public class ShopContextFactory {
	private Map<String, ShopContext> contextDefinitions;

	public ShopContext newInstance(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ShopContextException {
		try {
			
			String host = UrlHelper.getHost(httpServletRequest);
			
			ShopContext contextDefinition = contextDefinitions.get(host);
			ShopContextImpl cmsContext = new ShopContextImpl(contextDefinition);
			
			cmsContext.setHost(host);
			cmsContext.setPath(UrlHelper.getPathWithinApplication(httpServletRequest));
			cmsContext.setProtocol(UrlHelper.getProtocol(httpServletRequest));
			cmsContext.setHttpServletRequest(httpServletRequest);
			cmsContext.setHttpServletResponse(httpServletResponse);
			
			return cmsContext;
			
		} catch (Exception e) {
			throw new ShopContextException(e);
		}
	}

	public void setContextDefinitions(Map<String, ShopContext> contextDefinitions) {
		this.contextDefinitions = contextDefinitions;
	}

}
