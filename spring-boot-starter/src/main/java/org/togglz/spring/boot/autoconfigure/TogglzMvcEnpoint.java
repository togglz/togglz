package org.togglz.spring.boot.autoconfigure;

import java.util.Map;

import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.boot.actuate.endpoint.mvc.HypermediaDisabled;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

public class TogglzMvcEndpoint extends EndpointMvcAdapter {

    private final TogglzEndpoint delegate;

    public TogglzMvcEndpoint(TogglzEndpoint delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @ActuatorGetMapping("/{name:.*}")
    @ResponseBody
    @HypermediaDisabled
    public Object get(@PathVariable String name) {
        if (!this.delegate.isEnabled()) {
            // Shouldn't happen - MVC endpoint shouldn't be registered when delegate's
            // disabled
            return getDisabledResponse();
        }
        TogglzEndpoint.TogglzFeature feature = this.delegate.get(name);
        return feature == null ? ResponseEntity.notFound().build() : feature;
    }

    @ActuatorPostMapping("/{name:.*}")
    @ResponseBody
    @HypermediaDisabled
    public Object set(@PathVariable String name,
            @RequestBody Map<String, String> configuration) {
        if (!this.delegate.isEnabled()) {
            // Shouldn't happen - MVC endpoint shouldn't be registered when delegate's disabled
            return getDisabledResponse();
        }
        delegate.enable(name, "true".equals(configuration.get("enabled")));
        return HttpEntity.EMPTY;
    }

}
