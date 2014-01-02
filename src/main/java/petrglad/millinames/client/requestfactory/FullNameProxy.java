package petrglad.millinames.client.requestfactory;


import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.ValueProxy;
import petrglad.millinames.server.requestfactory.NameLocator;
import petrglad.millinames.shared.FullName;

@ProxyFor(value = FullName.class, locator = NameLocator.class)
public interface FullNameProxy extends ValueProxy {
    String getFirstName();
    String getLastName();
}
