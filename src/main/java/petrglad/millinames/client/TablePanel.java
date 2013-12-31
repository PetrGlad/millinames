package petrglad.millinames.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class TablePanel extends VerticalPanel {

    private int pageSize = 20;

    private int LIST_SIZE = 1000000; // FIXME Hardcode

    private GreetingServiceAsync source;

    final int rowHeight = 20;
    VerticalPanel dataPagePanel;
    ScrollPanel scroll;
    AbsolutePanel contentPanel;
    private boolean sortOrder;

    public TablePanel(GreetingServiceAsync rows) {
        this.source = rows;
        HorizontalPanel header = new HorizontalPanel();
        Label nameHeader = new Label("Name");
        header.add(nameHeader);
        Label lastNameHeader = new Label("Last name");
        header.add(lastNameHeader);
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
        dataPagePanel = new VerticalPanel();
        contentPanel.add(dataPagePanel);
        scroll.add(contentPanel);
        add(scroll);
        loadPage(0);

        nameHeader.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sortOrder = true;
                loadPage(scroll.getVerticalScrollPosition());
            }
        });
        lastNameHeader.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sortOrder = false;
                loadPage(scroll.getVerticalScrollPosition());
            }
        });
    }

    public void loadPage(int scrollPosition) {
        final int totalHeight = rowHeight * LIST_SIZE;
        final int topRow = scrollPosition / rowHeight;
        contentPanel.setWidgetPosition(dataPagePanel, 0, scrollPosition);
        source.getBatch(topRow, pageSize, sortOrder, new AsyncCallback<List<String[]>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(List<String[]> result) {
                dataPagePanel.setHeight((result.size() * rowHeight) + "px");
                while (dataPagePanel.getWidgetCount() > 0)
                    dataPagePanel.remove(0);
                for (String[] row : result)
                    dataPagePanel.add(formatRow(row));
                contentPanel.setHeight(totalHeight + "px");
            }
        });
    }

    public Widget formatRow(String[] columns) {
        Label html = new Label(columns[0] + " " + columns[1]);
        html.setHeight(rowHeight + "px");
        return html;
    }
}
