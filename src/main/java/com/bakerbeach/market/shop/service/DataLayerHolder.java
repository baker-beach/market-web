package com.bakerbeach.market.shop.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.bakerbeach.market.shop.model.DataLayer;

public class DataLayerHolder {
	public final static String DATA_LAYER_REQUEST_ATTRIBUTES_KEY = "dataLayer";

	@SuppressWarnings("unchecked")
	public static DataLayer getInstance() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

		Map<String, Object> sessionBasedDataLayer = (Map<String, Object>) requestAttributes
				.getAttribute(DATA_LAYER_REQUEST_ATTRIBUTES_KEY, RequestAttributes.SCOPE_SESSION);
		if (sessionBasedDataLayer == null) {
			sessionBasedDataLayer = new HashMap<>();
			requestAttributes.setAttribute(DATA_LAYER_REQUEST_ATTRIBUTES_KEY, sessionBasedDataLayer,
					RequestAttributes.SCOPE_SESSION);
		}

		Map<String, Object> requestBasedDataLayer = (Map<String, Object>) requestAttributes
				.getAttribute(DATA_LAYER_REQUEST_ATTRIBUTES_KEY, RequestAttributes.SCOPE_REQUEST);
		if (requestBasedDataLayer == null) {
			requestBasedDataLayer = new HashMap<>();
			requestAttributes.setAttribute(DATA_LAYER_REQUEST_ATTRIBUTES_KEY, requestBasedDataLayer,
					RequestAttributes.SCOPE_REQUEST);
		}

		return new DataLayer(sessionBasedDataLayer, requestBasedDataLayer);
	}

}
