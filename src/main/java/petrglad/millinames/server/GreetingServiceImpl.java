package petrglad.millinames.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import petrglad.millinames.client.GreetingService;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

    public String greetServer() throws IllegalArgumentException {
        // FieldVerifier.isValidName(input);
        String serverInfo = getServletContext().getServerInfo();
        return "I run " + serverInfo;
    }
}
