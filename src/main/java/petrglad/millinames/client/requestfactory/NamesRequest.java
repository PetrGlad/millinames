package petrglad.millinames.client.requestfactory;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import petrglad.millinames.server.requestfactory.NameLocator;

import java.util.List;

@Service(NameLocator.class)
public interface NamesRequest extends RequestContext {
    Request<List<FullNameProxy>> getBatch(final int start, final int count,
                                          final int orderColumn, final boolean ascending);
}
