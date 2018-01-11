package com.bakerbeach.market.shop.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.bakerbeach.market.cms.service.CmsContextHolder;
import com.bakerbeach.market.cms.service.Helper;
import com.bakerbeach.market.commons.Sanitization;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.Filter;
import com.bakerbeach.market.core.api.model.FilterList;
import com.bakerbeach.market.core.api.model.Option;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.customer.model.AnonymousCustomer;
import com.bakerbeach.market.order.api.model.OrderItem;

@Component
@Scope("prototype")
public class ShopHelper extends Helper {

	public static Boolean isIdentified() {
		Object principal = SecurityUtils.getSubject().getPrincipal();
		return principal != null && !(principal instanceof AnonymousCustomer);
	}

	public static Boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();
	}

	public static BigDecimal getNetPrice(OrderItem orderItem) {
		try {
			BigDecimal hundred = new BigDecimal(100);
			BigDecimal netPrice = orderItem.getTotalPrice().multiply(hundred).setScale(2, BigDecimal.ROUND_HALF_UP);
			return netPrice.divide(hundred.add(orderItem.getTaxPercent()), 2, BigDecimal.ROUND_HALF_UP);
		} catch (Exception e) {
			e.printStackTrace();
			return BigDecimal.ZERO;
		}
	}

	public static Integer getRemainingDays(Date date) {
		try {
			Date now = new Date();
			int days = Days.daysBetween(new DateTime(now), new DateTime(date)).getDays();
			return days;
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getFullName(Customer customer) {
		return customer.getFirstName() + " " + customer.getMiddleName() + " " + customer.getLastName();
	}

	public static String cf(Object number, String language, String country) {
		return cf(number, new Locale(language, country));
	}

	public static String cf(Object number) {
		return cf(number, ShopContextHolder.getInstance().getCurrentLocale());
	}

	public static String cf(Object number, Locale locale) {
		if (number != null) {
			NumberFormat nf = NumberFormat.getInstance(locale);
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			nf.setMinimumIntegerDigits(1);
			StringBuilder buffer;
			buffer = new StringBuilder(ShopContextHolder.getInstance().getLeftCurrencySymbol())
					.append(nf.format(number)).append(ShopContextHolder.getInstance().getRightCurrencySymbol());
			return buffer.toString();
		} else {
			return null;
		}
	}

	public static String basePriceFormat(Object number, Object divisor) {
		return basePriceFormat(number, divisor, ShopContextHolder.getInstance().getCurrentLocale());
	}

	public static String basePriceFormat(Object number, Object divisor, Locale locale) {
		if (number != null && number instanceof Number && divisor != null && divisor instanceof Number) {
			Double basePrice = ((Number) number).doubleValue() / ((Number) divisor).doubleValue();

			NumberFormat nf = NumberFormat.getInstance(locale);
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			nf.setMinimumIntegerDigits(1);
			StringBuilder buffer;
			buffer = new StringBuilder(ShopContextHolder.getInstance().getLeftCurrencySymbol()).append(nf.format(basePrice)).append(ShopContextHolder.getInstance().getRightCurrencySymbol());
			return buffer.toString();
		} else {
			return null;
		}
	}

	public static String nf(Object number, String locale) {
		if (number != null) {
			NumberFormat nf = NumberFormat.getInstance(new Locale(locale));
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			nf.setMinimumIntegerDigits(1);
			return nf.format(number);
		} else {
			return null;
		}
	}

	public static String resourceUrlFeed(String key) {
		return url("/resources" + key);
	}

	public static String tf(Object dateOrTime, String pattern) {
		Locale locale = ShopContextHolder.getInstance().getCurrentLocale();
		return tf(dateOrTime, pattern, locale);
	}

	public static String tf(Object dateOrTime, String pattern, Locale locale) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
			if (dateOrTime instanceof LocalTime) {
				LocalTime time = (LocalTime) dateOrTime;
				return time.format(formatter);
			} else if (dateOrTime instanceof LocalDateTime) {
				LocalDateTime dateTime = (LocalDateTime) dateOrTime;
				return dateTime.format(formatter);
			}
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
		}

		return dateOrTime.toString();
	}

	public static String df(Object date, String pattern) {
		Locale locale = ShopContextHolder.getInstance().getCurrentLocale();
		return df(date, pattern, locale);
	}

	public static String df(Object date, String pattern, Locale locale) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
			if (date instanceof LocalDate) {
				return ((LocalDate) date).format(formatter);
			} else if (date instanceof LocalDateTime) {
				return ((LocalDateTime) date).format(formatter);
			}
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
		}

		return date.toString();
	}

	public static String currencySymbol() {
		return ShopContextHolder.getInstance().getLeftCurrencySymbol();
	}

	public static String getCategoryTranslationKey(String categoryId) {
		String code = categoryId + "-" + ShopContextHolder.getInstance().getShopCode().toLowerCase() + ".category";
		return code;
	}

	public String productUrl(String ean) {
		return productUrl(ean, null);
	}

	public String productUrl(String ean, String selectedEan) {
		String url;
		if (ShopContextHolder.getInstance().getShopType().equals(ShopContext.FLASH_TYPE))
			url = url(String.format("/product/%s/", ean));
		else
			url = pageUrl("product-" + ean);

		if (selectedEan != null && selectedEan.length() > 0)
			url = url + "?selectedean=" + selectedEan;

		return url;
	}

	public static String brandKey(String brand) {
		String code = Sanitization.sanitizeCode(brand).concat(".brand");
		return code;
	}

	public String brandUrl(String brand) {
		return pageUrl("brand-".concat(Sanitization.sanitizeCode(brand)));
	}

	public static int randomInt(int minimum, int maximum) {
		return minimum + (int) (Math.random() * maximum);
	}

	public String facetUrl(String filterUrl, FilterList filterList, Option currentOption) {
		Locale locale = ShopContextHolder.getInstance().getCurrentLocale();

		// if (!filterList.isTranslated()) {
		for (Filter filter : filterList.getAvailable()) {
			for (Option option : filter.getOptions()) {
				String msg = translationService.getMessage(filter.getId(), "url", option.getCode(), null,
						option.getCode(), locale);
				// String msg = translationService.getMessage("facet", "url", option.getCode(),
				// null, option.getCode(), locale);
				option.setValue(msg);
			}
		}
		// }

		StringBuilder url = new StringBuilder();

		if (StringUtils.isNotBlank(filterUrl)) {
			url.append(filterUrl);
		}

		Filter _filter = null;
		if (currentOption != null)
			_filter = currentOption.getFilter();

		for (Filter filter : filterList.getUrlRelevantIncluding(_filter)) {
			String _url = filter.toUrl(currentOption);
			url.append(_url);
		}

		StringBuilder params = new StringBuilder();
		for (Filter filter : filterList.getActiveFiltersWithGetParamtersIncluding(_filter)) {
			params.append(filter.toGetParameter(currentOption));
		}

		if (params.length() > 0) {
			if (url.indexOf("?") < 0)
				params.replace(0, 1, "?");
			url.append(params);
		}

		return url(url.toString());
	}

	public String substringAfterLast(String str, String separator) {
		return StringUtils.substringAfterLast(str, separator);
	}

	public ShopContext getShopContext() {
		return ShopContextHolder.getInstance();
	}

}
