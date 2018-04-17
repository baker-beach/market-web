package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.commons.Messages;
import com.bakerbeach.market.customer.api.service.CustomerService;

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

		Messages messages = (Messages) model.get("messages");
		
		/*
		if (request.getMethod().equals(RequestMethod.POST.toString())) {
			FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
			flashMap.put("messages", messages);

			LostPasswordForm form = new LostPasswordForm();
			BindingResult result = bind(form, request);
			if (!result.hasErrors()) {
				try {
					ShopContext shopContext = ShopContextHolder.getInstance();
					customerService.renewPassword(form.getEmail(), shopContext.getShopCode());
					messages.add(new MessageImpl(Message.TYPE_INFO, "newPassword.success"));
					
					throw new RedirectException(new Redirect("login-password", null, Redirect.URL_ID));
				} catch (CustomerNotFoundException e) {
					messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "newPassword.error.customerNotfound"));					
				} catch (CustomerServiceException e) {
					log.error(ExceptionUtils.getStackTrace(e));
					messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "newPassword.error"));
				}
			} else {
				getFieldErrors(result, messages);
			}
			
			flashMap.put("form", form);
			throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
		}
		*/
		
	}
	
	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		super.handleRenderRequest(request, response, modelMap);
	}

	/*
	public static class LostPasswordForm {
		private String email;

		@NotEmpty(message = "lost-password.error.email")
		public String getEmail() {
			return email;
		}
		
		public void setEmail(String email) {
			this.email = email;
		}
		
	}
	*/

}
