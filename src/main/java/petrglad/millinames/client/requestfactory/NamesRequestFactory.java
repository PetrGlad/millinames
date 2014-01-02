package petrglad.millinames.client.requestfactory;

import com.google.web.bindery.requestfactory.shared.RequestFactory;

public interface NamesRequestFactory extends RequestFactory {
    NamesRequest getNamesRequest();
}
