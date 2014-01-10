package petrglad.millinames.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface NameService extends RemoteService {
    String regenerate();

    List<String[]> getBatch(int start, int count, int orderColumn, final boolean ascending);
}
