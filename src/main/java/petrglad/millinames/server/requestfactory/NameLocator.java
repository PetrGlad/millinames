package petrglad.millinames.server.requestfactory;

import com.google.web.bindery.requestfactory.shared.Locator;
import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petrglad.millinames.client.NameService;
import petrglad.millinames.shared.FullName;

import java.util.ArrayList;
import java.util.List;

public class NameLocator extends Locator<FullName, String> {
    private static final Logger LOG = LoggerFactory.getLogger(NameLocator.class);

    private static NameService service;

    // TODO Subclass ServiceLayer to initialize this properly.
    public static void setService(NameService service) {
        Assert.assertNull("Service is already attached.", NameLocator.service);
        NameLocator.service = service;
    }

    public static List<FullName> getBatch(int start, int count, int orderColumn) {
        Assert.assertNotNull("Service is not attached.", service);
        final long t = System.nanoTime();
        List<FullName> result = new ArrayList<FullName>();
        for (String[] strings : service.getBatch(start, count, orderColumn))
            result.add(new FullName(strings[0], strings[1]));
        LOG.debug("Batch query time {} uSec.", (System.nanoTime() - t) / 1000);
        return result;
    }

    @Override
    public FullName create(Class<? extends FullName> clazz) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public FullName find(Class<? extends FullName> clazz, String id) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Class<FullName> getDomainType() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getId(FullName domainObject) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Class<String> getIdType() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Object getVersion(FullName domainObject) {
        throw new RuntimeException("Not implemented");
    }
}
