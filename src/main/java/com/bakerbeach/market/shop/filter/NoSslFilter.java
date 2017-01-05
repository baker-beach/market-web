package com.bakerbeach.market.shop.filter;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.util.WebUtils;

public class NoSslFilter extends AuthorizationFilter {

	public static final int DEFAULT_HTTP_PORT = 80;
	public static final String HTTP_SCHEME = "http";

	private int port = DEFAULT_HTTP_PORT;

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append(HTTP_SCHEME).append("://");
		sb.append(request.getServerName());
		if (port != DEFAULT_HTTP_PORT && port != SslFilter.DEFAULT_HTTPS_PORT) {
			sb.append(":");
			sb.append(port);
		}
		if (request instanceof HttpServletRequest) {
			if (request.getAttribute("x-path") != null) {
				String contextPath = WebUtils.toHttp(request).getContextPath();
				if (contextPath != null) {
					sb.append(contextPath);
				}
				sb.append(request.getAttribute("x-path").toString());
			} else {
				sb.append(WebUtils.toHttp(request).getRequestURI());
			}
			String query = WebUtils.toHttp(request).getQueryString();
			if (query != null) {
				sb.append("?").append(query);
			}
		}

		WebUtils.toHttp(response).setStatus(301);
		WebUtils.toHttp(response).setHeader("Location", sb.toString());
		WebUtils.toHttp(response).setHeader("Connection", "close");

		return false;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		return !request.isSecure();
	}

	public void setPort(int port) {
		this.port = port;
	}

}
