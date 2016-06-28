package org.togglz.servlet.activation;

import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;
import org.togglz.servlet.util.HttpServletRequestHolder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Activation strategy that will use the IP address of the client to decide if a feature is active or not.
 * 
 * @author Christian Kaltepoth
 */
public class ClientIpActivationStrategy implements ActivationStrategy
{
   private final Log log = LogFactory.getLog(ClientIpActivationStrategy.class);

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

         List<String> parts = Strings.splitAndTrim(featureState.getParameter(PARAM_IPS), "[\\s,]+");

         List<InetAddress> ips = new ArrayList<>();
         List<CIDRUtils> cidrUtils = new ArrayList<>();
         for (String part : parts) {
            try {
               if (part.contains("/")) {
                  cidrUtils.add(new CIDRUtils(part));
               } else {
                  ips.add(InetAddress.getByName(part));
               }
            } catch (Exception e) {
               log.warn("Ignoring illegal IP address or CIDR range " + part);
            }
         }

         try {
            if (ips.contains(InetAddress.getByName(request.getRemoteAddr()))) {
               return true;
            }
   
            for (CIDRUtils cidrUtil : cidrUtils) {
               if (cidrUtil.isInRange(request.getRemoteAddr())) {
                  return true;
               }
            }
         } catch (UnknownHostException e) {
            log.error("Illegal address " + request.getRemoteAddr());
         }
      }

      return false;
   }

   @Override
   public Parameter[] getParameters()
   {
      return new Parameter[] {
               ParameterBuilder.create(PARAM_IPS).label("Client IPs")
                        .description("A comma-separated list of client IPs or address ranges in CIDR notation (e.g. 10.1.2.0/24) for which the feature should be active.")
      };
   }

}
