package com.bakerbeach.market.shop.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.subject.WebSubjectContext;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.bakerbeach.market.cms.model.CmsContext;
import com.bakerbeach.market.cms.service.CmsContextHolder;
import com.bakerbeach.market.commons.ServiceException;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.customer.api.service.CustomerService;

public class ShopRememberMeManager extends CookieRememberMeManager {
	private static final Logger log = LoggerFactory.getLogger(ShopRememberMeManager.class);

	private static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "nuggikette";
	private static final String DELIMITER = ":";
	private static int tokenValiditySeconds = Cookie.ONE_YEAR;
	private static String key = "chang3it";
	
	private static CustomerService customerService;

	private Cookie cookieTmpl;

	public ShopRememberMeManager() {
		Cookie cookie = new SimpleCookie(DEFAULT_REMEMBER_ME_COOKIE_NAME);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(tokenValiditySeconds);

		this.cookieTmpl = cookie;
	}

	@Override
	public PrincipalCollection getRememberedPrincipals(SubjectContext subjectContext) {
		
		try {
			Customer customer = getRememberedIdentity(subjectContext);
			if (customer != null) {
				PrincipalCollection principals = new SimplePrincipalCollection(customer, "windeln");
				return principals;
			} else {						
				PrincipalCollection principals = new SimplePrincipalCollection(customerService.createAnonymousCustomer(), "windeln");			
				return principals;
			}
		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	@Override
	protected PrincipalCollection onRememberedPrincipalFailure(RuntimeException e, SubjectContext context) {
		// TODO Auto-generated method stub
		return super.onRememberedPrincipalFailure(e, context);
	}
	
	@Override
	public void onSuccessfulLogin(Subject subject, AuthenticationToken token, AuthenticationInfo info) {
		forgetIdentity(subject);
		
		if (token instanceof UsernamePasswordToken) {
			UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
			String username = usernamePasswordToken.getUsername();
			String password = new String(usernamePasswordToken.getPassword());
			
			long expiryTime = System.currentTimeMillis() + 1000L * tokenValiditySeconds;
			String signatureValue = makeTokenSignature(expiryTime, username, password);
			String[] cookieTokens = new String[] { username, Long.toString(expiryTime), signatureValue, "false" };

	        HttpServletRequest request = WebUtils.getHttpRequest(subject);
	        HttpServletResponse response = WebUtils.getHttpResponse(subject);

	        Cookie cookie = new SimpleCookie(cookieTmpl);
	        cookie.setValue(encodeCookie(cookieTokens));
	        cookie.saveTo(request, response);
		}
	}
	
	@Override
	public void onFailedLogin(Subject subject, AuthenticationToken token, AuthenticationException ae) {
        HttpServletRequest request = WebUtils.getHttpRequest(subject);
        HttpServletResponse response = WebUtils.getHttpResponse(subject);

        Cookie cookie = new SimpleCookie(cookieTmpl);
        cookie.removeFrom(request, response);
	}
	
	private Customer getRememberedIdentity(SubjectContext subjectContext) throws Exception {
		if (!WebUtils.isHttp(subjectContext)) {
			if (log.isDebugEnabled()) {
				String msg = "SubjectContext argument is not an HTTP-aware instance.  This is required to obtain a "
						+ "servlet request and response in order to retrieve the rememberMe cookie. Returning "
						+ "immediately and ignoring rememberMe operation.";
				log.debug(msg);
			}
			return null;
		}

		WebSubjectContext wsc = (WebSubjectContext) subjectContext;
		if (isIdentityRemoved(wsc)) {
			return null;
		}

		HttpServletRequest request = WebUtils.getHttpRequest(wsc);
		HttpServletResponse response = WebUtils.getHttpResponse(wsc);

		String cookieValue = cookieTmpl.readValue(request, response);
		if (cookieValue != null) {
			String[] cookieTokens = decodeCookie(cookieValue);
			Customer customer = processLogin(cookieTokens);
			return customer;
		}
		
		return null;
	}

	private Customer processLogin(String[] cookieTokens) throws Exception {
		try {
			if (cookieTokens.length != 4) {
				throw new Exception("Cookie token did not contain 4" + " tokens, but contained '"
						+ Arrays.asList(cookieTokens) + "'");
			}

			long tokenExpiryTime;

			try {
				tokenExpiryTime = new Long(cookieTokens[1]).longValue();
			} catch (NumberFormatException nfe) {
				throw new Exception("Cookie token[1] did not contain a valid number (contained '" + cookieTokens[1]
						+ "')");
			}

			if (isTokenExpired(tokenExpiryTime)) {
				throw new Exception("Cookie token[1] has expired (expired on '" + new Date(tokenExpiryTime)
						+ "'; current time is '" + new Date() + "')");
			}

			Customer customer;
			CmsContext cmsContext = CmsContextHolder.getInstance();
			customer = customerService.findByEmail(cookieTokens[0],cmsContext.getAppCode());
			// TODO: check against encoded password
			return customer;
		} catch (ServiceException e) {
			log.error(ExceptionUtils.getStackTrace(e));
			throw new Exception(e);
		}
	}

	private String makeTokenSignature(long tokenExpiryTime, String username, String password) {
		String data = username + ":" + tokenExpiryTime + ":" + password + ":" + key;
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available!");
		}

		return new String(Hex.encodeHex(digest.digest(data.getBytes())));
	}

    private String encodeCookie(String[] cookieTokens) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < cookieTokens.length; i++) {
            sb.append(cookieTokens[i]);

            if (i < cookieTokens.length - 1) {
                sb.append(DELIMITER);
            }
        }

        String value = sb.toString();
        sb = new StringBuilder(new String(Base64.encodeBase64(value.getBytes())));

        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    private String[] decodeCookie(String cookieValue) throws Exception {
        for (int j = 0; j < cookieValue.length() % 4; j++) {
            cookieValue = cookieValue + "=";
        }

        if (!Base64.isBase64(cookieValue.getBytes())) {
            throw new Exception( "Cookie token was not Base64 encoded; value was '" + cookieValue + "'");
        }

        String cookieAsPlainText = new String(Base64.decodeBase64(cookieValue.getBytes()));

        String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, DELIMITER);

        if ((tokens[0].equalsIgnoreCase("http") || tokens[0].equalsIgnoreCase("https")) && tokens[1].startsWith("//")) {
            // Assume we've accidentally split a URL (OpenID identifier)
            String[] newTokens = new String[tokens.length - 1];
            newTokens[0] = tokens[0] + ":" + tokens[1];
            System.arraycopy(tokens, 2, newTokens, 1, newTokens.length - 1);
            tokens = newTokens;
        }

        return tokens;
    }

	private boolean isTokenExpired(long tokenExpiryTime) {
		return tokenExpiryTime < System.currentTimeMillis();
	}

	private boolean isIdentityRemoved(WebSubjectContext subjectContext) {
		ServletRequest request = subjectContext.resolveServletRequest();
		if (request != null) {
			Boolean removed = (Boolean) request.getAttribute(ShiroHttpServletRequest.IDENTITY_REMOVED_KEY);
			return removed != null && removed;
		}
		return false;
	}

	@Override
	protected void forgetIdentity(Subject subject) {
	}

	/**
	 * @return the customerService
	 */
	public static CustomerService getCustomerService() {
		return customerService;
	}

	/**
	 * @param customerService the customerService to set
	 */
	public static void setCustomerService(CustomerService customerService) {
		ShopRememberMeManager.customerService = customerService;
	}
	
}