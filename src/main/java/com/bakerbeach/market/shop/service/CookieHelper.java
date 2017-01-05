package com.bakerbeach.market.shop.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieHelper {

    public static final int DEFAULT_MAX_AGE = -1;
    public static final int DEFAULT_VERSION = -1;
    public static final String ROOT_PATH = "/";

    public static Cookie createCookie(String name, String value) {
    	Cookie cookie = new Cookie(name, value);
    	cookie.setMaxAge(DEFAULT_MAX_AGE);
    	cookie.setVersion(DEFAULT_VERSION);
    	cookie.setPath(ROOT_PATH);
    	cookie.setSecure(false);
    	
    	return cookie;
    }
	
	
	public static Cookie getCookie(HttpServletRequest request, String cookieName) {
	    Cookie cookies[] = request.getCookies();
	    if (cookies != null) {
	        for (javax.servlet.http.Cookie cookie : cookies) {
	            if (cookie.getName().equals(cookieName)) {
	                return cookie;
	            }
	        }
	    }
	    return null;
	}
	
}
