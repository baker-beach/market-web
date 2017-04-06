package com.bakerbeach.market.shop.box;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.bakerbeach.market.core.api.model.CartItemQualifier;
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
import com.bakerbeach.market.xcatalog.model.Asset;
import com.bakerbeach.market.xcatalog.model.Price;
import com.bakerbeach.market.xcatalog.model.Product;
import com.bakerbeach.market.xcatalog.model.Product.Option;
import com.bakerbeach.market.xcatalog.service.XCatalogService;

@org.springframework.stereotype.Component("com.bakerbeach.market.shop.box.XCartEditBox")
@org.springframework.context.annotation.Scope("prototype")
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

	protected Messages addItems(ShopContext shopContext, Collection<CartItem> cartItems) throws XCartServiceException {
		Messages messages = new MessagesImpl();
		
		try {
			String shopCode = shopContext.getShopCode();

			Customer customer = CustomerHelper.getCustomer();
			Cart cart = CartHolder.getInstance(cartService, shopCode, customer);


			for (CartItem cartItem : cartItems) {
				messages.addAll(cartService.addCartItem(shopContext, cart, cartItem));
			}

			cartService.calculate(shopContext, cart, customer);
			cartService.saveCart(customer, cart);
		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
			messages.addGlobalError(new MessageImpl(Message.TYPE_ERROR, "addItems.error"));			
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
			String priceGroup = cmsContext.getCurrentPriceGroup();
			Currency currency = Currency.getInstance(cmsContext.getCurrency());
			Date date = new Date();

			BigDecimal globalQuantity = new BigDecimal(productListForm.getQuantity());

			Map<String, ProductForm> requestedProducts = new HashMap<String, ProductForm>(
					productListForm.getProducts().size());
			for (String key : productListForm.getProducts().keySet()) {
				ProductForm product = productListForm.getProducts().get(key);
				if (product.getQuantity().compareTo(BigDecimal.ZERO) == 1) {
					requestedProducts.put(key, product);
				}
			}

			List<Product> products = catalogService.rawByGtin(shopCode, Product.Status.PUBLISHED,
					requestedProducts.keySet());
			for (Product product : products) {
				ProductForm productForm = requestedProducts.get(product.getGtin());

				// get quantity either from global value or for individual
				// items.
				BigDecimal itemCount = (globalQuantity.compareTo(BigDecimal.ZERO) != 0) ? globalQuantity
						: productForm.getQuantity();

				CartItem cartItem = cartService.getNewCartItem(shopCode, product.getCode(), itemCount);
				setCartItemAttributes(cartItem, product, cmsContext);

				for (Option po : product.getOptions().values()) {
					BigDecimal quantity = BigDecimal.ZERO;
					if (productForm.getOptions().containsKey(po.getCode())) {
						quantity = productForm.getOption(po.getCode()).getQuantity();
					} else if (po.isRequired()) {
						if (StringUtils.isNotEmpty(po.getComponentCode())) {
							if (product.getComponents().containsKey(po.getComponentCode())) {
								if (product.getComponent(po.getComponentCode()).isRequired()) {
									quantity = po.getDefaultQty();
								}
							}
						} else {
							quantity = po.getDefaultQty();
						}
					}

					if (quantity.compareTo(BigDecimal.ZERO) == 1) {
						CartItem.Option cio = cartItem.newOption(po.getCode(), null);
						cio.setGtin(po.getGtin());
						cio.setQuantity(quantity);

						Price price = po.getPrice(currency, priceGroup, date);
						if (price != null) {
							BigDecimal optionPrice = price.getValue();
							cio.setUnitPrice("std", price.getValue());
							cio.multiplyUnitPrices(cio.getQuantity());
						}
					}
				}

				// cart item price ---
				// TODO: change price model in product to tagged prices
				Price productPrice = product.getPrice(currency, priceGroup, date);
				cartItem.setUnitPrice("std", productPrice.getValue());
				cartItem.getOptions().forEach((k, o) -> {
					cartItem.addUnitPrices(o.getUnitPrices());
				});

				cartItems.add(cartItem);
			}

		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}

		return cartItems;
	}

	protected void setCartItemAttributes(CartItem cartItem, Product product, ShopContext shopContext) {
		if (Product.Type.VPRODUCT.equals(product.getType())) {
			cartItem.setQualifier(CartItemQualifier.VPRODUCT);
		} else {
			cartItem.setQualifier(CartItemQualifier.PRODUCT);
		}

		cartItem.setBrand(product.getBrand());
		cartItem.setTaxCode(product.getTaxCode());

		// TODO change price object structure ---
		Price price = product.getPrice(java.util.Currency.getInstance(shopContext.getCurrentCurrency().getIsoCode()),
				shopContext.getCurrentPriceGroup(), new Date());

		cartItem.setUnitPrice("std", price.getValue());

		cartItem.getTitle().put("title1", translationService.getMessage("product.cart.title1", "text",
				product.getGtin(), null, "product.cart.title1", shopContext.getCurrentLocale()));
		cartItem.getTitle().put("title2", translationService.getMessage("product.cart.title2", "text",
				product.getGtin(), null, "product.cart.title2", shopContext.getCurrentLocale()));
		cartItem.getTitle().put("title3", translationService.getMessage("product.cart.title3", "text",
				product.getGtin(), null, "product.cart.title3", shopContext.getCurrentLocale()));

		List<Asset> assets = product.getAssets("listing", "s");
		if (CollectionUtils.isNotEmpty(assets)) {
			cartItem.getImages().put("img1", assets.get(0).getPath());
		}

	}

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
			private BigDecimal quantity = BigDecimal.ONE;
			private Map<String, OptionForm> options = new LinkedHashMap<>();

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

			public BigDecimal getQuantity() {
				return quantity;
			}

			public void setQuantity(BigDecimal quantity) {
				this.quantity = quantity;
			}

			public Map<String, OptionForm> getOptions() {
				return options;
			}

			public OptionForm getOption(String code) {
				return StringUtils.isNotEmpty(code) ? options.get(code) : null;
			}

			public void setOptions(Map<String, OptionForm> options) {
				this.options = options;
			}

		}

		public static class OptionForm {
			private String code;
			private BigDecimal quantity = BigDecimal.ONE;

			public OptionForm() {
			}

			public OptionForm(String gtin) {
				this.code = gtin;
			}

			public String getCode() {
				return code;
			}

			public void setCode(String code) {
				this.code = code;
			}

			public BigDecimal getQuantity() {
				return quantity;
			}

			public void setQuantity(BigDecimal quantity) {
				this.quantity = quantity;
			}

		}
	}

}
