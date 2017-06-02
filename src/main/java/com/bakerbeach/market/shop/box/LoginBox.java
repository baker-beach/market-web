package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.commons.Messages;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.model.forms.Login;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.shop.service.ShopHelper;

@Component("com.bakerbeach.market.shop.box.LoginBox")
@Scope("prototype")
public class LoginBox extends AbstractLoginBox {

	private static final long serialVersionUID = 1L;

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws RedirectException {
		Messages messages = (Messages) model.get("messages");
		ShopHelper helper = (ShopHelper) model.get("helper");

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		
		if (request.getMethod().equals(RequestMethod.POST.toString())) {
			Login login = new Login();
			BindingResult result = bind(login, request);

			flashMap.put("login", login);
			flashMap.put("messages", messages);
			flashMap.put("loginTarget", "loginForm");
			
			if (!result.hasErrors()) {
				try {
					doLogin(login.getLoginEmail(), login.getLoginPassword(),true);
					Redirect redirect = onSuccessfulAuthentication(request, helper);

					if (StringUtils.isNotBlank(login.getTargetUrlId())) {
						redirect = new Redirect(login.getTargetUrlId(), null, Redirect.URL_ID);						
					}

					messages.addGlobalError(new MessageImpl(Message.TYPE_INFO, "login.success"));
					
					throw new RedirectException(redirect);
				} catch (AuthenticationException e) {
					messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "login.error"));
				}
			} else {
				getFieldErrors(result, messages);
			}

			throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
		} else {
			if (!model.containsKey("login")) {
				Login login = new Login();
				model.put("login", login);
			}
		}
	}
	
	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		super.handleRenderRequest(request, response, modelMap);

		ShopContext shopContext = ShopContextHolder.getInstance();
		if (shopContext.getRequestData().containsKey("target_url_id")) {
			String successTargetUrlId = (String) shopContext.getRequestData().get("target_url_id");
			modelMap.addAttribute("targetUrlId", successTargetUrlId);
		}

	}

}