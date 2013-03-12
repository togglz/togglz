package org.togglz.ws.client;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.context.ThreadLocalFeatureManagerProvider;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.test.Deployments;
import org.togglz.ws.server.ContextTogglzWebService;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WebServiceStateRepositoryIT {

    private static final String SERVICE_NAME = "TogglzWebService";

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor webXml = Descriptors.create(WebAppDescriptor.class);
        
        return ShrinkWrap.create(WebArchive.class, "togglz-ws.war")
//                .addPackage(ContextTogglzWebService.class.getPackage())
                .addAsLibraries(Deployments.getTogglzCoreArchive(), Deployments.getTogglzSpringArchive(), getTogglzWsArchive())
                .setWebXML(
                        new StringAsset(webXml
                        .createServlet()
                        .servletName(SERVICE_NAME).servletClass(ContextTogglzWebService.class.getName()).up()
                        .createServletMapping().servletName(SERVICE_NAME).urlPattern("/" + SERVICE_NAME).up()
                        .exportAsString())
                );
    }

    public static JavaArchive getTogglzWsArchive() {
        return ShrinkWrap.create(ExplodedImporter.class, "togglz-ws.jar")
                .importDirectory("target/classes")
                .as(JavaArchive.class);
    }    
    
    @ArquillianResource
    private URL url;

    @Test
    public void canRetrieveFeaturesFromWS() throws IOException {
        assertTrue(url != null);        
        System.out.println("url : "+url);
        StateRepository stateRepository = new WebServiceStateRepository(url+"/ws");
        ThreadLocalFeatureManagerProvider.bind(new FeatureManagerBuilder().togglzConfig(new ClientConfiguration()).stateRepository(stateRepository).build());
        System.out.println("Client features:");
        for (Feature f : FeatureContext.getFeatureManager().getFeatures()) {
            System.out.println(f.name() + " is" + (!f.isActive() ? " not" : "") + " active");
        }
    }
    
}
