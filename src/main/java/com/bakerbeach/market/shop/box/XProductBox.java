package com.bakerbeach.market.shop.box;

import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.cms.service.PageService;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.translation.api.service.TranslationService;
import com.bakerbeach.market.xcatalog.model.Group;
import com.bakerbeach.market.xcatalog.model.Product;
import com.bakerbeach.market.xcatalog.model.Product.Status;
import com.bakerbeach.market.xcatalog.model.SearchResult;
import com.bakerbeach.market.xcatalog.service.XCatalogService;

@Component("com.bakerbeach.market.shop.box.XProductBox")
@Scope("prototype")
public class XProductBox extends AbstractBox implements ProcessableBox {
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_PRODUCT_DETAIL_TEMPLATE = "product-detail";
	
//	private ShopProduct product = null;

	@Autowired
	@Qualifier("catalogService")
	protected XCatalogService cs;

	@Autowired
	protected PageService pageService;

	@Autowired
	protected TranslationService translationService;
	
	@SuppressWarnings("deprecation")
	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws ProcessableBoxException{
		ShopContext context = ShopContextHolder.getInstance();

		String code = getCode(context);
		if (StringUtils.isNotBlank(code)) {
			try {
				Locale locale = context.getCurrentLocale();
				String priceGroup = context.getCurrentPriceGroup();
				Currency currency = Currency.getInstance(context.getCurrency());
				String countryOfDelivery = context.getCountryOfDelivery();
				String shopCode = context.getShopCode();
				Date date = new Date();
				
				Group group = cs.groupByCode(shopCode, Status.PUBLISHED, locale, priceGroup, currency, countryOfDelivery, date, "code", code);

				getData().put("group", group);
				modelMap.addAttribute("productBox", this);
			} catch (Exception e) {
				log.warn(ExceptionUtils.getMessage(e));
				
				Redirect redirect = new Redirect("/", null, Redirect.RAW);
				throw new RedirectException(redirect);
			}
		} else {
			Redirect redirect = new Redirect("/", null, Redirect.RAW);
			throw new RedirectException(redirect);	
		}
	}

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {		
		Map<String, Object> data = getData();
		if (data.containsKey("group")) {
			try {
				Group group = (Group) getData().get("group");
				
				String template = group.getTemplate();
				if (StringUtils.isNotBlank(template)) {
					getData().put("template", template);
				} else {
					getData().put("template", DEFAULT_PRODUCT_DETAIL_TEMPLATE);
				}
				
				setActiveMember(request, group, modelMap);
				
			} catch (Exception e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	protected String getCode(ShopContext context) {
		if (context.getRequestData().containsKey("code")) {
			return (String) context.getRequestData().get("code");
		} else if (context.getRequestData().containsKey("group")) {
			return (String) context.getRequestData().get("group");
		} else {
			return null;
		}
	}

	// just setting first available here
	protected void setActiveMember(HttpServletRequest request, Group group, ModelMap modelMap) {
		if (!group.getMembers().isEmpty()) {
			String code = group.getMembers().get(0).getCode();
			for (Product member : group.getMembers()) {
				if (member.isAvailable()) {
					code = member.getCode();
					break;
				}
			}
			getData().put("active", code);
		}
	}
	
	@Override
	public String toString() {
		return "";
	}

//	public ShopProduct getProduct() {
//		return product;
//	}
}
