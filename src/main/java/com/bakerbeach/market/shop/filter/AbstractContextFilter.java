package com.bakerbeach.market.shop.filter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

public abstract class AbstractContextFilter extends GenericFilterBean {
	
	private String[] excludePaths;
	private AntPathMatcher matcher = new AntPathMatcher();
	
	public boolean skipFilter(HttpServletRequest httpServletRequest){
		boolean skipFilter = false;
		if (excludePaths != null) {
			for (String pattern : excludePaths) {
				if (matcher.match(pattern, httpServletRequest.getServletPath())) {
					skipFilter = true;
					break;
				}
			}
		}
		return skipFilter;
		
	}
	
	public void setExcludePaths(String[] excludePaths) {
		this.excludePaths = excludePaths;
	}

}
