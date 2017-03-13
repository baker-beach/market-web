package com.bakerbeach.market.shop.box;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.commons.MessagesImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.CartItem;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.Message;
import com.bakerbeach.market.core.api.model.Messages;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.box.XCartEditBox.AddToCartForm.ProductForm;
import com.bakerbeach.market.shop.box.XCartEditBox.QuantityUpdateCartForm.CartItemForm;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.translation.api.service.TranslationService;
import com.bakerbeach.market.xcart.api.service.XCartService;
import com.bakerbeach.market.xcart.api.service.XCartServiceException;
import com.bakerbeach.market.xcatalog.model.Price;
import com.bakerbeach.market.xcatalog.model.Product;
import com.bakerbeach.market.xcatalog.service.XCatalogService;

@Component("com.bakerbeach.market.shop.box.XCartEditBox")
@Scope("prototype")
public class XCartEditBox extends AbstractBox implements ProcessableBox {
	private static final String DEFAULT_EMPTY_CART_TMPL = "/cartEmpty";
	private static final long serialVersionUID = 1L;

	String emptyCartTemplate = DEFAULT_EMPTY_CART_TMPL;

	@Autowired
	@Qualifier("catalogService")
	protected XCatalogService catalogService;

	@Autowired
	private XCartService cartService;

	@Autowired
	protected TranslationService translationService;

	@SuppressWarnings("unchecked")
	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws ProcessableBoxException {
		try {
			ShopContext shopContext = ShopContextHolder.getInstance();
			Messages messages = (Messages) model.get("messages");
			Map<String, String[]> parameter = request.getParameterMap();

			if (request.getMethod().equals(RequestMethod.POST.toString())) {
				FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
				flashMap.put("messages", messages);

				if (shopContext.getRequestData() != null) {
					String operation = (String) shopContext.getRequestData().get("operation");
					if ("add".equals(operation)) {

						AddToCartForm addToCartForm = new AddToCartForm();
						BindingResult result = bind(addToCartForm, request);
						if (!result.hasErrors()) {
							List<CartItem> cartItems = getCartItems(addToCartForm);
							messages.addAll(addItems(shopContext, cartItems));
						} else {
							messages.add(new MessageImpl(Message.TYPE_ERROR, "unexpected"));
						}

					} else if ("update".equals(operation)) {
						QuantityUpdateCartForm cartForm = new QuantityUpdateCartForm();
						BindingResult result = bind(cartForm, request);
						if (!result.hasErrors()) {
							messages.addAll(updateQuantities(cartForm));
						} else {
							messages.add(new MessageImpl(Message.TYPE_ERROR, "unexpected"));
						}

					}

					String successTargetUrlId = null;
					if (parameter.containsKey("successTargetUrlId")) {
						successTargetUrlId = parameter.get("successTargetUrlId")[0];
					} else if (shopContext.getRequestData().containsKey("success_target_url_id")) {
						successTargetUrlId = (String) shopContext.getRequestData().get("success_target_url_id");
					} else if (getData().containsKey("success_target_url_id")) {
						successTargetUrlId = (String) getData().get("success_target_url_id");
					}

					if (successTargetUrlId != null && !messages.hasErrors()) {
						throw new RedirectException(new Redirect(successTargetUrlId, null, Redirect.URL_ID));
					} else {
						throw new RedirectException(new Redirect(request.getHeader("Referer"), null, Redirect.RAW));
					}
				}
			}
		} catch (RedirectException e) {
			throw e;
		} catch (Exception e) {
			throw new ProcessableBoxException();
		}
	}

	protected final Messages addItems(ShopContext shopContext, Collection<CartItem> cartItems)
			throws XCartServiceException {
		String shopCode = shopContext.getShopCode();

		Customer customer = CustomerHelper.getCustomer();
		Cart cart = CartHolder.getInstance(cartService, shopCode, customer);

		Messages messages = new MessagesImpl();

		for (CartItem cartItem : cartItems) {
			messages.addAll(cartService.addCartItem(shopContext, cart, cartItem));
		}

		cartService.calculate(shopContext, cart, customer);

		try {
			cartService.saveCart(customer, cart);
		} catch (Exception e) {
		}

		return messages;
	}

	protected final Messages updateQuantities(QuantityUpdateCartForm cartForm) throws XCartServiceException {
		ShopContext shopContext = ShopContextHolder.getInstance();
		String shopCode = shopContext.getShopCode();

		Customer customer = CustomerHelper.getCustomer();
		Cart cart = CartHolder.getInstance(cartService, shopCode, customer);

		Messages messages = new MessagesImpl();

		for (CartItemForm item : cartForm.getItems().values()) {
			if (item.getId() != null && item.getQuantity() != null) {
				messages.addAll(cartService.setQuantity(cart, item.getId(), new BigDecimal(item.getQuantity())));
			}
		}

		cartService.calculate(shopContext, cart, customer);

		try {
			cartService.saveCart(customer, cart);
		} catch (Exception e) {
		}

		return messages;
	}

	protected final List<CartItem> getCartItems(AddToCartForm productListForm) {
		List<CartItem> cartItems = new ArrayList<CartItem>();
		try {
			ShopContext cmsContext = ShopContextHolder.getInstance();
			String shopCode = cmsContext.getShopCode();
//			Locale locale = cmsContext.getCurrentLocale();
//			String priceGroup = cmsContext.getCurrentPriceGroup();
//			Currency currency = Currency.getInstance(cmsContext.getCurrency());
//			String countryOfDelivery = cmsContext.getCountryOfDelivery();
//			Date date = new Date();

			BigDecimal globalQuantity = new BigDecimal(productListForm.getQuantity());

			Map<String, ProductForm> requestedProducts = new HashMap<String, ProductForm>(
					productListForm.getProducts().size());
			for (String key : productListForm.getProducts().keySet()) {
				ProductForm product = productListForm.getProducts().get(key);
				if (product.getQuantity().compareTo(0) == 1) {
					requestedProducts.put(key, product);
				}
			}

			List<Product> products = catalogService.rawByGtin(shopCode, Product.Status.PUBLISHED,
					requestedProducts.keySet());
			// List<Product> products = catalogService.findByGtin(locale,
			// priceGroup, currency, countryOfDelivery, date,
			// requestedProducts.keySet());
			for (Product product : products) {
				ProductForm productForm = requestedProducts.get(product.getGtin());

				// get quantity either from global value or for individual
				// items.
				BigDecimal itemCount = (globalQuantity.compareTo(BigDecimal.ZERO) != 0) ? globalQuantity
						: new BigDecimal(productForm.getQuantity());

				CartItem cartItem = cartService.getNewCartItem(shopCode, product.getGtin(), itemCount);
				setCartItemAttributes(cartItem, product, cmsContext);

				cartItems.add(cartItem);
			}

		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}

		return cartItems;
	}
			
			/*
			 * 
			 * if (product instanceof BundleProduct) { BundleProduct bundle =
			 * (BundleProduct) product; for (BundleComponent component :
			 * bundle.getComponents()) { String componentName =
			 * component.getName();
			 * 
			 * if (component.isRequired()) { CartItemComponent cartItemComponent
			 * = cartService.getNewCartItemComponent(componentName);
			 * cartItem.getComponents().put(componentName, cartItemComponent);
			 * 
			 * for (BundleOption option : component.getOptions()) { String
			 * optionCode = option.getGtin();
			 * 
			 * if (option.isRequired()) { CartItemOption cartItemOption =
			 * cartService.getNewCartItemOption(optionCode);
			 * cartItemComponent.getOptions().put(optionCode, cartItemOption);
			 * setOptionAttributes(cartItemOption, option,
			 * option.getDefaultQty(), cmsContext.getCurrentLocale()); } } }
			 * 
			 * if (productForm.getComponents().containsKey(componentName)) {
			 * BundleComponentForm componentForm =
			 * productForm.getComponents().get(componentName);
			 * 
			 * for (BundleOption option : component.getOptions()) { String
			 * optionCode = option.getGtin();
			 * 
			 * BundleOptionForm optionForm =
			 * componentForm.getOptions().get(optionCode); if (optionForm !=
			 * null && optionForm.getQuantity() > 0) { Integer quantity =
			 * option.getDefaultQty(); if (optionForm.getQuantity() != null &&
			 * optionForm.getQuantity() >= option.getMinQty() &&
			 * optionForm.getQuantity() <= option.getMaxQty()) { quantity =
			 * optionForm.getQuantity(); } if (quantity > 0) { if
			 * (!cartItem.getComponents().containsKey(componentName)) {
			 * cartItem.getComponents().put(component.getName(),
			 * cartService.getNewCartItemComponent(componentName)); }
			 * 
			 * CartItemComponent cartItemComponent = cartItem.getComponents()
			 * .get(component.getName());
			 * 
			 * if (!cartItemComponent.getOptions().containsKey(optionCode)) {
			 * cartItemComponent.getOptions().put(option.getGtin(),
			 * cartService.getNewCartItemOption(optionCode)); }
			 * 
			 * CartItemOption cartItemOption =
			 * cartItemComponent.getOptions().get(optionCode);
			 * 
			 * setOptionAttributes(cartItemOption, option, quantity,
			 * cmsContext.getCurrentLocale()); } } } } }
			 * 
			 * BigDecimal unitPrice = product.getPrice(); BigDecimal
			 * monthlyUnitPrice = product.getMonthlyPrice(); if
			 * (monthlyUnitPrice == null) monthlyUnitPrice = BigDecimal.ZERO;
			 * for (CartItemComponent component :
			 * cartItem.getComponents().values()) { for (CartItemOption option :
			 * component.getOptions().values()) { unitPrice =
			 * unitPrice.add(option.getUnitPrice().multiply(new
			 * BigDecimal(option.getQuantity()))); if
			 * (option.getMonthlyUnitPrice() != null) { monthlyUnitPrice =
			 * monthlyUnitPrice.add(option.getMonthlyUnitPrice().multiply(new
			 * BigDecimal(option.getQuantity()))); } } }
			 * cartItem.setUnitPrice(unitPrice);
			 * cartItem.setMonthlyUnitPrice(monthlyUnitPrice); }
			 * 
			 */


	private void setCartItemAttributes(CartItem cartItem, Product product, ShopContext shopContext) {
		cartItem.setBrand(product.getBrand());
		cartItem.setTaxCode(product.getTaxCode());
		// TODO change with xcart project ---
		Price price = product.getPrice(java.util.Currency.getInstance(shopContext.getCurrentCurrency().getIsoCode()),
				shopContext.getCurrentPriceGroup(), new Date());
		
		cartItem.setUnitPrice(price.getCurrency(), price.getValues());
		
//		cartItem.setUnitPrice(price.getValues());
//		cartItem.put("title1", translationService.getMessage("product.cart.title1", "text", product.getGtin(), null, null, shopContext.getCurrentLocale()));
//		cartItem.put("title2", translationService.getMessage("product.cart.title2", "text", product.getGtin(), null, null, shopContext.getCurrentLocale()));
//		cartItem.put("title3", translationService.getMessage("product.cart.title3", "text", product.getGtin(), null, null, shopContext.getCurrentLocale()));
	}

//	protected void setCartItemAttributes(CartItem cartItem, Product product, Locale currentLocale) {
//		cartItem.setBrand(product.getBrand());
//		cartItem.setTaxCode(product.getTaxCode());
//		cartItem.setUnitPrice(product.getPrice());
//		cartItem.setUnitPrices(product.getPrices());
//		cartItem.setMonthlyUnitPrice(product.getMonthlyPrice());
//		
//		cartItem.setTitle1(translationService.getMessage("product.cart.title1", "text", product.getGtin(), null, null, locale));
//		cartItem.setTitle2(translationService.getMessage("product.cart.title2", "text", product.getGtin(), null, null, locale));
//		cartItem.setTitle3(translationService.getMessage("product.cart.title3", "text", product.getGtin(), null, null, locale));
//		
//		cartItem.setSize(translationService.getMessage("size", "text", product.getSize(), null, null, locale));
//		cartItem.setColor(translationService.getMessage("color", "text", product.getColor(), null, null, locale));
//		
//		Asset asset1 = product.getAsset("listing", 0, "m");
//		if (asset1 != null) {
//			cartItem.setImageUrl1(asset1.getPath());			
//		}		
//	}

	// protected void setCartItemAttributes(CartItem cartItem, Product product,
	// Locale locale) {
	// cartItem.setBrand(product.getBrand());
	// cartItem.setTaxCode(product.getTaxCode());
	// cartItem.setUnitPrice(product.getPrice());
	// cartItem.setUnitPrices(product.getPrices());
	// cartItem.setMonthlyUnitPrice(product.getMonthlyPrice());
	//
	// cartItem.setTitle1(translationService.getMessage("product.cart.title1",
	// "text", product.getGtin(), null, null, locale));
	// cartItem.setTitle2(translationService.getMessage("product.cart.title2",
	// "text", product.getGtin(), null, null, locale));
	// cartItem.setTitle3(translationService.getMessage("product.cart.title3",
	// "text", product.getGtin(), null, null, locale));
	//
	// cartItem.setSize(translationService.getMessage("size", "text",
	// product.getSize(), null, null, locale));
	// cartItem.setColor(translationService.getMessage("color", "text",
	// product.getColor(), null, null, locale));
	//
	// Asset asset1 = product.getAsset("listing", 0, "m");
	// if (asset1 != null) {
	// cartItem.setImageUrl1(asset1.getPath());
	// }
	// }

	// protected void setOptionAttributes(CartItemOption cartItemOption,
	// BundleOption option, Integer quantity, Locale locale) {
	// cartItemOption.setQuantity(quantity);
	// cartItemOption.setUnitPrice(option.getPrice());
	// cartItemOption.setUnitPrices(option.getPrices());
	// cartItemOption.setMonthlyUnitPrice(option.getMonthlyPrice());
	// String optionTitle1 = translationService.getMessage("option.title1",
	// "text", option.getGtin(), null,
	// option.getGtin(), locale);
	// cartItemOption.setTitle1(optionTitle1);
	// String optionTitle2 = translationService.getMessage("option.title2",
	// "text", option.getGtin(), null,
	// option.getGtin(), locale);
	// cartItemOption.setTitle2(optionTitle2);
	// String optionTitle3 = translationService.getMessage("option.title3",
	// "text", option.getGtin(), null,
	// option.getGtin(), locale);
	// cartItemOption.setTitle3(optionTitle3);
	// }

	public static class QuantityUpdateCartForm {
		private Map<String, CartItemForm> items = new HashMap<String, CartItemForm>();

		public Map<String, CartItemForm> getItems() {
			return items;
		}

		public static class CartItemForm {
			private String id;
			private Integer quantity;

			public CartItemForm(String id) {
				this.id = id;
			}

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public Integer getQuantity() {
				return quantity;
			}

			public void setQuantity(Integer quantity) {
				this.quantity = quantity;
			}

		}
	}

	public static class AddToCartForm {
		private Integer quantity = 0;
		private Map<String, ProductForm> products = new HashMap<String, ProductForm>();

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}

		public Map<String, ProductForm> getProducts() {
			return products;
		}

		public void setProducts(Map<String, ProductForm> products) {
			this.products = products;
		}

		public static class ProductForm {
			private String gtin;
			private Integer quantity = 1;
			private Map<String, BundleComponentForm> components = new HashMap<String, BundleComponentForm>();

			public ProductForm() {
			}

			public ProductForm(String gtin) {
				setGtin(gtin);
			}

			public String getGtin() {
				return gtin;
			}

			public void setGtin(String gtin) {
				this.gtin = gtin;
			}

			public Integer getQuantity() {
				return quantity;
			}

			public void setQuantity(Integer quantity) {
				this.quantity = quantity;
			}

			public Map<String, BundleComponentForm> getComponents() {
				return components;
			}

			public BundleComponentForm getComponent(String name) {
				if (name != null) {
					for (BundleComponentForm item : components.values()) {
						if (name.equals(item.getName())) {
							return item;
						}
					}
				}
				return null;
			}

			public void setComponents(Map<String, BundleComponentForm> components) {
				this.components = components;
			}

		}

		public static class BundleComponentForm {
			private String name;
			private Map<String, BundleOptionForm> options = new HashMap<String, BundleOptionForm>();

			public BundleComponentForm() {
			}

			public BundleComponentForm(String name) {
				setName(name);
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public Map<String, BundleOptionForm> getOptions() {
				return options;
			}

			public void setOptions(Map<String, BundleOptionForm> options) {
				this.options = options;
			}

			public void setOption(String gtin) {
				BundleOptionForm option = new BundleOptionForm(gtin);
				options.put(gtin, option);
			}

		}

		public static class BundleOptionForm {
			private String gtin;
			private Integer quantity = 1;

			public BundleOptionForm() {
			}

			public BundleOptionForm(String gtin) {
				this.gtin = gtin;
			}

			public String getGtin() {
				return gtin;
			}

			public void setGtin(String gtin) {
				this.gtin = gtin;
			}

			public Integer getQuantity() {
				return quantity;
			}

			public void setQuantity(Integer quantity) {
				this.quantity = quantity;
			}
		}
	}

}
