package com.bakerbeach.market.shop.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bakerbeach.market.cms.service.UrlRewriteResponse;
import com.bakerbeach.market.cms.service.UrlRewriter;

public class UrlRewriteFilter extends AbstractContextFilter {
	protected final Log logger = LogFactory.getLog(getClass());

	private UrlRewriter inboundUrlRewriter;
	private UrlRewriter outboundUrlRewriter;

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		final HttpServletResponse httpServletResponse = (HttpServletResponse) response;


		if (skipFilter(httpServletRequest)) {
			chain.doFilter(httpServletRequest, response);
		} else {
			UrlRewriteResponse urlRewriteResponse = new UrlRewriteResponse(httpServletResponse, httpServletRequest, outboundUrlRewriter);
			
			if (request.getAttribute("x-path") == null) {
				boolean requestRewritten = inboundUrlRewriter.processRequest(httpServletRequest, urlRewriteResponse, chain);
				if (!requestRewritten) {
					chain.doFilter(httpServletRequest, urlRewriteResponse);
				}
			} else {
				chain.doFilter(httpServletRequest, urlRewriteResponse);
			}
					
		}
	}

	public UrlRewriter getInboundUrlRewriter() {
		return inboundUrlRewriter;
	}

	public void setInboundUrlRewriter(UrlRewriter inboundUrlRewriter) {
		this.inboundUrlRewriter = inboundUrlRewriter;
	}

	public UrlRewriter getOutboundUrlRewriter() {
		return outboundUrlRewriter;
	}

	public void setOutboundUrlRewriter(UrlRewriter outboundUrlRewriter) {
		this.outboundUrlRewriter = outboundUrlRewriter;
	}

}