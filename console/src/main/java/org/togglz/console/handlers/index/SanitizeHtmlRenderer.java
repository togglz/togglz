package org.togglz.console.handlers.index;

import java.util.Locale;
import java.util.Map;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

public class SanitizeHtmlRenderer implements NamedRenderer {

    @Override
    public RenderFormatInfo getFormatInfo() {
        return null;
    }

    @Override
    public String getName() {
        return "sanitizeHtml";
    }

    @Override
    public Class<?>[] getSupportedClasses() {
        return new Class<?>[] { String.class };
    }

    @Override
    public String render(Object o, String format, Locale locale, Map<String, Object> model) {
        if (o instanceof String) {
            String html = (String) o;

            PolicyFactory policy = new HtmlPolicyBuilder()
                    .allowElements("a")
                    .allowUrlProtocols("https")
                    .allowAttributes("href").onElements("a")
                    .requireRelNofollowOnLinks()
                    .toFactory();

            return policy.sanitize(html);
        }
        return null;
    }
}