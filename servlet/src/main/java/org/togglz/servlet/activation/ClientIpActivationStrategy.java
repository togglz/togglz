package org.togglz.servlet.activation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.activation.Parameter;
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
   private final Logger log = LoggerFactory.getLogger(ClientIpActivationStrategy.class);

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

            try {
                String remoteAddr = request.getHeader("X-Forwarded-For");
                if (Strings.isBlank(remoteAddr)) {
                    remoteAddr = request.getRemoteAddr();
                }
                InetAddress remoteInetAddress = InetAddress.getByName(remoteAddr);
                for (String part : parts) {
                    if (part.equals(remoteAddr)) { // shortcut
                        return true;
                    }

                    if (part.contains("/")) {
                        CIDRUtils cidrUtil = new CIDRUtils(part);
                        if (cidrUtil.isInRange(remoteInetAddress)) {
                            return true;
                        }
                    } else if (remoteInetAddress.equals(InetAddress.getByName(part))) {
                        return true;
                    }
                }
            } catch (UnknownHostException | IllegalArgumentException e) {
                log.warn("Ignoring illegal IP address or CIDR range ");
            }
        }

        return false;
    }

   @Override
   public Parameter[] getParameters()
   {
      return new Parameter[] { new AddressParameter() };
   }

   protected static class AddressParameter implements Parameter {

      @Override
      public String getName() {
         return PARAM_IPS;
      }

      @Override
      public String getLabel() {
         return "Client IPs";
      }

      @Override
      public String getDescription() {
         return "A comma-separated list of client IPs or address ranges in CIDR notation (e.g. 10.1.2.0/24) for which the feature should be active.";
      }

      @Override
      public boolean isOptional() {
         return false;
      }

      @Override
      public boolean isLargeText() {
         return false;
      }

      @Override
      public boolean isValid(String addresses) {
         if (Strings.isBlank(addresses)) {
            return false;
         }

         List<String> addressList = Strings.splitAndTrim(addresses, ",");

         for(String address : addressList) {
             if (address.contains("/")) {
                 try {
                    new CIDRUtils(address);
                 } catch (UnknownHostException | IllegalArgumentException e) {
                    return false;
                 }
              } else {
                 try {
                    InetAddress.getByName(address);
                 } catch (UnknownHostException e) {
                    return false;
                 }
              }
         }

         return true;
      }

   }

}
