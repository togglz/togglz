package org.togglz.test;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;

public class WebAppDescriptorBuilder {

    private final WebAppDescriptor descriptor;

    public WebAppDescriptorBuilder() {
        descriptor = Descriptors.create(WebAppDescriptor.class);
    }

    public WebAppDescriptorBuilder contextParam(String name, String value) {
        descriptor.createContextParam()
            .paramName(name)
            .paramValue(value);
        return this;
    }

    public WebAppDescriptorBuilder filter(Class<?> clazz, String urlPattern) {
        descriptor.createFilter()
            .filterName(clazz.getSimpleName())
            .filterClass(clazz.getName())
            .up()
            .createFilterMapping()
            .filterName(clazz.getSimpleName())
            .urlPattern(urlPattern);
        return this;
    }

    public WebAppDescriptorBuilder servlet(Class<?> clazz, String urlPattern) {
        descriptor.createServlet()
            .servletName(clazz.getSimpleName())
            .servletClass(clazz.getName())
            .up()
            .createServletMapping()
            .servletName(clazz.getSimpleName())
            .urlPattern(urlPattern);
        return this;
    }

    public WebAppDescriptorBuilder listener(Class<?> clazz) {
        return listener(clazz.getName());
    }

    public WebAppDescriptorBuilder listener(String clazz) {
        descriptor.createListener()
            .listenerClass(clazz);
        return this;
    }

    public Asset exportAsAsset() {
        return new StringAsset(exportAsString());
    }

    public String exportAsString() {
        return descriptor.exportAsString();
    }

}
