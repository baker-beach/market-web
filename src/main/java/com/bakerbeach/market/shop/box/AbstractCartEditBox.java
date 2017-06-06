package com.bakerbeach.market.shop.box;

import java.math.BigDecimal;
import java.util.Arrays;
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
import org.springframework.validation.BindingResult;

import com.bakerbeach.market.cart.api.service.CartService;
import com.bakerbeach.market.cart.api.service.CartServiceException;
import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.commons.Messages;
import com.bakerbeach.market.commons.MessagesImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.CartItem;
import com.bakerbeach.market.core.api.model.CartItemQualifier;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.translation.api.service.TranslationService;
import com.bakerbeach.market.xcatalog.model.Asset;
import com.bakerbeach.market.xcatalog.model.Price;
import com.bakerbeach.market.xcatalog.model.Product;
import com.bakerbeach.market.xcatalog.model.Product.Option;
import com.bakerbeach.market.xcatalog.service.XCatalogService;

public abstract class AbstractCartEditBox extends AbstractBox implements ProcessableBox {
	private static final long serialVersionUID = 1L;
	
	@Autowired
	protected XCatalogService catalogService;

	@Autowired
	protected CartService cartService;

	@Autowired
	protected TranslationService translationService;

	protected void update(HttpServletRequest request, HttpServletResponse response, Messages messages) throws CartServiceException {
		ShopContext shopContext = ShopContextHolder.getInstance();
		Customer customer = CustomerHelper.getCustomer();
		Cart cart = CartHolder.getInstance(cartService, shopContext, customer);

		try {
			CartForm form = new CartForm();
			BindingResult result = bind(form, request);
			if (!result.hasErrors()) {
				
				BigDecimal defaultQuantity = new BigDecimal(form.getDefaultQuantity());
				form.getItems().forEach((k, v) -> {
					try {
						BigDecimal quantity = (v.getQuantity() != null)? v.getQuantity() : defaultQuantity;

						if (cart.getItems().containsKey(k)) {
							CartItem item = cart.getItems().get(k);
							
							if (CartForm.MODE_ADD.equals(form.getMode())) {
								quantity = item.getQuantity().add(quantity);
							}
							
							Messages msgs = cartService.setQuantity(cart, k, quantity);
							messages.addAll(msgs);
						} else {
							CartItem item = createNewCartItem(cart, v);

							item.setQuantity(quantity);

							Messages msgs = cartService.addCartItem(shopContext, cart, item);
							messages.addAll(msgs);
						}
					} catch (CartServiceException e) {
						log.warn(ExceptionUtils.getStackTrace(e));
					}
				});
				
			} else {
				messages.add(new MessageImpl(Message.TYPE_ERROR, "unexpected"));
			}

		} catch (Exception e) {
			throw(e);
		} finally {
			cartService.calculate(shopContext, cart, customer);
		}
	}

	private CartItem createNewCartItem(Cart cart, CartForm.CartItemForm form) throws CartServiceException {
		ShopContext shopContext = ShopContextHolder.getInstance();
		Currency currency = Currency.getInstance(shopContext.getCurrentCurrency().getIsoCode());
		String priceGroup = shopContext.getCurrentPriceGroup();
		Date date = new Date();
		
		List<Product> products = catalogService.rawByGtin(shopContext.getShopCode(), Product.Status.PUBLISHED, Arrays.asList(form.getId()));
		for (Product product : products) {
			
			// get implementation specific cartItem class ---
			CartItem cartItem = cart.getNewItem(product.getCode(), BigDecimal.ZERO);
			setCartItemAttributes(cartItem, product, shopContext);

			// TODO: options
			for (Option option : product.getOptions().values()) {
				BigDecimal quantity = BigDecimal.ZERO;
				if (form.getOptions().containsKey(option.getCode())) {
					quantity = form.getOption(option.getCode()).getQuantity();
				} else if (option.isRequired()) {
					if (StringUtils.isNotEmpty(option.getComponentCode())) {
						if (product.getComponents().containsKey(option.getComponentCode())) {
							if (product.getComponent(option.getComponentCode()).isRequired()) {
								quantity = option.getDefaultQty();
							}
						}
					} else {
						quantity = option.getDefaultQty();
					}
				}

				if (quantity.compareTo(BigDecimal.ZERO) == 1) {
					CartItem.Option cio = cartItem.newOption(option.getCode(), null);
					cio.setGtin(option.getGtin());
					cio.setQuantity(quantity);

					Price price = option.getPrice(currency, priceGroup, date);
					if (price != null) {
						cio.setUnitPrice("std", price.getValue());
						cio.multiplyUnitPrices(cio.getQuantity());
					}
				}
			}

			// use implementation specific id generation ---
			String id = cartItem.createId();
			cartItem.setId(id);
			
			// cart item price ---
			// TODO: change price model in product to tagged prices
			Price productPrice = product.getPrice(currency, priceGroup, date);
			cartItem.setUnitPrice("std", productPrice.getValue());
			cartItem.getOptions().forEach((k, o) -> {
				cartItem.addUnitPrices(o.getUnitPrices());
			});
			
			return cartItem;
		}
		
		
		throw new CartServiceException("not found");
	}
	
	protected final Messages addItems(ShopContext shopContext, Collection<CartItem> cartItems) throws CartServiceException {
		Messages messages = new MessagesImpl();
		
		try {
			String shopCode = shopContext.getShopCode();

			Customer customer = CustomerHelper.getCustomer();
			Cart cart = CartHolder.getInstance(cartService, shopContext, customer);


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

//	protected final Messages updateQuantities(QuantityUpdateCartForm cartForm) throws CartServiceException {
//		ShopContext shopContext = ShopContextHolder.getInstance();
//		String shopCode = shopContext.getShopCode();
//
//		Customer customer = CustomerHelper.getCustomer();
//		Cart cart = CartHolder.getInstance(cartService, shopContext, customer);
//
//		Messages messages = new MessagesImpl();
//
//		for (CartItemForm item : cartForm.getItems().values()) {
//			if (item.getId() != null && item.getQuantity() != null) {
//				messages.addAll(cartService.setQuantity(cart, item.getId(), new BigDecimal(item.getQuantity())));
//			}
//		}
//
//		cartService.calculate(shopContext, cart, customer);
//
//		try {
//			cartService.saveCart(customer, cart);
//		} catch (Exception e) {
//		}
//
//		return messages;
//	}


//	protected final List<CartItem> getNewCartItems(AddToCartForm productListForm) {
//		List<CartItem> cartItems = new ArrayList<CartItem>();
//		try {
//			ShopContext cmsContext = ShopContextHolder.getInstance();
//			String shopCode = cmsContext.getShopCode();
//			String priceGroup = cmsContext.getCurrentPriceGroup();
//			Currency currency = Currency.getInstance(cmsContext.getCurrency());
//			Date date = new Date();
//			Customer customer = CustomerHelper.getCustomer();
//
//			Cart cart = CartHolder.getInstance(cartService, cmsContext, customer);
//			
//			BigDecimal globalQuantity = new BigDecimal(productListForm.getQuantity());
//
//			Map<String, ProductForm> toBeRequested = new HashMap<String, ProductForm>();
//			for (Entry<String, ProductForm> e : productListForm.getProducts().entrySet()) {
//				if (!cart.getItems().containsKey(e.getKey())) {
//					toBeRequested.put(e.getKey(), e.getValue());
//				}
//			}
//			
//			List<Product> products = catalogService.rawByGtin(shopCode, Product.Status.PUBLISHED,
//					toBeRequested.keySet());
//			for (Product product : products) {
//				ProductForm productForm = toBeRequested.get(product.getGtin());
//			
//				BigDecimal itemCount = (globalQuantity.compareTo(BigDecimal.ZERO) != 0) ? globalQuantity
//						: productForm.getQuantity();
//			
//				CartItem cartItem = cart.getNewItem(product.getCode(), itemCount);
//				setCartItemAttributes(cartItem, product, cmsContext);
//
//				for (Option po : product.getOptions().values()) {
//					BigDecimal quantity = BigDecimal.ZERO;
//					if (productForm.getOptions().containsKey(po.getCode())) {
//						quantity = productForm.getOption(po.getCode()).getQuantity();
//					} else if (po.isRequired()) {
//						if (StringUtils.isNotEmpty(po.getComponentCode())) {
//							if (product.getComponents().containsKey(po.getComponentCode())) {
//								if (product.getComponent(po.getComponentCode()).isRequired()) {
//									quantity = po.getDefaultQty();
//								}
//							}
//						} else {
//							quantity = po.getDefaultQty();
//						}
//					}
//
//					if (quantity.compareTo(BigDecimal.ZERO) == 1) {
//						CartItem.Option cio = cartItem.newOption(po.getCode(), null);
//						cio.setGtin(po.getGtin());
//						cio.setQuantity(quantity);
//
//						Price price = po.getPrice(currency, priceGroup, date);
//						if (price != null) {
//							BigDecimal optionPrice = price.getValue();
//							cio.setUnitPrice("std", price.getValue());
//							cio.multiplyUnitPrices(cio.getQuantity());
//						}
//					}
//				}
//
//				// cart item price ---
//				// TODO: change price model in product to tagged prices
//				Price productPrice = product.getPrice(currency, priceGroup, date);
//				cartItem.setUnitPrice("std", productPrice.getValue());
//				cartItem.getOptions().forEach((k, o) -> {
//					cartItem.addUnitPrices(o.getUnitPrices());
//				});
//
//				cartItems.add(cartItem);
//			}			
//		} catch (Exception e) {
//			log.error(ExceptionUtils.getStackTrace(e));
//		}
//
//		return cartItems;
//	}
	
//	protected final List<CartItem> getCartItems(AddToCartForm productListForm) {
//		List<CartItem> cartItems = new ArrayList<CartItem>();
//		try {
//			ShopContext cmsContext = ShopContextHolder.getInstance();
//			String shopCode = cmsContext.getShopCode();
//			String priceGroup = cmsContext.getCurrentPriceGroup();
//			Currency currency = Currency.getInstance(cmsContext.getCurrency());
//			Date date = new Date();
//			
//			Customer customer = CustomerHelper.getCustomer();
//			
//			Cart cart = CartHolder.getInstance(cartService, cmsContext, customer);
//			
//			BigDecimal globalQuantity = new BigDecimal(productListForm.getQuantity());
//			
//			Map<String, ProductForm> requestedProducts = new HashMap<String, ProductForm>(
//					productListForm.getProducts().size());
//			for (String key : productListForm.getProducts().keySet()) {
//				ProductForm product = productListForm.getProducts().get(key);
//				if (product.getQuantity().compareTo(BigDecimal.ZERO) == 1) {
//					requestedProducts.put(key, product);
//				}
//			}
//			
//			List<Product> products = catalogService.rawByGtin(shopCode, Product.Status.PUBLISHED,
//					requestedProducts.keySet());
//			for (Product product : products) {
//				ProductForm productForm = requestedProducts.get(product.getGtin());
//				
//				// get quantity either from global value or for individual
//				// items.
//				BigDecimal itemCount = (globalQuantity.compareTo(BigDecimal.ZERO) != 0) ? globalQuantity
//						: productForm.getQuantity();
//				
//				CartItem cartItem = cart.getNewItem(product.getCode(), itemCount);
//				setCartItemAttributes(cartItem, product, cmsContext);
//				
//				for (Option po : product.getOptions().values()) {
//					BigDecimal quantity = BigDecimal.ZERO;
//					if (productForm.getOptions().containsKey(po.getCode())) {
//						quantity = productForm.getOption(po.getCode()).getQuantity();
//					} else if (po.isRequired()) {
//						if (StringUtils.isNotEmpty(po.getComponentCode())) {
//							if (product.getComponents().containsKey(po.getComponentCode())) {
//								if (product.getComponent(po.getComponentCode()).isRequired()) {
//									quantity = po.getDefaultQty();
//								}
//							}
//						} else {
//							quantity = po.getDefaultQty();
//						}
//					}
//					
//					if (quantity.compareTo(BigDecimal.ZERO) == 1) {
//						CartItem.Option cio = cartItem.newOption(po.getCode(), null);
//						cio.setGtin(po.getGtin());
//						cio.setQuantity(quantity);
//						
//						Price price = po.getPrice(currency, priceGroup, date);
//						if (price != null) {
//							BigDecimal optionPrice = price.getValue();
//							cio.setUnitPrice("std", price.getValue());
//							cio.multiplyUnitPrices(cio.getQuantity());
//						}
//					}
//				}
//				
//				// cart item price ---
//				// TODO: change price model in product to tagged prices
//				Price productPrice = product.getPrice(currency, priceGroup, date);
//				cartItem.setUnitPrice("std", productPrice.getValue());
//				cartItem.getOptions().forEach((k, o) -> {
//					cartItem.addUnitPrices(o.getUnitPrices());
//				});
//				
//				cartItems.add(cartItem);
//			}
//			
//		} catch (Exception e) {
//			log.error(ExceptionUtils.getStackTrace(e));
//		}
//		
//		return cartItems;
//	}
	
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

	public static class CartForm {
		public static final String MODE_ADD = "add";
		public static final String MODE_SET = "set";
		
		private String mode = MODE_ADD;
		private Integer defaultQuantity = 1;
		private Map<String, CartItemForm> items = new HashMap<String, CartItemForm>();

		public String getMode() {
			return mode;
		}
		
		public void setMode(String mode) {
			this.mode = mode;
		}
		
		public Integer getDefaultQuantity() {
			return defaultQuantity;
		}
		
		public Map<String, CartItemForm> getItems() {
			return items;
		}
		
		public static class CartItemForm {
			private String id;
			private BigDecimal quantity;
			private Map<String, OptionForm> options = new LinkedHashMap<>();

			public CartItemForm(String id) {
				this.id = id;
			}

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
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

}
