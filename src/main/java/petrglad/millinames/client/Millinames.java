package petrglad.millinames.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;

import java.util.AbstractList;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Millinames implements EntryPoint {

    public static class Ll extends AbstractList<String[]> {

        @Override
        public String[] get(int index) {
            return new String[]{"Name" + index, "LastName" + index};
        }

        @Override
        public int size() {
            return 3013;
        }
    }

    List<String[]> LIST = new Ll();

//    /**
//     * The message displayed to the user when the server cannot be reached or
//     * returns an error.
//     */
//    private static final String SERVER_ERROR = "An error occurred while "
//            + "attempting to contact the server. Please check your network "
//            + "connection and try again.";
//
//    /**
//     * Create a remote service proxy to talk to the server-side Greeting service.
//     */
//    private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
//
//    private final Messages messages = GWT.create(Messages.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
//         grid = new DataGrid();
        CellTable<String[]> grid = new CellTable<String[]>();
        TextColumn<String[]> nameColumn = new TextColumn<String[]>() {
            @Override
            public String getValue(String[] record) {
                return record[0];
            }
        };
        grid.addColumn(nameColumn, "Name");

        TextColumn<String[]> lastNameColumn = new TextColumn<String[]>() {
            @Override
            public String getValue(String[] record) {
                return record[1];
            }
        };
        grid.addColumn(lastNameColumn, "Last name");
        //grid.setRowCount(LIST.size(), true);
        //grid.setRowData(0, LIST);
        grid.setAutoHeaderRefreshDisabled(true);
        grid.setPageSize(20);
//        grid.setPageStart(0);
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        SimplePager pager = new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(grid);

        final ListDataProvider<String[]> listData = new ListDataProvider<String[]>();
        listData.setList(LIST);
        listData.addDataDisplay(grid);

        Panel dataPanel = new VerticalPanel();
        dataPanel.add(grid);
        dataPanel.add(pager);

        TabPanel tabs = new TabPanel();
        tabs.add(new Button("Reinitialize data"), "Operations");
        tabs.add(dataPanel, "Data");
        tabs.selectTab(1);
//        RootPanel.get().add(new HTML());
        RootPanel.get("nameTable").add(tabs);

//        final Button sendButton = new Button(messages.sendButton());
//        final TextBox nameField = new TextBox();
//        nameField.setText(messages.nameField());
//        final Label errorLabel = new Label();
//
//        // We can add style names to widgets
//        sendButton.addStyleName("sendButton");
//
//        // Add the nameField and sendButton to the RootPanel
//        // Use RootPanel.get() to get the entire body element
//        RootPanel.get("nameFieldContainer").add(nameField);
//        RootPanel.get("sendButtonContainer").add(sendButton);
//        RootPanel.get("errorLabelContainer").add(errorLabel);
//
//        // Focus the cursor on the name field when the app loads
//        nameField.setFocus(true);
//        nameField.selectAll();
//
//        // Create the popup dialog box
//        final DialogBox dialogBox = new DialogBox();
//        dialogBox.setText("Remote Procedure Call");
//        dialogBox.setAnimationEnabled(true);
//        final Button closeButton = new Button("Close");
//        // We can set the id of a widget by accessing its Element
//        closeButton.getElement().setId("closeButton");
//        final Label textToServerLabel = new Label();
//        final HTML serverResponseLabel = new HTML();
//        VerticalPanel dialogVPanel = new VerticalPanel();
//        dialogVPanel.addStyleName("dialogVPanel");
//        dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
//        dialogVPanel.add(textToServerLabel);
//        dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
//        dialogVPanel.add(serverResponseLabel);
//        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
//        dialogVPanel.add(closeButton);
//        dialogBox.setWidget(dialogVPanel);
//
//        // Add a handler to close the DialogBox
//        closeButton.addClickHandler(new ClickHandler() {
//            public void onClick(ClickEvent event) {
//                dialogBox.hide();
//                sendButton.setEnabled(true);
//                sendButton.setFocus(true);
//            }
//        });
//
//        // Create a handler for the sendButton and nameField
//        class MyHandler implements ClickHandler, KeyUpHandler {
//            /**
//             * Fired when the user clicks on the sendButton.
//             */
//            public void onClick(ClickEvent event) {
//                sendNameToServer();
//            }
//
//            /**
//             * Fired when the user types in the nameField.
//             */
//            public void onKeyUp(KeyUpEvent event) {
//                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
//                    sendNameToServer();
//                }
//            }
//
//            /**
//             * Send the name from the nameField to the server and wait for a response.
//             */
//            private void sendNameToServer() {
//                // First, we validate the input.
//                errorLabel.setText("");
//                String textToServer = nameField.getText();
//                if (!FieldVerifier.isValidName(textToServer)) {
//                    errorLabel.setText("Please enter at least four characters");
//                    return;
//                }
//
//                // Then, we send the input to the server.
//                sendButton.setEnabled(false);
//                textToServerLabel.setText(textToServer);
//                serverResponseLabel.setText("");
//                greetingService.greetServer(new AsyncCallback<String>() {
//                    public void onFailure(Throwable caught) {
//                        // Show the RPC error message to the user
//                        dialogBox.setText("Remote Procedure Call - Failure");
//                        serverResponseLabel.addStyleName("serverResponseLabelError");
//                        serverResponseLabel.setHTML(SERVER_ERROR);
//                        dialogBox.center();
//                        closeButton.setFocus(true);
//                    }
//
//                    public void onSuccess(String result) {
//                        dialogBox.setText("Remote Procedure Call");
//                        serverResponseLabel.removeStyleName("serverResponseLabelError");
//                        serverResponseLabel.setHTML(result);
//                        dialogBox.center();
//                        closeButton.setFocus(true);
//                    }
//                });
//            }
//        }
//
//        // Add a handler to send the name to the server
//        MyHandler handler = new MyHandler();
//        sendButton.addClickHandler(handler);
//        nameField.addKeyUpHandler(handler);
    }
}
