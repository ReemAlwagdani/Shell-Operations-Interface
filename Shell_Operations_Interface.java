
package test;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


public class Shell_Operations_Interface extends Application {

    /*
     create a file object to keep track of directory we are at
     that also keeps track of past directories so we can go back
     to a directory we created a file beacuase will use ProcessBuilder.
     directory() to set starting directory as home this method exepects
     a file as input
     */
    File directory = new File(System.getProperty("user.home"));

    // this variable will be used to store the chosen command as string so we can use it
    String selectedCommand;
    // chose to let it be let it be a class attribute so it can be accessed
    // by all event handler methods since we will have two

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Shell"); // set name

        /*
         we will use a Combobox data structer to store the available
         commands it is the best DS when using GUI since it allows
         user to pick one item from a drop down menu, exactly what
         we want. its used for selecting visually
         */
        // drop down menu of available commands :
        ComboBox<String> commands = new ComboBox<>();
        // to add into the comboBox
        commands.getItems().addAll(
                "ls", "pwd", "mkdir", "cd", "man", "touch", "cp", "mv",
                "rm", "rmdir", "cat", "less", "head", "grep", "wc",
                "chmod", "chown", "chgrp", "addUser", "addGroup",
                "ps", "quotacheck", "du", "gzip", "file", "find",
                "locate", "wget", "curl", "zip", "unzip", "bunzip2", "bzip2"
        );

        // show text in drop down menu :
        commands.setPromptText("Select a command");
        commands.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 14px; -fx-background-color: white;");
        // this line is just to change the font

        /*
         we will create a textField object that will be used
         to take parameters when needed
         the text feild will only show when needed
         so its visibilty will be set to false in the beggining
         */
        // used for taking paramteres when needed :
        TextField parameters = new TextField();
        parameters.setPromptText("Please Enter The Parameters (optional)");
        parameters.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 14px;"); // for font
        parameters.setVisible(true);
        // dont show unless this becomes true

        // text area used to show output :
        TextArea output = new TextArea();
        output.setEditable(false); // we dont want output to be editable
        output.setWrapText(true); // this allows text to complete down when theres not space
        output.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-control-inner-background: mistyrose;");
        // font

        // create the run button :
        Button run = new Button("Run");
        run.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 14px; -fx-background-color: white;");
        // font

        // this says run code inside when user selects command
        commands.setOnAction(new EventHandler<ActionEvent>() {//
            // we actually created the handler code block that checks for events

            @Override // this method is automatically added
            public void handle(ActionEvent event) { // it takes the action that just happened as parameter
                // get users commands choice : (will be stored in string so we can use it later)
                selectedCommand = commands.getValue();

                parameters.clear(); // to clear old input

                if (selectedCommand.equals("chmod")) {
                    parameters.setPromptText("This command will only work on files you own\n else you'll get an error");
                }

            }
        });

        /*
        Now we will create the second even handler thatll handle the steps
        after the user presses the "run" button
        depending on the selected command and the entered parameter
        the processbuilder will do its job accordingly
         */
        run.setOnAction(new EventHandler<ActionEvent>() {
            // this event handler code block includes things that will happen after the user presses the run button :
            @Override
            public void handle(ActionEvent event) {
                // first we will make sure that a command was picked
                if (selectedCommand == null || selectedCommand.isEmpty()) {
                    output.setText("Please select a command.");
                    return;
                }

                // now we will get the parameters if they were entered and store them in a variable
                String enteredParameter = parameters.getText().trim();

                /*
                if the command entered is cd , we need to change the directory
                we wont need the process builder
                just change the directory
                 */
                if (selectedCommand.equals("cd")) {
                    if (enteredParameter.equals("~")) { // go to home
                        directory = new File(System.getProperty("user.home"));
                    } else {
                        File newDirectory = new File(directory, enteredParameter);
                        if (newDirectory.exists() && newDirectory.isDirectory()) {
                            directory = newDirectory;
                        } else {
                            output.setText("Invalid directory: " + newDirectory.getAbsolutePath());
                            return;
                        }
                    }
                    output.setText("Changed directory to:\n" + directory.getAbsolutePath());
                    return;
                }

                /*

                chgrp, chown : wont work cuz you neeed to be running as root, or use sudo so it wont work
                less: wont work on gui cuz it needs interactive command line
                addUser , addGroup : need to use a command line tool in macos called dcls so not supported (also needs sudo or root)
                quotacheck : macos doesnt use quotachecks so its not possible

                 */
                if (selectedCommand.equals("chgrp") || selectedCommand.equals("chown") || selectedCommand.equals("addUser") || selectedCommand.equals("addGroup")) {
                    output.setText("Cannot run this command. \nNeed to be running as root or use sudo. \nThis GUI cannot run as root or use sudo");
                    return;

                } else if (selectedCommand.equals("less")) {
                    output.setText("This command cannot run on GUI. \nIt needs an interactive command line.\nuse 'head' or 'cat' instead");
                    return;

                } else if (selectedCommand.equals("quotacheck")) {
                    output.setText("MacOS does not have per-user quota system.");
                    return;

                } else if ((selectedCommand.equals("gzip") || selectedCommand.equals("bunzip2") || selectedCommand.equals("cat") // these commands need a parameter or will crash/lag                      || selectedCommand.equals("chgrp") || selectedCommand.equals("head") || selectedCommand.equals("bunzip2")
                        || selectedCommand.equals("bzip2")|| selectedCommand.equals("head")) && enteredParameter.equals("")) {
                    output.setText("This command needs a parameter.\nPlease enter one and run again.");
                    return;

                }

                // Build and execute other shell commands
                try {
                    // this line is used to combine the selected command and paramter if there is one
                    String commandANDparameter = selectedCommand + (enteredParameter.isEmpty() ? "" : " " + enteredParameter);
                    // create the process builder 
                    ProcessBuilder commandExecuter = new ProcessBuilder("bash", "-c", commandANDparameter);
                    commandExecuter.directory(directory);

                    Process currentProcess = commandExecuter.start();

                    BufferedReader resultReader = new BufferedReader( // reads output
                            new InputStreamReader(currentProcess.getInputStream())
                    );
                    BufferedReader errorReader = new BufferedReader( // reads error messages
                            new InputStreamReader(currentProcess.getErrorStream())
                    );

                    StringBuilder result = new StringBuilder();
                    StringBuilder errorMessage = new StringBuilder();
                    String line;

                    while ((line = resultReader.readLine()) != null) { // while there is still an output read it and save it to line
                        result.append(line).append("\n");
                    }
                    while ((line = errorReader.readLine()) != null) { // while there is still error text read it and save it to line
                        errorMessage.append(line).append("\n");
                    }

                    int exitCode = currentProcess.waitFor(); // code of process, 1 is good other is not
                    if (exitCode == 0) {
                        output.setText(result.length() > 0 ? result.toString() : "Command executed successfully."); // print done if command is executed 
                    } else { // theres an error :

                        /*
                        chmod : only works on files you own
                        ps : will work with parameters. but alone will give an error because there isnt an open terminal (exit code 1)
                        gzip : works but needs to be installed
                        wget : needs to be installed use curl as alternative
                        */

                        // error outputs i want to specify :
                        if (selectedCommand.equals("ps") && enteredParameter.equals("")) {
                            output.setText("Error (exit code " + exitCode + "):\n" + errorMessage.toString() + "\nNo real terminal session availabe.");
                            return;

                        }
                        if (selectedCommand.equals("chmod")) {
                            output.setText("Error (exit code " + exitCode + "):\n" + errorMessage.toString() + "\nThis command will only work on files you own.");
                            return;

                        }
                        if (selectedCommand.equals("gzip")) {
                            output.setText("Error (exit code " + exitCode + "):\n" + errorMessage.toString() + "\nThis command needs to be installed"
                                    + "\nother wise use one of the alternatives found in command list"
                                    + "\nbzip2 or bunzip2"
                                    + "\nzip or unzip");
                            return;

                        }
                        if (selectedCommand.equals("wget")) {
                            output.setText("Error (exit code " + exitCode + "):\n" + errorMessage.toString() + "\nThis command needs to be installed"
                                    + "\nother wise use 'curl' alternative found in command list");
                            return;

                        }

                        output.setText("Error (exit code " + exitCode + "):\n" + errorMessage.toString()); // the default error messsage
                    }
                } catch (Exception e) {
                    output.setText("Failed to run command:\n" + e.getMessage());
                }


            }
        });

        /*
        Now we weve created the buttons we now want to create the actualt
        window and place that buttons
        for that we will use something called a layout container
        there are many types we chose VBox as it is the simplest
         */
        VBox window = new VBox(15, commands, parameters, run, output); // set spacing
        window.setPadding(new Insets(20)); // adds space arounf the buttons and boxes so that theyre not stuck to the ege
        window.setStyle("-fx-background-color: pink;"); // color

        Scene scene = new Scene(window, 600, 400); // window size
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
