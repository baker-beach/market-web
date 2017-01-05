package com.bakerbeach.market.shop.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.bakerbeach.market.cms.service.UrlHelper;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextFactory;
import com.bakerbeach.market.shop.service.ShopContextHolder;

public class ShopContextFilter extends AbstractContextFilter {
	protected static final Logger LOG = Logger.getLogger(ShopContextFilter.class.getName());

	private ShopContextFactory shopContextFactory;

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		final HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		if (skipFilter(httpServletRequest)) {
			chain.doFilter(httpServletRequest, httpServletResponse);
		} else {
			try {
				String host = UrlHelper.getHost(httpServletRequest);
				String path = UrlHelper.getPathWithinApplication(httpServletRequest);

				ShopContext shopContext = ShopContextHolder.getInstance();
				if (shopContext == null) {
					shopContext = shopContextFactory.newInstance(host, path);
					shopContext.setProtocol(UrlHelper.getProtocol(httpServletRequest));
					ShopContextHolder.setInstance(shopContext);
				} else {
					shopContext.setHost(host);
					shopContext.setPath(path);
					shopContext.setProtocol(UrlHelper.getProtocol(httpServletRequest));
					shopContext.setFilterList(null);
					shopContext.setPageId(null);
				}
			
				chain.doFilter(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
 				LOG.error(ExceptionUtils.getStackTrace(e));
				throw new ServletException(e);
			}
		}
	}

	public void setShopContextFactory(ShopContextFactory shopContextFactory) {
		this.shopContextFactory = shopContextFactory;
	}

}
