package petrglad.millinames.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class TablePanel extends VerticalPanel {

    private int pageSize = 20;

    private final int LIST_SIZE = 1000000; // XXX Hardcode

    private GreetingServiceAsync source;

    final int rowHeight = 20;
    Grid dataPagePanel;
    ScrollPanel scroll;
    AbsolutePanel contentPanel;
    private boolean sortOrder;

    public TablePanel(GreetingServiceAsync rows) {
        this.source = rows;
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
                int cellIndex = header.getCellForEvent(event).getCellIndex();
                sortOrder = cellIndex == 0;
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
        source.getBatch(topRow, pageSize, sortOrder, new AsyncCallback<List<String[]>>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO Show notification
            }

            @Override
            public void onSuccess(List<String[]> result) {
                dataPagePanel.setHeight((result.size() * rowHeight) + "px");
                dataPagePanel.resize(result.size(), 2);
                for (int i = 0; i < result.size(); i++) {
                    String[] row = result.get(i);
                    dataPagePanel.setText(i, 0, row[0]);
                    dataPagePanel.setText(i, 1, row[1]);
                }
                contentPanel.setHeight(totalHeight + "px");
            }
        });
    }
}
