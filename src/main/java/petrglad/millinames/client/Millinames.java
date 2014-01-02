package petrglad.millinames.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.requestfactory.shared.Receiver;
import petrglad.millinames.client.requestfactory.FullNameProxy;
import petrglad.millinames.client.requestfactory.NamesRequest;
import petrglad.millinames.client.requestfactory.NamesRequestFactory;

import java.util.List;

public class Millinames implements EntryPoint {

    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final NameServiceAsync greetingService = GWT.create(NameService.class);
    private NamesRequestFactory requestFactory;

    /**
     * This is the entry point method.
     */
    // TODO Use xml layouts wherever reasonable
    public void onModuleLoad() {
        initRequestFactory();

        Widget dataPanel = new TablePanel(requestFactory);
        final Button regenerateButton = new Button("Reinitialize data");
        regenerateButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                regenerateButton.setEnabled(false);
                greetingService.regenerate(new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        showDialog("Error regenerating data.");
                        regenerateButton.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(String result) {
                        showDialog("Regeneration finished.\n" + result);
                        regenerateButton.setEnabled(true);
                    }
                });
            }
        });

        TabPanel tabs = new TabPanel();
        tabs.add(regenerateButton, "Operations");
        tabs.add(dataPanel, "Data");
        tabs.selectTab(0);
        tabs.setWidth("250px");
        RootPanel.get("nameTable").add(tabs);
    }

    private void initRequestFactory() {
        final EventBus eventBus = new SimpleEventBus();
        requestFactory = GWT.create(NamesRequestFactory.class);
        requestFactory.initialize(eventBus);
    }

    public void showDialog(String message) {
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(message);
        final Button closeButton = new Button("Close");
        dialogBox.add(closeButton);
        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        dialogBox.show();
    }
}
