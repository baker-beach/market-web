package com.bakerbeach.market.shop.box;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.validator.constraints.NotEmpty;
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
import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.commons.Messages;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.customer.api.service.CustomerService;
import com.bakerbeach.market.customer.api.service.CustomerServiceException;
import com.bakerbeach.market.customer.api.service.CustomerServiceException.CustomerNotFoundException;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.LostPasswordBox")
@Scope("prototype")
public class LostPasswordBox extends AbstractBox implements ProcessableBox {
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(LostPasswordBox.class.getName());

	@Autowired
	private CustomerService customerService;

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws ProcessableBoxException {

		Messages messages = (Messages) model.get("messages");
		if (request.getMethod().equals(RequestMethod.POST.toString())) {
			FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
			flashMap.put("messages", messages);

			LostPasswordForm form = new LostPasswordForm();
			BindingResult result = bind(form, request);
			if (!result.hasErrors()) {
				try {
					request.getRemoteHost();
					request.getScheme();
					request.getContextPath();
					
					try {
						ShopContext shopContext = ShopContextHolder.getInstance();
						shopContext.getHost();
						
						
						URI uri = new URI(request.getScheme(), shopContext.getHost(), request.getContextPath().concat("/resetpassword"), null);
						System.out.println(uri);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					ShopContext shopContext = ShopContextHolder.getInstance();
					String token = customerService.createResetPasswordToken(form.getEmail(), shopContext.getShopCode());
					messages.add(new MessageImpl(Message.TYPE_INFO, "passwordToken.success"));
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
		
	}
	
	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		// TODO Auto-generated method stub
		super.handleRenderRequest(request, response, modelMap);
	}
	
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

}
