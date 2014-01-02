package petrglad.millinames.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.requestfactory.shared.Receiver;
import petrglad.millinames.client.requestfactory.FullNameProxy;
import petrglad.millinames.client.requestfactory.NamesRequest;
import petrglad.millinames.client.requestfactory.NamesRequestFactory;

import java.util.List;

// TODO Use xml layouts wherever reasonable
public class TablePanel extends VerticalPanel {

    private int pageSize = 20;

    private final int LIST_SIZE = 1000000; // XXX Hardcode

    private NamesRequestFactory requests;

    final int rowHeight = 20;
    Grid dataPagePanel;
    ScrollPanel scroll;
    AbsolutePanel contentPanel;
    private int sortColumn = 0;

    public TablePanel(NamesRequestFactory requests) {
        this.requests = requests;
        final Grid header = new Grid(1, 2);
        header.setStyleName("tableHeader");
        header.setWidth("100%");
        header.setText(0, 0, "Name");
        header.setText(0, 1, "Last name");
        header.getColumnFormatter().setWidth(0, "50%");
        header.getColumnFormatter().setWidth(0, "50%");
        header.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sortColumn = header.getCellForEvent(event).getCellIndex();
                loadPage(scroll.getVerticalScrollPosition());
            }
        });

        add(header);

        scroll = new ScrollPanel();
        scroll.setHeight("400px");
        scroll.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(final ScrollEvent event) {
                loadPage(event.getRelativeElement().getScrollTop());
            }
        });
        contentPanel = new AbsolutePanel();
        contentPanel.setWidth("100%");
        dataPagePanel = new Grid(0, 2);
        dataPagePanel.getColumnFormatter().setWidth(0, "50%");
        dataPagePanel.getColumnFormatter().setWidth(1, "50%");
        dataPagePanel.setWidth("100%");
        contentPanel.add(dataPagePanel);
        scroll.add(contentPanel);
        add(scroll);
        loadPage(0);
    }

    public void loadPage(int scrollPosition) {
        final int totalHeight = rowHeight * LIST_SIZE;
        final int topRow = scrollPosition / rowHeight;
        contentPanel.setWidgetPosition(dataPagePanel, 0, scrollPosition);

        requests.getNamesRequest().getBatch(topRow, pageSize, sortColumn).fire(new Receiver<List<FullNameProxy>>() {
            @Override
            public void onSuccess(List<FullNameProxy> result) {
                dataPagePanel.setHeight((result.size() * rowHeight) + "px");
                dataPagePanel.resize(result.size(), 2);
                for (int i = 0; i < result.size(); i++) {
                    FullNameProxy name = result.get(i);
                    dataPagePanel.setText(i, 0, name.getFirstName());
                    dataPagePanel.setText(i, 1, name.getLastName());
                }
                contentPanel.setHeight(totalHeight + "px");
            }
        });
    }
}
