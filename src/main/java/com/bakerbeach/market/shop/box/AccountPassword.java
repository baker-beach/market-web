package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.commons.FieldMessageImpl;
import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.commons.Messages;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.customer.api.service.CustomerService;
import com.bakerbeach.market.customer.api.service.CustomerServiceException;
import com.bakerbeach.market.shop.model.forms.PasswordForm;
import com.bakerbeach.market.shop.service.CustomerHelper;

@Component("com.bakerbeach.market.shop.box.AccountPassword")
@Scope("prototype")
public class AccountPassword extends AbstractBox implements ProcessableBox {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private CustomerService customerService;

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {

		Messages messages = (Messages) modelMap.get("messages");
		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("messages", messages);
		
		Customer customer = CustomerHelper.getCustomer();

		if (request.getMethod().equals(RequestMethod.POST.toString())) {
			PasswordForm passwordForm = new PasswordForm();
			BindingResult result = bind(passwordForm, request);

			if (!result.hasErrors()) {
				if (passwordForm.getNewPassword().equals(passwordForm.getCheckNewPassword())) {
					try {
						customerService.changePassword(customer, passwordForm.getNewPassword());
						messages.addGlobalMessage(new MessageImpl("password.success"));
					} catch (CustomerServiceException e) {
						messages.addGlobalError(new MessageImpl("password.error.save"));
					}
				} else {
					messages.addFieldError(new FieldMessageImpl("checkNewPassword",Message.TYPE_ERROR,"password.error.checkNewPassword"));
				}
			} else {
				getFieldErrors(result, messages);
			}
			throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
		}

	}

}
