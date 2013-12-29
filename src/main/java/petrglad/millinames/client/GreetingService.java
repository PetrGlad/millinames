package petrglad.millinames.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
    String regenerate();

    // TODO This is slow: prototype stub
    List<String[]> getBatch(int start, int count, boolean orderByFirst);
}
