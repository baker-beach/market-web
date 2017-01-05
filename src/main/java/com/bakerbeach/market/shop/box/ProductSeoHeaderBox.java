package com.bakerbeach.market.shop.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

@Component
@Scope("prototype")
public class ProductSeoHeaderBox extends SeoHeaderBox {
	private static final long serialVersionUID = 1L;

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		
		super.handleRenderRequest(request, response, modelMap);
		
		/*
		ProductBox productBox = (ProductBox) modelMap.get("productBox");
		Helper helper = (Helper) modelMap.get("helper");
		
		if (productBox != null) {
			try {
				StringBuilder sb = new StringBuilder("Perfect Moment");
				
				GroupedProduct product = (GroupedProduct) productBox.getData().get("product");
				String name = product.getName();
				sb.append(" - ").append(name);
				
				List<String> categories = product.getCategories();
				if (categories != null && !categories.isEmpty()) {
					String category = categories.get(categories.size()-1);
					sb.append(" - ").append(helper.t(category));
				}
				
				sb.append(" - www.perfectmoment.com").toString();
				
				getData().put(TITLE, sb.toString());

				String description = product.getDescription1();
				if (description != null && !description.isEmpty()) {
					StringBuilder sb2 = new StringBuilder(name).append(" - ");
					sb2.append(StringUtils.abbreviate(description, 200));
					getData().put(DESCRIPTION, sb2.toString());
				}
				
				metaTags.add(new MetaTag("description", getDescription()));
				metaTags.add(new MetaTag("robots", "INDEX,FOLLOW"));
				
				return;
			} catch (Exception e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}			
		}

		// otherwise ---
		super.handleRenderRequest(request, response, modelMap, serviceHolder);
		
		*/
	}

}
