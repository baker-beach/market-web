package com.bakerbeach.market.shop.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.model.Breadcrumbs;
import com.bakerbeach.market.cms.model.CmsContext;
import com.bakerbeach.market.cms.model.UrlMappingInfo;
import com.bakerbeach.market.cms.service.UrlHelper;
import com.bakerbeach.market.core.api.model.Address;
import com.bakerbeach.market.core.api.model.Currency;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.FilterList;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.core.service.order.model.OrderStatus;
import com.bakerbeach.market.payment.api.model.PaymentInfo;
import com.bakerbeach.market.shop.service.ShopHelper;

public class ShopContextImpl implements ShopContext, CmsContext {

	private static final long serialVersionUID = 1L;

	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	private ModelMap modelMap;

	private String helperClass = ShopHelper.class.getName();
	private String shopCode;
	private String bigGroupCode;
	private Boolean useBigGroupCode = true;
	private String orderSequenceCode;
	private Long orderSequenceRandomOffset = 1l;
	private List<String> registrationShopCode;
	private String shopType;
	private String cartCode;
	private String assortmentCode;
	private String host;
	private Integer port;
	private Integer securePort;
	private String path;
	private String protocol;
	private String pageId;
	private FilterList filterList;
	private Map<String, Object> reguestData = new HashMap<String, Object>();
	private Map<String, Object> sessionData = new HashMap<String, Object>();
	private Map<String, Currency> currencies = new HashMap<String, Currency>();
	private String defaultCurrency;
	private List<Locale> locales;
	private Locale defaultLocale;
	private Locale currentLocale;
	private List<String> priceGroups;
	private String defaultPriceGroup;
	private String currentPriceGroup;
	private List<String> validCountries;
	private String defaultCountryOfDelivery;
	private String deviceClass;
	private Address billingAddress;
	private Address shippingAddress;
	private String orderStatus = OrderStatus.POST_FRAUD;
	private String orderId;
	private Set<Integer> validSteps = new HashSet<Integer>();
	private List<String> newsletterIds;
	private String remoteIp;
	private Breadcrumbs breadcrumbs;
	private String solrUrl;
	private String device;
	private String gtmId;
	private String defaultPageId;
	private PaymentInfo paymentInfo;

	private String CURRENT_CURRENCY = "CURRENT_CURRENCY";

	public ShopContextImpl() {
		super();
	}

	public ShopContextImpl(ShopContext definition) {
		setShopCode(definition.getShopCode());
		setBigGroupCode(definition.getBigGroupCode());
		setUseBigGroupCode(definition.getUseBigGroupCode());
		setOrderSequenceCode(definition.getOrderSequenceCode());
		setOrderSequenceRandomOffset(definition.getOrderSequenceRandomOffset());
		setAssortmentCode(definition.getAssortmentCode());
		setHost(definition.getHost());
		setPort(definition.getPort());
		setSecurePort(definition.getSecurePort());
		setCartCode(definition.getCartCode());
		setLocales(definition.getLocales());
		setPriceGroups(definition.getPriceGroups());
		setValidCountries(definition.getValidCountries());
		setCurrencies(definition.getCurrencies());
		setDefaultCurrency(definition.getDefaultCurrency());
		setSolrUrl(definition.getSolrUrl());
		setNewsletterIds(definition.getNewsletterIds());
		setGtmId(definition.getGtmId());
		setShopType(definition.getShopType());
		setRegistrationShopCode(definition.getRegistrationShopCode());
	}

	@Override
	public ShopContext refine(Customer customer) {
		if (customer != null) {
			setCurrentPriceGroup(customer.getPriceGroup());
		}

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CmsContext refine(UrlMappingInfo urlMappingInfo) {
		if (urlMappingInfo != null) {
			String pageId = (String) urlMappingInfo.get("page_id");
			setPageId(pageId);
			setData((Map<String, Object>) urlMappingInfo.get("data"));
		}

		return this;
	}

	@Override
	public String getShopCode() {
		return shopCode;
	}

	@Override
	public void setShopCode(String shopCode) {
		this.shopCode = shopCode;
	}

	@Override
	public String getBigGroupCode() {
		return bigGroupCode;
	}
	
	@Override
	public Boolean getUseBigGroupCode() {
		return useBigGroupCode;
	}
	
	@Override
	public void setUseBigGroupCode(Boolean useBigGroupCode) {
		this.useBigGroupCode = useBigGroupCode;
	}

	@Override
	public String getOrderSequenceCode() {
		return orderSequenceCode;
	}

	@Override
	public void setOrderSequenceCode(String orderSequenceCode) {
		this.orderSequenceCode = orderSequenceCode;
	}

	@Override
	public Long getOrderSequenceRandomOffset() {
		return orderSequenceRandomOffset;
	}

	@Override
	public void setOrderSequenceRandomOffset(Long orderSequenceRandomOffset) {
		this.orderSequenceRandomOffset = orderSequenceRandomOffset;
	}

	@Override
	public void setBigGroupCode(String bigGroupCode) {
		this.bigGroupCode = bigGroupCode;
	}

	@Override
	public List<String> getRegistrationShopCode() {
		return registrationShopCode;
	}

	@Override
	public void setRegistrationShopCode(List<String> registrationShopCode) {
		this.registrationShopCode = registrationShopCode;
	}

	@Override
	public void setRegistrationShopCodeString(String registrationShopCodeString) {
		this.registrationShopCode = Arrays.asList(registrationShopCodeString.split(","));
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public Integer getPort() {
		return port;
	}

	@Override
	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public Integer getSecurePort() {
		return securePort;
	}

	@Override
	public void setSecurePort(Integer securePort) {
		this.securePort = securePort;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getPageId() {
		return pageId;
	}

	@Override
	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	@Override
	public FilterList getFilterList() {
		return filterList;
	}

	@Override
	public void setFilterList(FilterList filterList) {
		this.filterList = filterList;
	}

	@Override
	public Map<String, Object> getData() {
		return getRequestData();
	}

	@Override
	public void setData(Map<String, Object> data) {
		setReguestData(data);
	}

	@Override
	public Map<String, Object> getRequestData() {
		return reguestData;
	}

	public void setReguestData(Map<String, Object> data) {
		if (data != null)
			this.reguestData = data;
		else
			this.reguestData = new HashMap<String, Object>();
	}

	@Override
	public Map<String, Object> getSessionData() {
		return sessionData;
	}

	public void setSessionData(Map<String, Object> data) {
		if (data != null)
			this.sessionData = data;
		else
			this.reguestData = new HashMap<String, Object>();
	}

	@Override
	public String getCartCode() {
		return cartCode;
	}

	@Override
	public void setCartCode(String cartCode) {
		this.cartCode = cartCode;
	}

	@Override
	public List<Locale> getLocales() {
		return locales;
	}

	@Override
	public void setLocales(List<Locale> locales) {
		this.locales = locales;
	}

	public void setLocalesString(String localesStr) {
		this.locales = new ArrayList<Locale>();
		for (String str : localesStr.split(",")) {
			this.locales.add(LocaleUtils.toLocale(str));
		}
	}

	@Override
	public Locale getDefaultLocale() {
		if (defaultLocale != null) {
			return defaultLocale;
		} else {
			defaultLocale = locales.get(0);
			return defaultLocale;
		}
	}

	@Override
	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	@Override
	public Locale getCurrentLocale() {
		return (currentLocale != null) ? currentLocale : getDefaultLocale();
	}

	@Override
	public void setCurrentLocale(Locale currentLocale) {
		this.currentLocale = currentLocale;
	}

	@Override
	public List<String> getPriceGroups() {
		return priceGroups;
	}

	@Override
	public void setPriceGroups(List<String> priceGroups) {
		this.priceGroups = priceGroups;
	}

	public void setPriceGroupsString(String priceGroupsStr) {
		setPriceGroups(Arrays.asList(priceGroupsStr.split(",")));
	}

	@Override
	public String getDefaultPriceGroup() {
		if (defaultPriceGroup != null) {
			return defaultPriceGroup;
		} else {
			defaultPriceGroup = priceGroups.get(0);
			return defaultPriceGroup;
		}
	}

	@Override
	public void setDefaultPriceGroup(String defaultPriceGroup) {
		this.defaultPriceGroup = defaultPriceGroup;
	}

	@Override
	public String getCurrentPriceGroup() {
		return (currentPriceGroup != null) ? currentPriceGroup : getDefaultPriceGroup();
	}

	@Override
	public void setCurrentPriceGroup(String currentPriceGroup) {
		this.currentPriceGroup = currentPriceGroup;
	}

	@Override
	public List<String> getValidCountries() {
		return validCountries;
	}

	@Override
	public void setValidCountries(List<String> validCountries) {
		this.validCountries = validCountries;
	}

	public void setValidCountriesString(String validCountriesStr) {
		setValidCountries(Arrays.asList(validCountriesStr.split(",")));
	}

	@Override
	public boolean isCountryValid(String countryCode) {
		return validCountries.contains(countryCode);
	}

	@Override
	public String getCountryOfDelivery() {
		if (getShippingAddress() != null && getShippingAddress().getCountryCode() != null)
			return getShippingAddress().getCountryCode();
		else
			return getDefaultCountryOfDelivery();

	}

	@Override
	public String getDefaultCountryOfDelivery() {
		if (defaultCountryOfDelivery != null) {
			return defaultCountryOfDelivery;
		} else {
			defaultCountryOfDelivery = validCountries.get(0);
			return defaultCountryOfDelivery;
		}
	}

	@Override
	public void setDefaultCountryOfDelivery(String defaultCountryOfDelivery) {
		this.defaultCountryOfDelivery = defaultCountryOfDelivery;
	}

	@Override
	public String getDeviceClass() {
		return deviceClass;
	}

	@Override
	public void setDeviceClass(String deviceClass) {
		this.deviceClass = deviceClass;
	}

	@Override
	public Address getBillingAddress() {
		return billingAddress;
	}

	@Override
	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}

	@Override
	public Address getShippingAddress() {
		return shippingAddress;
	}

	@Override
	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	@Override
	public String getOrderStatus() {
		return orderStatus;
	}

	@Override
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Override
	public String getOrderId() {
		return orderId;
	}

	@Override
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Override
	public Set<Integer> getValidSteps() {
		return validSteps;
	}

	@Override
	public void setValidSteps(Set<Integer> validSteps) {
		this.validSteps = validSteps;
	}

	@Override
	public String getRemoteIp() {
		return remoteIp;
	}

	@Override
	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Breadcrumbs getBreadcrumbs() {
		return breadcrumbs;
	}

	public void setBreadcrumbs(Breadcrumbs breadcrumbs) {
		this.breadcrumbs = breadcrumbs;
	}

	@Override
	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@Override
	public String getSolrUrl() {
		return solrUrl;
	}

	@Override
	public String getAssortmentCode() {
		return assortmentCode;
	}

	@Override
	public void setAssortmentCode(String assortmentCode) {
		this.assortmentCode = assortmentCode;
	}

	@Override
	public String getDevice() {
		return device;
	}

	@Override
	public void setDevice(String device) {
		this.device = device;
	}

	@Override
	public void setGtmId(String gtmId) {
		this.gtmId = gtmId;
	}

	@Override
	public String getGtmId() {
		return gtmId;
	}

	public List<String> getNewsletterIds() {
		return newsletterIds;
	}

	public void setNewsletterIds(List<String> newsletterIds) {
		this.newsletterIds = newsletterIds;
	}

	public void setNewsletterIdsString(String str) {
		setNewsletterIds(Arrays.asList(str.split(",")));
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getShopType() {
		return shopType;
	}

	public void setShopType(String shopType) {
		this.shopType = shopType;
	}

	@Override
	public String getAppCode() {
		return this.shopCode;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}

	public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
	}

	public ModelMap getModelMap() {
		return modelMap;
	}

	public void setModelMap(ModelMap modelMap) {
		this.modelMap = modelMap;
	}

	public String getHelperClass() {
		return helperClass;
	}

	public void setHelperClass(String helperClass) {
		this.helperClass = helperClass;
	}

	public String getLeftCurrencySymbol() {
		if (isCurrencySymbolAtFront()) {
			return getCurrencySymbol() + "&nbsp;";
		} else {
			return "";
		}
	}

	public String getRightCurrencySymbol() {
		if (isCurrencySymbolAtFront()) {
			return "";
		} else {
			return getCurrencySymbol() + "&nbsp;";
		}
	}

	public String getDefaultPageId() {
		return defaultPageId;
	}

	public void setDefaultPageId(String defaultPageId) {
		this.defaultPageId = defaultPageId;
	}

	public PaymentInfo getPaymentInfo() {
		return paymentInfo;
	}

	public void setPaymentInfo(PaymentInfo paymentInfo) {
		this.paymentInfo = paymentInfo;
	}

	@Override
	public String getApplicationPath() {
		String p = this.getHttpServletRequest().isSecure() ? "https" : "http";
		StringBuilder path = new StringBuilder(p);
		try {
			path.append("://").append(host).append(":")
					.append(("http".equals(p)) ? this.getPort() : this.getSecurePort())
					.append(UrlHelper.getContextPath(httpServletRequest, httpServletRequest.getCharacterEncoding()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path.toString();
	}

	public Currency getCurrentCurrency() {
		if (getSessionData().containsKey(CURRENT_CURRENCY))
			return currencies.get(getSessionData().get(CURRENT_CURRENCY));
		else
			return currencies.get(defaultCurrency);

	}

	@Override
	public void setCurrency(String currency) {
		if (currencies.containsKey(currency))
			getSessionData().put(CURRENT_CURRENCY, currency);
	}

	@Override
	public String getCurrency() {
		return getCurrentCurrency().getIsoCode();
	}

	@Override
	public Boolean isCurrencySymbolAtFront() {
		return getCurrentCurrency().isCurrencySymbolAtFront();
	}

	@Override
	public String getCurrencySymbol() {
		return getCurrentCurrency().getSymbol();
	}

	public String getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(String defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	public Map<String, Currency> getCurrencies() {
		return currencies;
	}

	public void setCurrencies(Map<String, Currency> currencies) {
		this.currencies = currencies;
	}

}
