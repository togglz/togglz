package org.togglz.servlet.activation;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;
import org.togglz.servlet.util.HttpServletRequestHolder;

/**
 * Activation strategy that will use the IP address of the client to decide if a feature is active or not.
 * 
 * @author Christian Kaltepoth
 */
public class ClientIpActivationStrategy implements ActivationStrategy
{

   public static final String ID = "client-ip";

   public static final String PARAM_IPS = "ips";

   @Override
   public String getId()
   {
      return ID;
   }

   @Override
   public String getName()
   {
      return "IP address (client)";
   }

   @Override
   public boolean isActive(FeatureState featureState, FeatureUser user)
   {

      HttpServletRequest request = HttpServletRequestHolder.get();
      if (request != null) {

         String allowedIpsParam = featureState.getParameter(PARAM_IPS);
         List<String> allowsIps = Strings.splitAndTrim(allowedIpsParam, "[\\s,]+");

         // TODO: This should support a simple form of subnet matching
         return allowsIps.contains(request.getRemoteAddr());

      }

      return false;

   }

   @Override
   public Parameter[] getParameters()
   {
      return new Parameter[] {
               ParameterBuilder.create(PARAM_IPS).label("Client IPs")
                        .description("A comma-separated list of client IPs for which the feature should be active.")
      };
   }

}
