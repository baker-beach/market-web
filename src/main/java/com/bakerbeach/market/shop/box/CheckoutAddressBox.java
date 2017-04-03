package com.bakerbeach.market.shop.box;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.bakerbeach.market.address.api.service.CustomerAddressService;
import com.bakerbeach.market.address.api.service.CustomerAdressServiceException;
import com.bakerbeach.market.address.service.AddressCheckEngine;
import com.bakerbeach.market.cms.box.ProcessableBoxException;
import com.bakerbeach.market.cms.box.RedirectException;
import com.bakerbeach.market.cms.model.Redirect;
import com.bakerbeach.market.commons.FieldMessageImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.CustomerAddress;
import com.bakerbeach.market.core.api.model.Message;
import com.bakerbeach.market.core.api.model.Messages;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.shop.model.ShopCustomerAddress;
import com.bakerbeach.market.shop.model.forms.AddressForm;
import com.bakerbeach.market.shop.model.forms.AddressForm.AddressFormAddress;
import com.bakerbeach.market.shop.service.CartHolder;
import com.bakerbeach.market.shop.service.CheckoutStatusResolver;
import com.bakerbeach.market.shop.service.CustomerHelper;
import com.bakerbeach.market.shop.service.ShopContextHolder;
import com.bakerbeach.market.xcart.api.service.XCartService;

@Component("com.bakerbeach.market.shop.box.CheckoutAddressBox")
@Scope("prototype")
public class CheckoutAddressBox extends AbstractCheckoutStepBox{
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private XCartService cartService;

	private static String nl = "DR:Drenthe;FL:Flevoland;FR:Friesland;GE:Gelderland;GR:Groningen;LI:Limburg;NB:Noord-Brabant;NH:Noord-Holland;OV:Overijssel;UT:Utrecht;ZE:Zeeland;ZH:Zuid-Holland";
	private static String it = "AG:Agrigento;AL:Alessandria;AN:Ancona;AO:Aosta;AR:Arezzo;AP:Ascoli Piceno;AT:Asti;AV:Avellino;BA:Bari;BL:Belluno;BN:Benevento;BG:Bergamo;BI:Biella;BO:Bologna;BZ:Bolzano;BS:Brescia;BR:Brindisi;CA:Cagliari;CL:Caltanissetta;CB:Campobasso;CE:Caserta;CT:Catania;CZ:Catanzaro;CH:Chieti;CO:Como;CS:Cosenza;CR:Cremona;KR:Crotone;CN:Cuneo;EN:Enna;FE:Ferrara;FI:Firenze;FG:Foggia;FO:Forli-Cesena;FR:Frosinone;GE:Genova;GO:Gorizia;GR:Grosseto;IM:Imperia;IS:Isernia;SP:La Spezia;AQ:L’Aquila;LT:Latina;LE:Lecce;LC:Lecco;LI:Livorno;LO:Lodi;LU:Lucca;MC:Macerata;MN:Mantova;MS:Massa-Carrara;MT:Matera;ME:Messina;MI:Milano;MO:Modena;MB:Monza e Brianza;NA:Napoli;NO:Novara;NU:Nuoro;OR:Oristano;PD:Padova;PA:Palermo;PR:Parma;PV:Pavia;PG:Perugia;PS:Pesaro;PE:Pescara;PC:Piacenza;PI:Pisa;PT:Pistoia;PN:Pordenone;PZ:Potenza;PO:Prato;RG:Ragusa;RA:Ravenna;RC:Reggio Calabria;RE:Reggio Emilia;RI:Rieti;RN:Rimini;RM:Roma;RO:Rovigo;SA:Salerno;SS:Sassari;SV:Savona;SI:Siena;SR:Siracusa;SO:Sondrio;TA:Taranto;TE:Teramo;TR:Terni;TO:Torino;TP:Trapani;TN:Trento;TV:Treviso;TS:Trieste;UD:Udine;VA:Varese;VE:Venezia;VB:Verbania-Cusio-Ossola;VC:Vercelli;VR:Verona;VV:Vibo Valentia;VI:Vicenza;VT:Viterbo";
	private static String ca = "AB:Alberta;BC:British Columbia;MB:Manitoba;NB:New Brunswick;NL:Newfoundland;NT:Northwest Territories;NS:Nova Scotia;NU:Nunavut;ON:Ontario;PE:Prince Edward Island;QC:Quebec;SK:Saskatchewan;YT:Yukon";
	private static String us = "AL:Alabama;AK:Alaska;AZ:Arizona;AR:Arkansas;CA:California;CO:Colorado;CT:Connecticut;DE:Delaware;DC:District Of Columbia (Washington, D.C.);FL:Florida;GA:Georgia;HI:Hawaii;ID:Idaho;IL:Illinois;IN:Indiana;IA:Iowa;KS:Kansas;KY:Kentucky;LA:Louisiana;ME:Maine;MD:Maryland;MA:Massachusetts;MI:Michigan;MN:Minnesota;MS:Mississippi;MO:Missouri;MT:Montana;NE:Nebraska;NV:Nevada;NH:New Hampshire;NJ:New Jersey;NM:New Mexico;NY:New York;NC:North Carolina;ND:North Dakota;OH:Ohio;OK:Oklahoma;OR:Oregon;PA:Pennsylvania;PR:Puerto Rico;RI:Rhode Island;SC:South Carolina;SD:South Dakota;TN:Tennessee;TX:Texas;UT:Utah;VT:Vermont;VA:Virginia;WA:Washington;WV:West Virginia;WI:Wisconsin;WY:Wyoming<;AA:Armed Forces Americas;AE:Armed Forces;AP:Armed Forces Pacific;AS:American Samoa;GU:Guam;MP:Northern Mariana Islands;VI:Virgin Islands";
	
	private static final Map<String, Map<String, String>> regions = new HashMap<String, Map<String,String>>();
	
	private static final Map<String, String> nlRegions = new LinkedHashMap<String, String>();
	private static final Map<String, String> itRegions = new LinkedHashMap<String, String>();
	private static final Map<String, String> caRegions = new LinkedHashMap<String, String>();
	private static final Map<String, String> usRegions = new LinkedHashMap<String, String>();
	static {
		for (String kv : nl.split(";")) {
			String k = StringUtils.substringBefore(kv, ":");
			String v = StringUtils.substringAfter(kv, ":");
			nlRegions.put(k, v);
		}
		
		for (String kv : it.split(";")) {
			String k = StringUtils.substringBefore(kv, ":");
			String v = StringUtils.substringAfter(kv, ":");
			itRegions.put(k, v);
		}
		
		for (String kv : ca.split(";")) {
			String k = StringUtils.substringBefore(kv, ":");
			String v = StringUtils.substringAfter(kv, ":");
			caRegions.put(k, v);
		}
		
		for (String kv : us.split(";")) {
			String k = StringUtils.substringBefore(kv, ":");
			String v = StringUtils.substringAfter(kv, ":");
			usRegions.put(k, v);
		}
		
		regions.put("NL", nlRegions);
		regions.put("IT", itRegions);
		regions.put("CA", caRegions);
		regions.put("US", usRegions);
	}

	public static Map<String, Map<String, String>> getRegions() {
		return regions;
	}

	@Autowired
	protected CustomerAddressService addressService;
	
	@Autowired
	protected AddressCheckEngine addressCheckEngine;

	protected Redirect handleError(HttpServletRequest request, HttpServletResponse response, AddressForm addressForm,
			Messages messages) {
		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("messages", messages);
		flashMap.put("addressForm", addressForm);
		flashMap.put("checkout", 1);
		return new Redirect(request.getHeader("Referer"), null, Redirect.RAW);
	}

	protected Redirect handleSuccess(ModelMap model, CustomerAddress billingAddress, CustomerAddress shippingAddress) {

		ShopContext shopContext = ShopContextHolder.getInstance();
		shopContext.setShippingAddress(shippingAddress);
		shopContext.setBillingAddress(billingAddress);
		shopContext.getValidSteps().add(CheckoutStatusResolver.STEP_ADDRESS);
		
		Customer customer = CustomerHelper.getCustomer();
		Cart cart = CartHolder.getInstance(cartService, shopContext.getShopCode(), customer);
		cartService.calculate(shopContext, cart, customer);

		return new Redirect(checkoutStatusResolver.nextStepPageId(shopContext), null);
	}
	
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		AddressForm addressForm = (AddressForm) model.get("addressForm");
		if (addressForm == null) {
			addressForm = new AddressForm();
			model.put("addressForm", addressForm);
			initAddressForm(model, addressForm);
		}
	}
	
	protected void initAddressForm(ModelMap model, AddressForm addressForm) {
		Customer customer = CustomerHelper.getCustomer();

		ShopContext shopContext = ShopContextHolder.getInstance();

		if (shopContext.getBillingAddress() != null && shopContext.getShippingAddress() != null) {
			AddressFormAddress billingAddress = new AddressFormAddress(shopContext.getBillingAddress());
			addressForm.setBillingAddress(billingAddress);
			AddressFormAddress shippingAddress = new AddressFormAddress(shopContext.getShippingAddress());
			addressForm.setShippingAddress(shippingAddress);

			if (!shopContext.getBillingAddress().getId().equals(shopContext.getShippingAddress().getId())) {
				addressForm.setUseShipping(true);
			}
			return;
		}

		try {
			Map<String, CustomerAddress> addressMap = addressService.findDefaultsByCustomer(customer);
			if (addressMap.get(CustomerAddress.TAG_DEFAULT_BILLING_ADDRESS) != null) {
				AddressFormAddress billingAddress = new AddressFormAddress(
						addressMap.get(CustomerAddress.TAG_DEFAULT_BILLING_ADDRESS));
				addressForm.setBillingAddress(billingAddress);
			}
			if (addressMap.get(CustomerAddress.TAG_DEFAULT_SHIPPING_ADDRESS) != null) {
				AddressFormAddress shippingAddress = new AddressFormAddress(
						addressMap.get(CustomerAddress.TAG_DEFAULT_SHIPPING_ADDRESS));
				addressForm.setShippingAddress(shippingAddress);
			}
			
			if (addressMap.get(CustomerAddress.TAG_DEFAULT_SHIPPING_ADDRESS) != null && addressMap.get(CustomerAddress.TAG_DEFAULT_BILLING_ADDRESS) != null &&!addressMap.get(CustomerAddress.TAG_DEFAULT_BILLING_ADDRESS).getId()
					.equals(addressMap.get(CustomerAddress.TAG_DEFAULT_SHIPPING_ADDRESS).getId())) {
				addressForm.setUseShipping(true);
			}

		} catch (CustomerAdressServiceException e) {
			addressForm.setUseShipping(true);
		}
	}
	
	protected void handleActionRequestForward(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws ProcessableBoxException {
		if (request.getMethod().equals(RequestMethod.POST.toString())) {
				AddressForm addressForm = new AddressForm();
				BindingResult result = bind(addressForm, request);
				model.put("addressForm", addressForm);

				Messages messages = (Messages) model.get("messages");

				boolean isValid = true;

				for (ObjectError tempError : result.getAllErrors()) {
					if (tempError instanceof FieldError) {
						String name = ((FieldError) tempError).getField();

						if (name.startsWith("billing")) {
							isValid = false;
							messages.addFieldError(
									new FieldMessageImpl(name, Message.TYPE_ERROR, tempError.getDefaultMessage()));
						} else {
							if (addressForm.isUseShipping()) {
								isValid = false;
								messages.addFieldError(
										new FieldMessageImpl(name, Message.TYPE_ERROR, tempError.getDefaultMessage()));
							}
						}
					}
				}

				ShopCustomerAddress billingAddress = null;
				ShopCustomerAddress shippingAddress = null;
				billingAddress = new ShopCustomerAddress(addressForm.billingAddress);
				billingAddress.getTags().add(CustomerAddress.TAG_DEFAULT_BILLING_ADDRESS);
				if (!addressForm.isUseShipping()) {
					shippingAddress = billingAddress;
					shippingAddress.getTags().add(CustomerAddress.TAG_DEFAULT_SHIPPING_ADDRESS);
				} else {
					shippingAddress = new ShopCustomerAddress(addressForm.shippingAddress);
					shippingAddress.getTags().add(CustomerAddress.TAG_DEFAULT_SHIPPING_ADDRESS);
				}

				if (!addressCheckEngine.checkAddress(billingAddress, messages))
					isValid = false;
				if (!addressCheckEngine.checkAddress(shippingAddress, messages))
					isValid = false;

				Customer customer = CustomerHelper.getCustomer();
				shippingAddress.setCustomerId(customer.getId());
				billingAddress.setCustomerId(customer.getId());

				// TODO: call normalize

				if (isValid) {
					try {
						addressService.saveOrUpdate(shippingAddress);
						addressService.saveOrUpdate(billingAddress);						
						
						throw new RedirectException(handleSuccess(model, billingAddress, shippingAddress));
					} catch (RedirectException e) {
						throw e;
					} catch (Exception e) {
						log.error(ExceptionUtils.getStackTrace(e));
					}
				}

				throw new RedirectException(handleError(request, response, addressForm, messages));
				
				/*
				if (isValid) {
					addressService.saveOrUpdate(shippingAddress);
					addressService.saveOrUpdate(billingAddress);
					throw new RedirectException(handleSuccess(model, billingAddress, shippingAddress));
				} else {
					throw new RedirectException(handleError(request, response, addressForm, messages));
				}
				*/
		}
	}

	@Override
	public Integer getStep() {
		return CheckoutStatusResolver.STEP_ADDRESS;
	}

}
