package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.core.api.model.FilterList;

@Component("com.bakerbeach.market.shop.box.FacetFilterBox")
@Scope("prototype")
public class FacetFilterBox extends AbstractBox {
	private static final long serialVersionUID = 1L;
	
	FilterList filterList;
	String filterUrl;

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
//		SearchResult catalogSearchResult = (SearchResult) modelMap.get("searchResult");
//		if (catalogSearchResult != null) {
//			filterList = catalogSearchResult.getFilterList();
//			filterUrl = catalogSearchResult.getFilterUrl();
//			getData().put("categoryInfo", catalogSearchResult.getCategoryInfo());			
//		}
	}

	@Override
	public String toString() {
		return "";
	}

	public FilterList getFilterList() {
		return filterList;
	}

	public String getFilterUrl() {
		return filterUrl;
	}
	
}
