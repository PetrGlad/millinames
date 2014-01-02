package petrglad.millinames.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import petrglad.millinames.client.requestfactory.NamesRequestFactory;

public class Millinames implements EntryPoint {

    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final NameServiceAsync greetingService = GWT.create(NameService.class);
    private NamesRequestFactory requestFactory;
    private Label messageBox;

    /**
     * This is the entry point method.
     */
    // TODO Use xml layouts wherever reasonable
    public void onModuleLoad() {
        initRequestFactory();
        final TabPanel tabs = new TabPanel();
        tabs.add(makeRegenerateButton(), "Operations");
        tabs.add(new TablePanel(requestFactory, 1000000), "Data");
        tabs.selectTab(0);
        tabs.setWidth("250px");

        VerticalPanel vp = new VerticalPanel();
        vp.add(tabs);
        messageBox = new Label();
        messageBox.setStyleName("notification");
        vp.add(messageBox);
        RootPanel.get("nameTable").add(vp);
    }

    private Button makeRegenerateButton() {
        final Button regenerateButton = new Button("Reinitialize data");
        regenerateButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                regenerateButton.setEnabled(false);
                showNotification("Regenerating data.");
                greetingService.regenerate(new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        showNotification("Error regenerating data.");
                        regenerateButton.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(String result) {
                        showNotification(result);
                        regenerateButton.setEnabled(true);
                    }
                });
            }
        });
        return regenerateButton;
    }

    private void initRequestFactory() {
        final EventBus eventBus = new SimpleEventBus();
        requestFactory = GWT.create(NamesRequestFactory.class);
        requestFactory.initialize(eventBus);
    }

    public void showNotification(String message) {
        this.messageBox.setText(message);
    }
}
