package org.togglz.spring.boot.autoconfigure;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.boot.actuate.endpoint.mvc.HypermediaDisabled;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
public class TogglesMvcEnpoint extends EndpointMvcAdapter {

    private final TogglesEndpoint delegate;

    @Inject
    public TogglesMvcEnpoint(TogglesEndpoint delegate) {
        super(delegate);
        this.delegate = delegate;
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
