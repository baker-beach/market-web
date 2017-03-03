package com.bakerbeach.market.shop.box;

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
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.catalog.model.CatalogSearchResult;
import com.bakerbeach.market.catalog.model.GroupedProduct;
import com.bakerbeach.market.cms.box.Box;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.xcatalog.model.Facets;
import com.bakerbeach.market.xcatalog.model.SearchResult;
import com.bakerbeach.market.xcatalog.model.Product.Status;

@Component("com.bakerbeach.market.shop.box.XProductListBox")
@Scope("prototype")
public class XProductListBox extends AbstractXProductListBox implements ProcessableBox {
	protected static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {
		
		Map<String, String[]> parameter = new HashMap<String, String[]>(request.getParameterMap());
		init(parameter);

		ShopContext cmsContext = ShopContextHolder.getInstance();
		Locale locale = cmsContext.getCurrentLocale();
		
		ListValuedMap<String, String> pathParameter = getPathParameter(locale, cmsContext.getPath(), pathPrefix);
		ListValuedMap<String, String> queryParameter = getQueryParameter(locale, parameter);
		
		ListValuedMap<String, String> parameterUnion = new ArrayListValuedHashMap<String, String>(pathParameter);
		parameterUnion.putAll(queryParameter);

		String priceGroup = cmsContext.getCurrentPriceGroup();
		Currency currency = Currency.getInstance(cmsContext.getCurrency());
		String countryOfDelivery = cmsContext.getCountryOfDelivery();
		Date date = new Date();

		SearchResult searchResult = null;
		try {
			Facets facets = getFacets(isGetOnly, parameterUnion);
			searchResult = catalogService.groupByIndexQuery(cmsContext.getShopCode(), Status.PUBLISHED, locale, priceGroup, currency, countryOfDelivery, date, facets, query, filterQueries, groupKey, pager, sort);
			searchResult.setFilterUrl(pathPrefix);
		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}

		modelMap.addAttribute("searchResult", searchResult);		
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
