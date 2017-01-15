package com.bakerbeach.market.shop.box;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.bakerbeach.market.catalog.model.FieldOption;
import com.bakerbeach.market.catalog.model.Pager;
import com.bakerbeach.market.catalog.service.CatalogService;
import com.bakerbeach.market.catalog.utils.FacetFactory;
import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.service.PageService;
import com.bakerbeach.market.core.api.model.Filter;
import com.bakerbeach.market.core.api.model.FilterList;
import com.bakerbeach.market.core.api.model.Option;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.translation.api.model.I18NMessage;
import com.bakerbeach.market.translation.api.service.TranslationService;
import com.bakerbeach.market.translation.service.TranslationServiceException;

public abstract class AbstractProductListBox extends AbstractBox {
	protected static final long serialVersionUID = 1L;

	protected static final String QUERY_PARAM = "q";
	protected String query;
	
	protected static final String GROUP_KEY_PARAM = "group_key";
	protected static final String DEFAULT_GROUP_KEY_VALUE = "primary_group";
	protected String groupKey = DEFAULT_GROUP_KEY_VALUE;

	protected static final String PATH_PREFIX_KEY = "path_prefix";
	protected static final String DEFAULT_PATH_PREFIX = "";	
	protected String pathPrefix = DEFAULT_PATH_PREFIX;

	protected static final String SORT_PARAM = "sort";
	protected String sort = null;
	
	protected static final Integer DEFAULT_PAGE_SIZE = 120;
	protected Integer pageSize = DEFAULT_PAGE_SIZE;

	protected static final Integer DEFAULT_PAGE_NUMBER = 1;
	protected Integer pageNumber = DEFAULT_PAGE_NUMBER;
	
	protected static final Integer DEFAULT_OFFSET = 0;
	protected static final String PAGE_SIZE_PARAM = "pagesize";
	protected static final String PAGE_PARAM = "page";
	
	protected List<String> filterQueries = new ArrayList<>();
	
	protected Boolean isGetOnly = false;

	protected Pager pager;

	@Autowired
	protected CatalogService catalogService;

	@Autowired
	protected PageService pageService;
	
	@Autowired
	protected TranslationService translationService;

	protected void init(Map<String, String[]> parameter) {
		ShopContext cmsContext = ShopContextHolder.getInstance();
		
		if (getData().containsKey(GROUP_KEY_PARAM)) {
			groupKey = (String) getData().get(GROUP_KEY_PARAM);
		}
		
		if (cmsContext.getRequestData().containsKey(PATH_PREFIX_KEY)) {
			pathPrefix = (String) cmsContext.getRequestData().get(PATH_PREFIX_KEY);
		}
	
		if (parameter.containsKey(QUERY_PARAM)) {
			query= parameter.get(QUERY_PARAM)[0];
		}

		if (parameter.containsKey(SORT_PARAM)) {
			sort = (String) parameter.get(SORT_PARAM)[0];
		}
		if (StringUtils.isBlank(sort)) {
			sort = groupKey.concat("_sort asc");
		}

		if (parameter.containsKey(PAGE_SIZE_PARAM)) {
			pageSize = Integer.parseInt(parameter.get(PAGE_SIZE_PARAM)[0]);
			parameter.remove(PAGE_SIZE_PARAM);
		}
		
		if (parameter.containsKey(PAGE_PARAM)) {
			pageNumber = Integer.parseInt(parameter.get(PAGE_PARAM)[0]);
			parameter.remove(PAGE_PARAM);
		}
		
		pager = new Pager(pageSize, pageNumber);

		parameter.remove(SORT_PARAM);
		parameter.remove(QUERY_PARAM);
		parameter.remove(PAGE_SIZE_PARAM);
		parameter.remove(PAGE_PARAM);
		
	}

	protected String getGroupKey() {
		String groupKey = DEFAULT_GROUP_KEY_VALUE;
		if (getData().containsKey(GROUP_KEY_PARAM)) {
			groupKey = (String) getData().get(GROUP_KEY_PARAM);
		}
		
		return groupKey;
	}

	protected String getSort(Map<String, String[]> parameter, String groupKey) {
		if (parameter.containsKey(SORT_PARAM)) {
			String sort = (String) parameter.get(SORT_PARAM)[0];
			parameter.remove(SORT_PARAM);
			
			return sort;
		} else {
			return groupKey.concat("_sort asc");
		}
	}

	protected String getQuery(Map<String, String[]> parameter) {
		if (parameter.containsKey(QUERY_PARAM)) {
			String query= parameter.get(QUERY_PARAM)[0];
			parameter.remove(QUERY_PARAM);
			
			return query;
		} else {
			return null;
		}
	}
	
	protected Pager getPageInfo(Map<String, String[]> parameter) {
		Integer pageSize = DEFAULT_PAGE_SIZE;
		Integer pageNumber = DEFAULT_PAGE_NUMBER;
		
		if (parameter.containsKey(PAGE_SIZE_PARAM)) {
			pageSize = Integer.parseInt(parameter.get(PAGE_SIZE_PARAM)[0]);
			parameter.remove(PAGE_SIZE_PARAM);
		}
		
		if (parameter.containsKey(PAGE_PARAM)) {
			pageNumber = Integer.parseInt(parameter.get(PAGE_PARAM)[0]);
			parameter.remove(PAGE_PARAM);
		}
		
		return new Pager(pageSize, pageNumber);
	}
	
	protected FilterList getFilterList(Boolean isGetOnly, ListValuedMap<String, String> parameter) {	
		FilterList filterList = FacetFactory.newInstance(isGetOnly);

		for (String key : parameter.keySet()) {
			if (key.equals("min_price")) {
				Filter filter = filterList.get("price");
				if (filter != null) {
					Option option = new FieldOption(parameter.get("min_price").get(0), null, true);
					filter.addOption(option);
					filter.setActive(true);
				}				
			} else if (key.equals("max_price")) {
				Filter filter = filterList.get("price");
				if (filter != null) {
					Option option = new FieldOption(parameter.get("max_price").get(0), null, true);
					filter.addOption(option);
					filter.setActive(true);
				}
			} else {
				Filter filter = filterList.get(key);
				if (filter != null) {
					List<String> values = parameter.get(key);
					for (String code : values) {				
						Option option = new FieldOption(code, null, true);
						filter.addOption(option);
						filter.setActive(true);
					}
				}
			}
		}
		
		return filterList;
	}

	protected ListValuedMap<String, String> getQueryParameter(Locale locale, Map<String, String[]> requestParameter) {
		ListValuedMap<String, String> queryParameter = new ArrayListValuedHashMap<String, String>();
		
		for (String key : requestParameter.keySet()) {
			for (int i = 0; i < requestParameter.get(key).length; i++) {
				try {
					I18NMessage msg = translationService.getReverseUrlTranslation(Arrays.asList(key), requestParameter.get(key)[i], LocaleUtils.toLocale(locale.getLanguage()));
					queryParameter.put(msg.getTag(), msg.getText(locale.getLanguage()));
				} catch (TranslationServiceException e) {
					log.error(ExceptionUtils.getStackTrace(e));
				}
			}
		}
		
		return queryParameter;
	}
	
	protected ListValuedMap<String, String> getPathParameter(Locale locale, String path, String prefix) {
		ListValuedMap<String, String> parameter = new ArrayListValuedHashMap<String, String>();

		if (path.startsWith(prefix)) {
			path = path.replaceFirst(prefix, "");
		}
		
		String categporyPath = StringUtils.substringBefore(path, "_");
		categporyPath = StringUtils.trimToNull(StringUtils.strip(categporyPath, "/"));

		String attributePath = StringUtils.substringAfter(path, "_");
		attributePath = StringUtils.trimToNull(StringUtils.strip(attributePath, "_"));
		
		if (StringUtils.isNotBlank(categporyPath)) {
			List<String> tags = Arrays.asList("category");
			List<String> attributes = Arrays.asList(categporyPath);
			for (String attribute : attributes) {
				try {
					I18NMessage msg = translationService.getReverseUrlTranslation(tags, attribute,
							LocaleUtils.toLocale(locale.getLanguage()));
					parameter.put(msg.getTag(), msg.getText(locale.getLanguage()));
				} catch (TranslationServiceException e) {
					log.error(ExceptionUtils.getStackTrace(e));
				}
			}
		}
		
		if (StringUtils.isNotBlank(attributePath)) {
			List<String> tags = new ArrayList<>(FacetFactory.getUrlRelevantFilters());
			tags.remove("category");
			
			List<String> attributes = Arrays.asList(attributePath.split("_"));
			for (String attribute : attributes) {
				try {
					I18NMessage msg = translationService.getReverseUrlTranslation(tags, attribute,
							LocaleUtils.toLocale(locale.getLanguage()));
					parameter.put(msg.getTag(), msg.getText(locale.getLanguage()));
				} catch (TranslationServiceException e) {
					log.error(ExceptionUtils.getStackTrace(e));
				}
			}			
		}

		return parameter;
	}
	
	protected ListValuedMap<String, String> getUrlParameter(Locale locale, String path)
			throws TranslationServiceException {
		ListValuedMap<String, String> parameter = new ArrayListValuedHashMap<String, String>();
		
		
		String categporyPath = StringUtils.substringBefore(path, "/_");
		if (!categporyPath.isEmpty()) {
			List<String> tags = Arrays.asList("category");
			
			categporyPath = StringUtils.strip(categporyPath, "/");
			if (StringUtils.isNotBlank(categporyPath)) {
				List<String> categories = Arrays.asList(categporyPath);
				for (String category : categories) {
					I18NMessage msg = translationService.getReverseUrlTranslation(tags, category, LocaleUtils.toLocale(locale.getLanguage()));
					String key = msg.getTag().replaceAll("\\.url$", "");
					parameter.put(key, msg.getText(locale.getLanguage()));
				}				
			}
		}
		
		String attributePath = StringUtils.substringAfter(path, "/_");
		if (!attributePath.isEmpty()) {
			List<String> tags = new ArrayList<>(FacetFactory.getUrlRelevantFilters());
			tags.remove("category");
			
			List<String> attributes = Arrays.asList(attributePath.split("_"));
			for (String attribute : attributes) {
				I18NMessage msg = translationService.getReverseUrlTranslation(tags, attribute, LocaleUtils.toLocale(locale.getLanguage()));
				String key = msg.getTag().replaceAll("\\.url$", "");
				parameter.put(key, msg.getText(locale.getLanguage()));
			}
		}
		
		return parameter;
	}
	
	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	public void setIsGetOnly(Boolean isGetOnly) {
		this.isGetOnly = isGetOnly;
	}
	
}
