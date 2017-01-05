package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;

public abstract class AbstractCheckoutStepBox extends AbstractBox implements ProcessableBox{
	
	private static final long serialVersionUID = 1L;

	public final void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws ProcessableBoxException{
		ShopContext cmsContext = ShopContextHolder.getInstance();
		if(request.getMethod().equals("GET") && modelMap.get("checkout") == null && !cmsContext.getValidSteps().contains(getStep())) {
			if (modelMap.containsKey("messages")) {
				FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
				flashMap.put("messages", modelMap.get("messages"));
			}

			throw new RedirectException(new Redirect("checkout",null));
		}
		if(preHandleActionRequestForward(request, response, modelMap))
			handleActionRequestForward(request, response, modelMap);
	}
	
	protected abstract void handleActionRequestForward(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws ProcessableBoxException;
	
	public abstract Integer getStep();
	
	protected abstract boolean preHandleActionRequestForward(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws ProcessableBoxException;

}
