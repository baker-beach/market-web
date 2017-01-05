package com.bakerbeach.market.shop.box;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.bakerbeach.market.cms.box.AbstractBox;
import com.bakerbeach.market.cms.box.ProcessableBox;
import com.bakerbeach.market.shop.service.ShopContextHolder;

@Component("com.bakerbeach.market.shop.box.SeoHeaderBox")
@Scope("prototype")
public class SeoHeaderBox extends AbstractBox implements ProcessableBox {
	private static final long serialVersionUID = 1L;

	protected static final String TITLE = "page_title";
	protected static final String DESCRIPTION = "page_description";

	protected static final String INDEX = "page_index";
	protected static final String FOLLOW = "page_follow";

	protected List<MetaTag> metaTags = new ArrayList<>();
	protected List<LinkTag> linkTags = new ArrayList<>();
	

	public Map<String, Object> getRequestData() {
		return ShopContextHolder.getInstance().getRequestData();
	}
	
	private String getData(String key) {
		Map<String, Object> requestData = getRequestData();
		if (requestData.containsKey(key)) {
			return (String) requestData.get(key);
		} else {
			return (String) getData().get(key);			
		}
	}
		
	public String getTitle() {
		return getData(TITLE);
	}

	public String getDescription() {
		return getData(DESCRIPTION);
	}

	public String getIndex() {
		return getData(INDEX);
	}

	public String getFollow() {
		return getData(FOLLOW);
	}
	
	protected LinkTag getCanonical(HttpServletRequest request) {

		String requestUrl = request.getRequestURL().toString();
		
		// TODO: check for alternative name in config.
		String serverName = request.getServerName();

		// TODO: heck for configured protocol.
		String protocol = "https";
		if (!request.isSecure()) {
			protocol = "http";			
		}
		
		Integer port = request.getLocalPort();
		if (port == 443 || port == 80) {
			port = null;
		}

//		String contextPath = request.getContextPath();
		String uri = request.getRequestURI();		
//		if (!contextPath.isEmpty()) {
//			uri = uri.replaceFirst(contextPath, "");
//		}
		
		StringBuilder href = new StringBuilder();
		href.append(protocol).append("://");
		href.append(serverName);
		if (port != null) {
			href.append(":").append(port);
		}
		href.append(uri);
		
		if (request.getParameterMap().isEmpty() && href.toString().equals(requestUrl)) {
			return null;
		} else {
			return new LinkTag(null, "canonical", href.toString());
		}	
	}



	public Tag getTitleTag() {
		return new Tag("title", getTitle());
	}
	
	public List<MetaTag> getMetaTags() {
		return metaTags;
	}

	public List<LinkTag> getLinkTags() {
		return linkTags;
	}

	@Override
	public void handleActionRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {		
	}

	@Override
	public void handleRenderRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
		metaTags.add(new MetaTag("description", getDescription()));
		metaTags.add(new MetaTag("robots", String.format("%s,%s", getIndex(), getFollow())));
		
		LinkTag canonical = getCanonical(request);
		if (canonical != null) {
			linkTags.add(canonical);			
		}

	}

	@Override
	public String toString() {
		return "";
	}

	public class Tag {
		String name;
		String content;
		
		Tag(String name, String content){
			this.name = name;
			this.content = content;
		}
		
		@Override public String toString() {
			return String.format("<%s>%s</%s>", name, content, name);
		}
	}
	
	public class TitleTag {
		String content;

		TitleTag(String content){
			this.content = content;
		}

		@Override public String toString() {
			return String.format("<title>%s</title>", content);
		}
	}

	public class MetaTag {
		String name;
		String content;
		
		MetaTag(String name, String content){
			this.name = name;
			this.content = content;
		}
		
		@Override public String toString() {
			return String.format("<meta name=\"%s\" content=\"%s\">", name, content);
		}
	}
	
	public class LinkTag {
		String hreflang;
		String rel;
		String href;

		LinkTag(String hreflang, String rel, String href){
			this.hreflang = hreflang;
			this.rel = rel;
			this.href = href;
		}

		@Override 
		public String toString() {
			if (hreflang != null) {
				return String.format("<link hreflang=\"%s\" rel=\"%s\" href=\"%s\">", hreflang, rel, href);
			} else {
				return String.format("<link rel=\"%s\" href=\"%s\">", rel, href);
			}
		}
	}
}
