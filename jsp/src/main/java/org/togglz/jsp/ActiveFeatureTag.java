package org.togglz.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.LazyResolvingFeatureManager;
import org.togglz.core.util.NamedFeature;
import org.togglz.core.util.Strings;

public class ActiveFeatureTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    protected FeatureManager featureManager;

    protected String name;

    public ActiveFeatureTag() {
        this.featureManager = new LazyResolvingFeatureManager();
    }

    @Override
    public int doStartTag() throws JspException {
        if (isFeatureActive()) {
            return Tag.EVAL_BODY_INCLUDE;
        }
        return Tag.SKIP_BODY;
    }

    protected boolean isFeatureActive() {
        if (Strings.isNotBlank(name)) {
            return featureManager.isActive(new NamedFeature(name));
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public void setFeatureManager(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

}
