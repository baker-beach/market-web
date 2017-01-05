package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.commons.FieldMessageImpl;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.Message;
import com.bakerbeach.market.core.api.model.Messages;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.customer.api.service.CustomerService;
import com.bakerbeach.market.customer.api.service.CustomerServiceException;
import com.bakerbeach.market.shop.model.forms.RegisterForm;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.shop.service.ShopHelper;

@SuppressWarnings("serial")
@Component("com.bakerbeach.market.shop.box.RegistrationBox")
@Scope("prototype")
public class RegistrationBox extends AbstractLoginBox {
	
	@Autowired
	private CustomerService customerService;


	public void handlePostActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {

		Messages messages = (Messages) modelMap.get("messages");

		ShopHelper helper = (ShopHelper) modelMap.get("helper");

		ShopContext shopContext = ShopContextHolder.getInstance();

		RegisterForm registerForm = null;
		
		try {
			registerForm = (RegisterForm) getClass().getClassLoader()
					.loadClass("com.bakerbeach.market.shop.model.forms.SimpleNameRegistrationForm").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

		}

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("messages", messages);
		flashMap.put("registrationForm", registerForm);
		
		if (registerForm != null) {
			BindingResult result = bind(registerForm, request);
			if (!result.hasErrors()) {
				
				if (!registerForm.getRegisterPassword().equals(registerForm.getRegisterPasswordConfirm())) {
					messages.addFieldError(new FieldMessageImpl("registerPasswordConfirm", Message.TYPE_ERROR, "register.error.confirmPassword"));					
					throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
				}
				
				try {
					Customer customer = customerService.register(
							registerForm.getRegisterEmail(), registerForm.getRegisterPassword(),
							shopContext.getShopCode());

					if (!StringUtils.isEmpty(registerForm.getRegisterFirstName()))
						customer.setFirstName(registerForm.getRegisterFirstName());

					if (!StringUtils.isEmpty(registerForm.getRegisterMiddleName()))
						customer.setMiddleName(registerForm.getRegisterMiddleName());

					if (!StringUtils.isEmpty(registerForm.getRegisterLastName()))
						customer.setLastName(registerForm.getRegisterLastName());

					customerService.update(customer);

				} catch (CustomerServiceException e) {
					messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "register.error"));
					throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
				}
				try {
					doLogin(registerForm.getRegisterEmail(), registerForm.getRegisterPassword(),true);
				} catch (AuthenticationException ae) {
					messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "login.error"));
					throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
				}
				messages.addGlobalInfo(new MessageImpl(Message.TYPE_INFO, "registration.success"));
				throw new RedirectException(onSuccessfulAuthentication(request, helper));
			} else {
				getFieldErrors(result, messages);
				throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
			}
		}

	}

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {
		if(request.getMethod().equals(RequestMethod.POST.toString()))
			handlePostActionRequest(request,response,modelMap);
		
	}
	
	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		// TODO Auto-generated method stub
		super.handleRenderRequest(request, response, modelMap);
	}

}
