package com.bakerbeach.market.shop.box;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.core.api.model.Messages;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@org.springframework.stereotype.Component("com.bakerbeach.market.shop.box.XCartEditBox")
@org.springframework.context.annotation.Scope("prototype")
public class XCartEditBox extends AbstractCartEditBox implements ProcessableBox {
	private static final String DEFAULT_EMPTY_CART_TMPL = "/cartEmpty";
	private static final long serialVersionUID = 1L;

	String emptyCartTemplate = DEFAULT_EMPTY_CART_TMPL;

	@SuppressWarnings("unchecked")
	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws ProcessableBoxException {
		try {
			ShopContext shopContext = ShopContextHolder.getInstance();
			Messages messages = (Messages) model.get("messages");
			Map<String, String[]> parameter = request.getParameterMap();

			if (request.getMethod().equals(RequestMethod.POST.toString())) {
				request.getSession().setAttribute("messages", messages);

				update(request, response, messages);
				
				String successTargetUrlId = null;
				if (parameter.containsKey("successTargetUrlId")) {
					successTargetUrlId = parameter.get("successTargetUrlId")[0];
				} else if (shopContext.getRequestData().containsKey("success_target_url_id")) {
					successTargetUrlId = (String) shopContext.getRequestData().get("success_target_url_id");
				} else if (getData().containsKey("success_target_url_id")) {
					successTargetUrlId = (String) getData().get("success_target_url_id");
				}

				if (successTargetUrlId != null && !messages.hasErrors()) {
					throw new RedirectException(new Redirect(successTargetUrlId, null, Redirect.URL_ID));
				} else {
					throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
				}
			}
		} catch (RedirectException e) {
			throw e;
		} catch (Exception e) {
			throw new ProcessableBoxException();
		}
	}

}
