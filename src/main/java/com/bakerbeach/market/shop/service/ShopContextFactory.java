package com.bakerbeach.market.shop.service;

import java.util.Map;

import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.model.ShopContextImpl;

public class ShopContextFactory {
	private Map<String, ShopContext> contextDefinitions;

	public ShopContext newInstance(String host, String path) throws ShopContextException {
		try {
			ShopContext contextDefinition = contextDefinitions.get(host);
			ShopContext cmsContext = new ShopContextImpl(contextDefinition);
			cmsContext.setPath(path);
			cmsContext.setHost(host);

			return cmsContext;
		} catch (Exception e) {
			throw new ShopContextException(e);
		}
	}

	public void setContextDefinitions(Map<String, ShopContext> contextDefinitions) {
		this.contextDefinitions = contextDefinitions;
	}

}
