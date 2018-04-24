package com.bakerbeach.market.shop.box;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.bakerbeach.market.customer.api.model.ResetPasswordToken;
import com.bakerbeach.market.customer.api.service.CustomerService;
import com.bakerbeach.market.customer.api.service.CustomerServiceException;

@Component("com.bakerbeach.market.shop.box.ResetPasswordBox")
@Scope("prototype")
public class ResetPasswordBox extends AbstractBox implements ProcessableBox {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ResetPasswordBox.class.getName());

	@Autowired
	private CustomerService customerService;

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws ProcessableBoxException {

		if (request.getMethod().equals(RequestMethod.POST.toString())) {
			Messages messages = (Messages) model.get("messages");
			FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
			flashMap.put("messages", messages);

			ResetPasswordForm form = new ResetPasswordForm();
			BindingResult result = bind(form, request);
			if (!result.hasErrors()) {
				try {
					if (form.getNewPassword().equals(form.getCheckNewPassword())) {
						ResetPasswordToken token = customerService.getResetPasswordToken(form.getTokenId());
						Customer customer = customerService.findById(token.getCustomerId());

						if (token.getExpiresAt().after(new Date())) {
							customerService.changePassword(customer, form.getNewPassword());
							messages.addGlobalMessage(new MessageImpl(Message.TYPE_INFO, "password.success"));

							customerService.deleteResetPasswordToken(token.getId());

							throw new RedirectException(new Redirect("login", null));
						} else {
							messages.addFieldError(new FieldMessageImpl("checkNewPassword", Message.TYPE_ERROR,
									"password.error.checkNewPassword"));
						}
					} else {
						messages.addFieldError(new FieldMessageImpl("checkNewPassword", Message.TYPE_ERROR,
								"password.error.checkNewPassword"));
					}
				} catch (RedirectException e) {
					throw e;
				} catch (CustomerServiceException e) {
					messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "password.error.save"));
				} catch (Exception e) {
					messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "password.error.save"));
				}
			} else {
				getFieldErrors(result, messages);
			}

			flashMap.put("form", form);
			throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
		}

	}

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		ResetPasswordForm form = (ResetPasswordForm) model.get("form");
		if (form == null) {
			form = new ResetPasswordForm();
			form.setTokenId(request.getParameter("t"));
			model.put("form", form);
		}
	}

	public static class ResetPasswordForm {
		private String tokenId;
		private String newPassword;
		private String checkNewPassword;

		public String getTokenId() {
			return tokenId;
		}

		public void setTokenId(String tokenId) {
			this.tokenId = tokenId;
		}

		@Length(min = 5, message = "password.error.newPassword")
		public String getNewPassword() {
			return newPassword;
		}

		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}

		@Length(min = 5, message = "password.error.checkNewPassword")
		public String getCheckNewPassword() {
			return checkNewPassword;
		}

		public void setCheckNewPassword(String checkNewPassword) {
			this.checkNewPassword = checkNewPassword;
		}

	}

}
