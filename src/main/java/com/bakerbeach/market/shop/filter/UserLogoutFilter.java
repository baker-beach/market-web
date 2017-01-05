package com.bakerbeach.market.shop.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.util.WebUtils;

import com.bakerbeach.market.cms.service.UrlHelper;

public class UserLogoutFilter extends LogoutFilter {
	
	@Override
    protected void issueRedirect(ServletRequest request, ServletResponse response, String redirectUrl) throws Exception {
		if(request instanceof HttpServletRequest){
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			
			String path = UrlHelper.getPathWithinApplication(httpRequest);
			
			if (!path.equals("/")) {
				path = path.split("/")[1];
				if(!path.equals("logout"))
					redirectUrl = "/" + path + redirectUrl;
			}
			
			
		}
		
        WebUtils.issueRedirect(request, response, redirectUrl);
    }

}
