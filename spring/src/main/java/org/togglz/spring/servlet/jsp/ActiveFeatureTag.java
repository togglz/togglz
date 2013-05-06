package org.togglz.spring.servlet.jsp;

import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.context.WebApplicationContext;
import org.togglz.core.util.ConstructorBasedActiveFeatureMap;

@SuppressWarnings("serial")
public class ActiveFeatureTag extends TagSupport {

	private String name;

	@Override
	public int doStartTag() throws JspException {

		if (name != null) {
			Map<String, Boolean> activeFeatures = activeFeatures();
			if (activeFeatures.get(name)) {
				return Tag.EVAL_BODY_INCLUDE;
			}
		}
		return Tag.SKIP_BODY;

	}

	private Map<String, Boolean> activeFeatures() {
		WebApplicationContext context = getWebApplicationContext(pageContext
				.getServletContext());
		return context.getBean(ConstructorBasedActiveFeatureMap.class);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
