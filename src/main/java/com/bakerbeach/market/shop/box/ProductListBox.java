package com.bakerbeach.market.shop.box;

import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.catalog.model.CatalogSearchResult;
import com.bakerbeach.market.catalog.model.GroupedProduct;
import com.bakerbeach.market.catalog.model.Pager;
import com.bakerbeach.market.cms.box.Box;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.core.api.model.FilterList;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.translation.api.model.I18NMessage;
import com.bakerbeach.market.translation.service.TranslationServiceException;

@Component("com.bakerbeach.market.shop.box.ProductListBox")
@Scope("prototype")
public class ProductListBox extends AbstractProductListBox implements ProcessableBox {
	protected static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ProductListBox.class);
	
	private static final String PATH_PREFIX_KEY = "path_prefix";
	private static final String DEFAULT_PATH_PREFIX= "/products";
	
	protected String pathPrefix = DEFAULT_PATH_PREFIX;

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {
		ShopContext cmsContext = ShopContextHolder.getInstance();

		Locale locale = cmsContext.getCurrentLocale();
		String priceGroup = cmsContext.getCurrentPriceGroup();
		Currency currency = Currency.getInstance(cmsContext.getCurrency());
		String assortmentCode = cmsContext.getAssortmentCode();
		String countryOfDelivery = cmsContext.getCountryOfDelivery();
		Date date = new Date();

		if (cmsContext.getRequestData().containsKey(PATH_PREFIX_KEY)) {
			pathPrefix = (String) cmsContext.getRequestData().get(PATH_PREFIX_KEY);
		}
		
		String groupKey = DEFAULT_GROUP_KEY_VALUE;
		if (getData().containsKey(GROUP_KEY_PARAM)) {
			groupKey = (String) getData().get(GROUP_KEY_PARAM);
		}
		
		Map<String, String[]> parameter = new HashMap<String, String[]>(request.getParameterMap());
		String sort = getSort(parameter, groupKey);
		Pager pager = getPageInfo(parameter);
		String query = getQuery(parameter);

		for (String key : parameter.keySet()) {
			for (int i = 0; i < parameter.get(key).length; i++) {
				try {
					I18NMessage msg = translationService.getReverseUrlTranslation(Arrays.asList(key), parameter.get(key)[i], LocaleUtils.toLocale(locale.getLanguage()));
					parameter.get(key)[i] = msg.getText(locale.getLanguage());					
				} catch (TranslationServiceException e) {
					log.error(ExceptionUtils.getStackTrace(e));
				}
			}			
		}
		
		if (sort == null) {
			sort = groupKey.concat("_sort asc");
		}

		CatalogSearchResult catalogSearchResult = null;
		String filterUrl = null;
		
		Boolean isGetOnly = false;

		if (query != null) {
			filterUrl = new StringBuilder(request.getServletPath()).append("?").append(QUERY_PARAM).append("=")
					.append(query).toString();
			isGetOnly = true;
		} else if (getData().containsKey(QUERY_PARAM)) {
			query = (String) getData().get(QUERY_PARAM);
			filterUrl = cmsContext.getPath();
			isGetOnly = true;
		} else {
			try {
				query = "*:*";
				
				filterUrl = pathPrefix + "/";
				
				String path = cmsContext.getPath().replace(pathPrefix, "");
				ListValuedMap<String, String> urlParameter = getUrlParameter(locale, path);
				for (String key : urlParameter.keySet()) {			
					String[] values = urlParameter.get(key).toArray(new String[0]);
					if (parameter.containsKey(key)) {
						values = ArrayUtils.addAll(parameter.get(key), values);
					}
					parameter.put(key, values);						
				}
			} catch (TranslationServiceException e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}
		}

		try {
			FilterList filterList = getFilterList(isGetOnly, parameter);
			
			catalogSearchResult = catalogService.groupIndexQuery(locale, priceGroup, currency, assortmentCode,
					countryOfDelivery, date, filterList, query, groupKey, pager, sort);
			catalogSearchResult.setFilterUrl(filterUrl);
		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}

		modelMap.addAttribute("catalogSearchResult", catalogSearchResult);
	}

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		CatalogSearchResult catalogSearchResult = (CatalogSearchResult) modelMap.get("catalogSearchResult");
		Collection<GroupedProduct> groupedProducts = catalogSearchResult.getProducts();

		for (GroupedProduct groupedProduct : groupedProducts) {
			try {
				Box b = pageService.getBoxByType("product-list-item");
				b.setId(UUID.randomUUID().toString());
				b.getData().put("product", groupedProduct);
				addChildBox("products", b);
			} catch (Exception e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

}
