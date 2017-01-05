package com.bakerbeach.market.shop.box;

import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.catalog.model.CatalogSearchResult;
import com.bakerbeach.market.catalog.model.FieldOption;
import com.bakerbeach.market.catalog.model.GroupedProduct;
import com.bakerbeach.market.catalog.model.Pager;
import com.bakerbeach.market.catalog.service.CatalogService;
import com.bakerbeach.market.catalog.utils.FacetFilterFactory;
import com.bakerbeach.market.cms.box.Box;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.core.api.model.Filter;
import com.bakerbeach.market.core.api.model.FilterList;
import com.bakerbeach.market.core.api.model.Option;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.translation.service.TranslationServiceException;

@Component
@Scope("prototype")
public class SubCategoryBox extends AbstractProductListBox implements ProcessableBox {
	private static final Logger LOG = LoggerFactory.getLogger(SubCategoryBox.class);

	private static final long serialVersionUID = 1L;

	protected static final String QUERY_PARAM = "q";

	protected static final Integer DEFAULT_PAGE_SIZE = 120;
	protected static final Integer DEFAULT_OFFSET = 0;
	protected static final String DEFAULT_SORT = "";

	protected static final String PAGE_SIZE_PARAM = "pagesize";
	protected static final String PAGE_PARAM = "page";
	protected static final String SORT_PARAM = "sort";
	
	@SuppressWarnings({ "unchecked", "unused", "null", "deprecation" })
	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
			throws ProcessableBoxException {
		ShopContext cmsContext = ShopContextHolder.getInstance();

		CatalogService catalogService = null;
//		CatalogService catalogService = CatalogServiceHolder.getCatalogService();

		String path = cmsContext.getPath();

		Integer offset = DEFAULT_OFFSET;
		String sort = DEFAULT_SORT;
		Integer pageSize = DEFAULT_PAGE_SIZE;

		Map<String, String[]> parameters = new HashMap<String, String[]>(request.getParameterMap());
		if (parameters.containsKey(SORT_PARAM)) {
			sort = parameters.get(SORT_PARAM)[0];

			parameters.remove(SORT_PARAM);
		}
		if (parameters.containsKey(PAGE_SIZE_PARAM)) {
			pageSize = Integer.parseInt(parameters.get(PAGE_SIZE_PARAM)[0]);

			parameters.remove(PAGE_SIZE_PARAM);
		}
		if (parameters.containsKey(PAGE_PARAM)) {
			Integer pageNo = Integer.parseInt(parameters.get(PAGE_PARAM)[0]);
			offset = (pageNo * pageSize) - pageSize;

			parameters.remove(PAGE_PARAM);
		}

		CatalogSearchResult catalogSearchResult = null;

		String query = null;
		if (parameters.containsKey(QUERY_PARAM)) {
			query = parameters.get(QUERY_PARAM)[0];

			FilterList filterList = FacetFilterFactory.newInstanceSearch(request.getParameterMap(),
					cmsContext.getCurrentLocale().getLanguage());
			// catalogServiceResult =
			// serviceHolder.getCatalogService().getResultBySearchQuery(getShopContext(),
			// query, isRawSearch, filterList, customerPriceGroup,
			// customerTaxCode, country, pageSize, offset, sort);
			StringBuilder filterUrl = new StringBuilder(request.getServletPath());
			filterUrl.append("?").append(QUERY_PARAM).append("=").append(query);
			// catalogServiceResult.setFilterUrl(filterUrl.toString());

		} else {
			try {
				Locale locale = cmsContext.getCurrentLocale();
				String priceGroup = cmsContext.getCurrentPriceGroup();
				Currency currency = Currency.getInstance(cmsContext.getCurrency());
				String assortmentCode = cmsContext.getAssortmentCode();
				String countryOfDelivery = cmsContext.getCountryOfDelivery();
				Date date = new Date();

				ListValuedMap<String, String> urlParameter = getUrlParameter(locale, path);

				FilterList filterList = FacetFilterFactory.newInstance(false);
				for (String key : urlParameter.keySet()) {
					Filter filter = filterList.get(key);
					if (filter != null) {
						List<String> values = urlParameter.get(key);
						for (String urlCode : values) {
							String code = urlCode.replaceFirst("\\.url$", "");
							Option option = new FieldOption(code, null, true);
//							Option option = new FieldOption(code, urlCode, null, true);
							filter.addOption(option);
							filter.setActive(true);
						}
					}
				}
				
				Map<String, String[]> parameter = new HashMap<String, String[]>(request.getParameterMap());
				Pager pager = getPageInfo(parameter);

				catalogSearchResult = catalogService.groupIndexQuery(locale, priceGroup, currency,
						assortmentCode, countryOfDelivery, date, filterList, "*:*", "primary_group", pager, sort);
				
			} catch (TranslationServiceException e) {
				// TODO Auto-generated catch block
				LOG.error(ExceptionUtils.getStackTrace(e));
			}

			// FilterList filterList =
			// serviceHolder.getCmsService().getFilterList(path, parameters,
			// cmsContext);
			// catalogSearchResult = catalogService.findProducts(cmsContext,
			// pageSize, offset, "primary_group_sort asc");
			//
			// if (filterList.containsId(FacetFilterFactory.CATEGORY_KEY)){
			// Filter categoryFilter =
			// filterList.get(FacetFilterFactory.CATEGORY_KEY);
			// List<Option> categoryValues =
			// categoryFilter.getSelectedOptions();
			// if(categoryValues.size() > 0) {
			// Option option1 = categoryValues.get(0);
			// String value = option1.getValue();
			//
			// getData().put("category", value);
			// }
			// }

		}

		modelMap.addAttribute("catalogSearchResult", catalogSearchResult);
	}

	@SuppressWarnings("null")
	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		CatalogSearchResult catalogSearchResult = (CatalogSearchResult) modelMap.get("catalogSearchResult");
		Collection<GroupedProduct> groupedProducts = catalogSearchResult.getProducts();

		for (GroupedProduct groupedProduct : groupedProducts) {
			try {
				Box b = null;
//				Box b = PageServiceHolder.getPageService().getBoxByType("productListItem");
				b.setId(UUID.randomUUID().toString());
				b.getData().put("product", groupedProduct);
				addChildBox("products", b);
				/*
				if (groupedProduct.getAssets() != null && !groupedProduct.getAssets().isEmpty()) {
					Box b = PageServiceHolder.getPageService().getBoxByType("productListItem");
					b.setId(UUID.randomUUID().toString());
					b.getData().put("product", groupedProduct);
					addChildBox("products", b);
				}
				*/
			} catch (Exception e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	// public CatalogServiceResult getCatalogServiceResult() {
	// return catalogServiceResult;
	// }

	@Override
	public String toString() {
		return "";
	}

}
