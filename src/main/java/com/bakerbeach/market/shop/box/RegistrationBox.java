package com.bakerbeach.market.shop.box;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.commons.Messages;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.customer.api.service.CustomerService;
import com.bakerbeach.market.customer.api.service.CustomerServiceException;
import com.bakerbeach.market.newsletter.api.model.NewsletterSubscription;
import com.bakerbeach.market.newsletter.api.service.NewsletterServiceException;
import com.bakerbeach.market.newsletter.api.service.NewsletterSubscriptionService;
import com.bakerbeach.market.shop.model.forms.NewsletterSubscriptionForm;
import com.bakerbeach.market.shop.model.forms.RegisterForm;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.shop.service.ShopHelper;

@SuppressWarnings("serial")
@Component("com.bakerbeach.market.shop.box.RegistrationBox")
@Scope("prototype")
public class RegistrationBox extends AbstractLoginBox {

	@Autowired
	private CustomerService customerService;

	@Autowired(required = false)
	private NewsletterSubscriptionService newsletterSubscriptionService;

	@Autowired(required = false)
	private NewsletterSubscriptionForm newsletterSubscriptionForm;

	@Autowired(required = false)
	@Qualifier("registrationForm")
	private RegisterForm registrationForm;

	public void handlePostActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {

		Messages messages = (Messages) modelMap.get("messages");

		ShopHelper helper = (ShopHelper) modelMap.get("helper");

		ShopContext shopContext = ShopContextHolder.getInstance();

		RegisterForm registerForm = null;
		try {
			registerForm = registrationForm.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("messages", messages);
		flashMap.put("registrationForm", registerForm);

		if (registerForm != null) {
			BindingResult result = bind(registerForm, request);

			if (!result.hasErrors()) {
				Customer customer = null;

				if (!registerForm.getRegisterPassword().equals(registerForm.getRegisterPasswordConfirm())) {
					messages.addFieldError(new FieldMessageImpl("registerPasswordConfirm", Message.TYPE_ERROR,
							"register.error.confirmPassword"));
					throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
				}

				try {
					List<String> shopCodesOnRegistration = new ArrayList<>();
					shopCodesOnRegistration.add(shopContext.getShopCode());
					if (CollectionUtils.isNotEmpty(shopContext.getGroupCodes())) {
						shopCodesOnRegistration.addAll(shopContext.getGroupCodes());
					}

					String firstName = "";
					if (!StringUtils.isEmpty(registerForm.getRegisterFirstName()))
						firstName = registerForm.getRegisterFirstName();

					String lastName = "";
					if (!StringUtils.isEmpty(registerForm.getRegisterLastName()))
						lastName = registerForm.getRegisterLastName();

					customer = customerService.register(registerForm.getRegisterEmail(),
							registerForm.getRegisterPassword(), shopCodesOnRegistration, firstName, lastName);

					customerService.update(customer);

				} catch (CustomerServiceException e) {
					messages.addGlobalError(new MessageImpl("registration", Message.TYPE_ERROR, "register.error",
							Arrays.asList(Message.TAG_BOX), Arrays.asList()));
					throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
				}
				try {
					doLogin(registerForm.getRegisterEmail(), registerForm.getRegisterPassword(), true);
				} catch (AuthenticationException ae) {
					messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "login.error"));
					throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
				}
				messages.addGlobalInfo(new MessageImpl("registration", Message.TYPE_INFO, "registration.success",
						Arrays.asList(Message.TAG_BOX), Arrays.asList()));

				newsletterSubscription(request, flashMap, customer);

				throw new RedirectException(onSuccessfulAuthentication(request, helper));
			} else {
				getFieldErrors(result, messages);
				throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
			}
		}

	}

	protected void newsletterSubscription(HttpServletRequest request, FlashMap flashMap, Customer customer) {
		if (newsletterSubscriptionService != null && newsletterSubscriptionForm != null) {
			try {
				NewsletterSubscriptionForm nsf = newsletterSubscriptionForm.getClass().newInstance();
				flashMap.put("newsletterSubscriptionForm", nsf);
				BindingResult r = bind(nsf, request);
				if (!r.hasErrors()) {
					String prefix = customer.getPrefix();
					String firstName = customer.getFirstName();
					String lastName = customer.getLastName();
					String email = customer.getEmail();
					for (NewsletterSubscriptionForm.FormEntry entry : nsf.getNewsletter()) {
						try {
							String newsletterCode = entry.getName();
							Boolean isChecked = entry.isChecked();
							if (isChecked) {
								newsletterSubscriptionService.subscribe(prefix, firstName, lastName, email,
										newsletterCode, NewsletterSubscription.STATUS_REQUESTED);
							}
						} catch (NewsletterServiceException e) {
							log.error(ExceptionUtils.getStackTrace(e));
						}

					}
					;
				}
			} catch (InstantiationException | IllegalAccessException e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {
		if (request.getMethod().equals(RequestMethod.POST.toString()))
			handlePostActionRequest(request, response, modelMap);

	}

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		// TODO Auto-generated method stub
		super.handleRenderRequest(request, response, modelMap);
	}

}
