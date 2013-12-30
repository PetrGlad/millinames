package petrglad.millinames.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class Millinames implements EntryPoint {

    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

//    /**
//     * The message displayed to the user when the server cannot be reached or
//     * returns an error.
//     */
//    private static final String SERVER_ERROR = "An error occurred while "
//            + "attempting to contact the server. Please check your network "
//            + "connection and try again.";
//


//    private final Messages messages = GWT.create(Messages.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        Widget dataPanel = new TablePanel(greetingService);

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
                        showDialog("Regeneration completed.");
                        regenerateButton.setEnabled(true);
                    }
                });
            }
        });

        TabPanel tabs = new TabPanel();
        tabs.add(regenerateButton, "Operations");
        tabs.add(dataPanel, "Data");
        tabs.selectTab(0);
        tabs.setWidth("30%");
        RootPanel.get("nameTable").add(tabs);
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
