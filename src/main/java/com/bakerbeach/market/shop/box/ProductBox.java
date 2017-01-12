package com.bakerbeach.market.shop.box;

import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.catalog.model.CatalogSearchResult;
import com.bakerbeach.market.catalog.model.GroupedProduct;
import com.bakerbeach.market.catalog.model.ShopProduct;
import com.bakerbeach.market.catalog.service.CatalogService;
import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.cms.service.PageService;
import com.bakerbeach.market.core.api.model.Product;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.translation.api.service.TranslationService;

@Component("com.bakerbeach.market.shop.box.ProductBox")
@Scope("prototype")
public class ProductBox extends AbstractBox implements ProcessableBox {
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_PRODUCT_DETAIL_TEMPLATE = "productDetail";
	
	private ShopProduct product = null;

	@Autowired
	private CatalogService catalogService;

	@Autowired
	protected PageService pageService;

	@Autowired
	protected TranslationService translationService;
	
	@SuppressWarnings("deprecation")
	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws ProcessableBoxException{
		ShopContext context = ShopContextHolder.getInstance();

		String group = getGroup(context);
		if (StringUtils.isNotBlank(group)) {
			try {
				Locale locale = context.getCurrentLocale();
				String priceGroup = context.getCurrentPriceGroup();
				Currency currency = Currency.getInstance(context.getCurrency());
				String countryOfDelivery = context.getCountryOfDelivery();
				Date date = new Date();
				
				CatalogSearchResult catalogSearchResult = catalogService.findGroupByGroupCode(locale, priceGroup,
						currency, countryOfDelivery, date, Arrays.asList(group));
				GroupedProduct product = catalogSearchResult.getProducts().iterator().next();
				getData().put("product", product);
				
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
		

		
//		if (cmsContext.getData().containsKey("primary_group")) {
//			try {				
//				String primaryGroup = (String) cmsContext.getData().get("primary_group");
//				
//				Locale locale = cmsContext.getCurrentLocale();
//				String priceGroup = cmsContext.getCurrentPriceGroup();
//				Currency currency = Currency.getInstance(cmsContext.getCurrency());
//				String countryOfDelivery = cmsContext.getCountryOfDelivery();
//				Date date = new Date();
//				
//				CatalogSearchResult catalogSearchResult = catalogService.findGroupByGroupCode(locale, priceGroup,
//						currency, countryOfDelivery, date, Arrays.asList(primaryGroup));
//				GroupedProduct product = catalogSearchResult.getProducts().iterator().next();
//				getData().put("product", product);
//				
//				modelMap.addAttribute("productBox", this);				
//			} catch (Exception e) {
//				Redirect redirect = new Redirect("/", null, Redirect.RAW);
//				throw new RedirectException(redirect);
//
//			}
//		} else {
//			Redirect redirect = new Redirect("/", null, Redirect.RAW);
//			throw new RedirectException(redirect);
//		}
	}

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {		
		Map<String, Object> data = getData();
		if (data.containsKey("product")) {
			try {
				GroupedProduct product = (GroupedProduct) getData().get("product");
				String template = product.getTemplate();
				if (StringUtils.isNotBlank(template)) {
					getData().put("template", template);
				} else {
					getData().put("template", DEFAULT_PRODUCT_DETAIL_TEMPLATE);
				}

				setActiveMember(request, product, modelMap);
			} catch (Exception e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	protected String getGroup(ShopContext context) {
		if (context.getRequestData().containsKey("primary_group")) {
			return (String) context.getRequestData().get("primary_group");
		} else if (context.getRequestData().containsKey("group")) {
			return (String) context.getRequestData().get("group");
		} else {
			return null;
		}
	}

	protected void setActiveMember(HttpServletRequest request, GroupedProduct product, ModelMap modelMap) {
		if (!product.getMembers().isEmpty()) {
			String gtin = product.getMembers().get(0).getGtin();
			for (Product member : product.getMembers()) {
				if (member.isAvailable()) {
					gtin = member.getGtin();
					break;
				}
			}
			getData().put("active", gtin);
		}
	}
	
	@Override
	public String toString() {
		return "";
	}

	public ShopProduct getProduct() {
		return product;
	}
}
