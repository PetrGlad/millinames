package petrglad.millinames.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.requestfactory.shared.Receiver;
import petrglad.millinames.client.requestfactory.FullNameProxy;
import petrglad.millinames.client.requestfactory.NamesRequestFactory;

import java.util.List;

import static petrglad.millinames.shared.Constants.NAME_COLUMN_COUNT;

// TODO Use xml layouts wherever reasonable
public class TablePanel extends VerticalPanel {

    private final int rowHeight = 20;
    private int pageSize = 20;
    private int listSize;
    private NamesRequestFactory requests;
    private Grid dataPagePanel;
    private ScrollPanel scroll;
    private AbsolutePanel contentPanel;
    private int sortColumn = 0;

    public TablePanel(NamesRequestFactory requests, int listSize) {
        this.requests = requests;
        this.listSize = listSize;
        add(makeHeader());

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
        dataPagePanel = new Grid(0, NAME_COLUMN_COUNT);
        dataPagePanel.setWidth("100%");
        setupColumns(dataPagePanel);
        contentPanel.add(dataPagePanel);
        scroll.add(contentPanel);
        add(scroll);
        loadPage(0);
    }

    private Grid makeHeader() {
        final Grid header = new Grid(1, NAME_COLUMN_COUNT);
        header.setStyleName("tableHeader");
        header.setText(0, 0, "Name");
        header.setText(0, 1, "Last name");
        header.setWidth("95%");// XXX (layout) Since scrollpanel has scrollbar columns of header are not be aligned with data columns exactly
        setupColumns(header);
        header.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sortColumn = header.getCellForEvent(event).getCellIndex();
                loadPage(scroll.getVerticalScrollPosition());
            }
        });
        return header;
    }

    private void setupColumns(Grid nameGrid) {
        nameGrid.getColumnFormatter().setWidth(0, "50%");
        nameGrid.getColumnFormatter().setWidth(1, "50%");
    }

    public void loadPage(int scrollPosition) {
        final int totalHeight = rowHeight * listSize;
        final int topRow = scrollPosition / rowHeight;
        contentPanel.setWidgetPosition(dataPagePanel, 0, scrollPosition);
        requests.getNamesRequest().getBatch(topRow, pageSize, sortColumn).fire(new Receiver<List<FullNameProxy>>() {
            @Override
            public void onSuccess(List<FullNameProxy> result) {
                dataPagePanel.setHeight((result.size() * rowHeight) + "px");
                dataPagePanel.resize(result.size(), NAME_COLUMN_COUNT);
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
