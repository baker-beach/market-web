package com.bakerbeach.market.shop.filter;

import java.util.List;
import java.util.Map;

import org.apache.shiro.spring.web.ShiroFilterFactoryBean;

import com.bakerbeach.market.cms.model.UrlMappingInfo;
import com.bakerbeach.market.cms.service.UrlService;

public class MarketShiroFilterFactoryBean extends ShiroFilterFactoryBean{

	private UrlService urlService;
	
	private String defaultFilters; 
	
	public void initChainDefinition(){
		List<UrlMappingInfo> list = urlService.getFilterUrls();
		StringBuilder sb = new StringBuilder();
		for(UrlMappingInfo info : list){
			for(Map<String,String> mapping : info.getUrls()){
				sb.append(mapping.get("value")).append(" = ").append(info.get("filter")).append("\r");
			}
		}
		if(defaultFilters != null)
			sb.append("/**").append(" = ").append(defaultFilters).append("\r");
		setFilterChainDefinitions(sb.toString());
	}

	/**
	 * @return the urlService
	 */
	public UrlService getUrlService() {
		return urlService;
	}

	/**
	 * @param urlService the urlService to set
	 */
	public void setUrlService(UrlService urlService) {
		this.urlService = urlService;
	}

	/**
	 * @return the defaultFilter
	 */
	public String getDefaultFilters() {
		return defaultFilters;
	}

	/**
	 * @param defaultFilter the defaultFilter to set
	 */
	public void setDefaultFilters(String defaultFilters) {
		this.defaultFilters = defaultFilters;
	}
	

}
