package com.bakerbeach.market.shop.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

public class SessionFilter extends GenericFilterBean{
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		
		if(httpServletRequest.getSession().getAttribute("isSesssionNew") == null){
			httpServletRequest.getSession().setAttribute("isSesssionNew",Boolean.TRUE);
			httpServletRequest.getSession().setAttribute("cookie_switch",Boolean.TRUE);
		}
		else
			httpServletRequest.getSession().setAttribute("isSesssionNew",Boolean.FALSE);
		
		chain.doFilter(httpServletRequest, httpServletResponse);
		
	}

}
