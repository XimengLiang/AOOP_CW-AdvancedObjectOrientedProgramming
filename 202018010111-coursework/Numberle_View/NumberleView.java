package Numberle_View;

import Numberle_Controller.NumberleController;

import Numberle_Model.INumberleModel;
import Numberle_Model.NumberleModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

public class NumberleView implements Observer {
    private INumberleModel model;
    private NumberleController controller;
    private final JFrame frame = new JFrame("Numberle");
    private final StringBuilder input = new StringBuilder();
    private final JTextField[][] fields = new JTextField[INumberleModel.MAX_ATTEMPTS][7];

    private int lineNumber;
    private int currentPosition = 0;
    private HashMap<String, JButton> buttonMap = new HashMap<>();
    private boolean lastFlag1 = false;
    private boolean lastFlag2 = false;
    private boolean lastFlag3 = false;

    public NumberleView(INumberleModel model, NumberleController controller) {
        this.model = model;
        this.controller = controller;
        controller.startNewGame();
        ((NumberleModel) this.model).addObserver(this);
        this.controller.setView(this);
        update((NumberleModel) this.model, null);
        initializeFrame();
    }


    public NumberleView(INumberleModel model, NumberleController controller,boolean lastFlag1,boolean lastFlag2,boolean lastFlag3) {
        this.model = model;
        this.controller = controller;
        controller.startNewGame();
        ((NumberleModel) this.model).addObserver(this);
        this.controller.setView(this);
        update((NumberleModel) this.model, null);
        this.lastFlag1=lastFlag1;
        this.lastFlag2=lastFlag2;
        this.lastFlag3=lastFlag3;
        initializeFrame();
    }


    public NumberleView() {
        initializeFrame();
    }

    /**
     * Initializes the main frame of the application with all user interface components.
     */
    public void initializeFrame() {
        // Set default close operation and frame dimensions, and disable resizing
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setResizable(false);

        // Create top panel and set layout
        JPanel topPanel = new JPanel(new BorderLayout());
        String answer=null;
        if(controller.isShouldDisplayTarget()) {
            answer = "The anwser equation：" + controller.getTargetWord();
        }
        JLabel topLabel = new JLabel(answer);
        topLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(topLabel, BorderLayout.CENTER);

        // Adding an icon to the top panel
        JLabel iconLabel = new JLabel(new ImageIcon("E:\\大四下\\AOOP\\cw1\\202018010111-coursework\\icon(1).png"));
        iconLabel.setHorizontalAlignment(JLabel.LEFT);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        topPanel.add(iconLabel, BorderLayout.WEST);
        frame.add(topPanel, BorderLayout.NORTH);

        // Keyboard panel setup
        JPanel keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new BorderLayout());

        // Center panel setup for display
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        center.add(new JPanel());
        frame.add(center, BorderLayout.CENTER);

        // Display panel configuration
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(6, 7, 5, 5));
        displayPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15,15));

        // Text field setup with custom fonts and borders
        int textFieldColumns = 3;
        Font boldFont = new Font("Arial", Font.BOLD, 18);
        Border roundedBorder = BorderFactory.createLineBorder(Color.GRAY, 2, true);
        Border marginBorder = new EmptyBorder(5, 5, 5, 5);
        Border compoundBorder = BorderFactory.createCompoundBorder(roundedBorder, marginBorder);


        for (int i = 0; i < INumberleModel.MAX_ATTEMPTS; i++) {
            for (int j = 0; j < 7; j++) {
                fields[i][j] = new JTextField();
                JTextField textField = new JTextField();
                fields[i][j].setFont(boldFont);
                textField.setColumns(textFieldColumns);
                fields[i][j].setBorder(compoundBorder);
                fields[i][j].setEditable(false);
                fields[i][j].setHorizontalAlignment(JTextField.CENTER);
                displayPanel.add(fields[i][j]);
            }
        }
        center.add(displayPanel);
        center.add(new JPanel());
        frame.add(center, BorderLayout.CENTER);

        // Keyboard panel layout with numeric and operation keys
        keyboardPanel.setLayout(new GridLayout(2, INumberleModel.MAX_ATTEMPTS, 5, 5));
        keyboardPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel numberPanel = new JPanel(new GridLayout(1, 10, 5, 5));
        String[] numberKeys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

        for (String key : numberKeys) {
            JButton button = new JButton(key);
            buttonMap.put(key, button);
            button.setPreferredSize(new Dimension(100, 60));
            button.addActionListener(e -> {
                if (currentPosition < 7) {
                    fields[lineNumber][currentPosition].setText(key);
                    currentPosition++;
                }
            });
            numberPanel.add(button);
        }

        JPanel operationPanel = new JPanel(new GridLayout(1, 7, 5, 5));
        String[] operationKeys = {"Back", "+", "-", "*", "/", "=", "Restart","Set", "Enter"};
        for (String key : operationKeys) {
            JButton button = new JButton(key);
            buttonMap.put(key, button);
            button.addActionListener(e -> {
                if (currentPosition <= 7) {
                    switch (key) {
                        case "Back":
                            if (currentPosition > 0) {
                                fields[lineNumber][currentPosition - 1].setText("");
                                currentPosition--;
                            }
                            break;
                        case "Enter":
                            for (int i = 0; i < currentPosition; i++) {
                                input.append(fields[lineNumber][i].getText());
                            }

                            controller.processInput(input.toString());
                            input.setLength(0);


                            break;
                        case "+":
                        case "-":
                        case "*":
                        case "/":
                        case "=":
                            if (currentPosition < 7) {
                                fields[lineNumber][currentPosition].setText(key);
                                currentPosition++;
                            }
                    }
                }
            });
            operationPanel.add(button);
        }
        buttonMap.get("Restart").setEnabled(false);
        buttonMap.get("Restart").addActionListener(e -> {
            restartGame();
        });

        frame.add(displayPanel, BorderLayout.CENTER);

        keyboardPanel.add(numberPanel, BorderLayout.NORTH);
        keyboardPanel.add(operationPanel, BorderLayout.CENTER);

        frame.add(keyboardPanel, BorderLayout.SOUTH);

        frame.setVisible(true);


        // Action listener for settings button to handle game options
        buttonMap.get("Set").addActionListener(e -> {
            Object[] options = {"Display errors", "Display target equation", "Random generate"};

            // Create a checkbox panel for user options
            JPanel panel = new JPanel();
            JCheckBox[] checkBoxes = new JCheckBox[options.length];

            // Loop through options to create checkboxes for each one
            for (int i = 0; i < options.length; i++) {
                checkBoxes[i] = new JCheckBox((String) options[i]);

                // Set the checkbox state based on previous user choices
                checkBoxes[i].setSelected(i == 0 ? lastFlag1 : (i == 1 ? lastFlag2 : lastFlag3));

                // Add each checkbox to the panel
                panel.add(checkBoxes[i]);
            }

            // Display a confirmation dialog with the checkbox panel
            int result = JOptionPane.showConfirmDialog(null, panel,
                    "Select Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // Handle the user's selection from the dialog
            if (result == JOptionPane.OK_OPTION) {
                frame.dispose();

                // Retrieve the selection states from checkboxes and update lastFlag variables
                boolean flag1 = checkBoxes[0].isSelected();
                boolean flag2 = checkBoxes[1].isSelected();
                boolean flag3 = checkBoxes[2].isSelected();
                lastFlag1 = flag1;
                lastFlag2 = flag2;
                lastFlag3 = flag3;
                NumberleModel numberleModel = new NumberleModel(flag1, flag2, flag3);
                NumberleController controller = new NumberleController(numberleModel);
                controller.startNewGame();
                ((NumberleModel) this.model).addObserver(this);
                this.controller.setView(this);
                update((NumberleModel) this.model, null);
                new NumberleView(numberleModel,controller,flag1,flag2,flag3);
            }
        });
    }





    @Override
    /**
     * Handles updates from the observable model, processing various types of messages and updating the UI accordingly.
     *
     * @param o The observable object.
     * @param arg The argument passed by the notifyObservers in Observable, expected to be either a String or ArrayList.
     */
    public void update(Observable o, Object arg) {
        // Check if the update argument is a string, which is used for status messages
        if (arg instanceof String) {
            String message = (String) arg;

            // Handle different types of messages received from the Observable
            switch (message) {
                case "Invalid Input":
                    JOptionPane.showMessageDialog(frame, message, "Game Won", JOptionPane.INFORMATION_MESSAGE);
                    currentPosition = input.length();
                    break;
                case "Game Won":
                    if (controller.isGameWon()) {
                        int option = JOptionPane.showConfirmDialog(frame,
                                "Congratulations! You won! " + controller.getTargetWord()
                                        + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            restartGame();
                        } else {
                            disableInputComponents();
                            ((NumberleModel) this.model).deleteObserver(this);
                            System.out.println("User doesn't want to play again. Exiting the game.");
                        }
                    } else {
                        int option = JOptionPane.showConfirmDialog(frame, "Sorry, you lost. The word was "
                                + controller.getTargetWord()
                                + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.YES_OPTION) {
                            restartGame();
                        } else {
                            disableInputComponents();
                            ((NumberleModel) this.model).deleteObserver(this);
                            System.out.println("User doesn't want to play again. Exiting the game.");
                        }
                    }

                    break;
                case "Game Over":
                    if (controller.isGameWon()) {
                        int option = JOptionPane.showConfirmDialog(frame, "Congratulations! You won! "
                                + controller.getTargetWord()
                                + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            restartGame();
                        } else if(option == JOptionPane.NO_OPTION){
                            disableInputComponents();
                            // Delete observer
                            ((NumberleModel) this.model).deleteObserver(this);

                        }
                    } else {
                        int option = JOptionPane.showConfirmDialog(frame, "Sorry, you lost. The word was "
                                + controller.getTargetWord()
                                + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.YES_OPTION) {
                            restartGame();
                        } else if(option == JOptionPane.NO_OPTION){
                            disableInputComponents();
                            ((NumberleModel) this.model).deleteObserver(this);
                        }
                    }
                    lineNumber = INumberleModel.MAX_ATTEMPTS - controller.getRemainingAttempts();
                    break;
                case "Try Again":
                    JOptionPane.showMessageDialog(frame, message + ", Attempts remaining: "
                            + controller.getRemainingAttempts(), "Try Again", JOptionPane.INFORMATION_MESSAGE);
                    lineNumber = INumberleModel.MAX_ATTEMPTS - controller.getRemainingAttempts();
                    break;
                case "Invalid Input: The equation must contain exactly one equals sign (=).":
                    JOptionPane.showMessageDialog(frame,
                            "Invalid Input: The equation must contain exactly one equals sign (=).",
                            "Try Again", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "Incorrect Equation: The left side is not equal to right side.":
                    JOptionPane.showMessageDialog(frame,
                            "Incorrect Equation: The left side is not equal to right side.",
                            "Try Again", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "Invalid Input: The equation must be 7 characters long.":
                    JOptionPane.showMessageDialog(frame,
                            "Invalid Input: The equation must be 7 characters long.",
                            "Try Again", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "Invalid Input: The equation must contain at least one operator (+, -, *, /).":
                    JOptionPane.showMessageDialog(frame,
                            "Invalid Input: The equation must contain at least one operator (+, -, *, /).",
                            "Try Again", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
        } else if (arg instanceof ArrayList) {
            // Handle updates that include color feedback for the input fields
            ArrayList<String> list = (ArrayList) arg;
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                updateFieldAndButtonColor(i, s);
            }
            currentPosition = 0;
            buttonMap.get("Restart").setEnabled(true);
        }


    }

    private void restartGame() {
        frame.dispose();
        NumberleModel numberleModel = new NumberleModel(true,
                true, true); // Assuming default flags for new game
        NumberleController controller = new NumberleController(numberleModel);
        controller.startNewGame();
        ((NumberleModel) this.model).addObserver(this);
        this.controller.setView(this);
        update((NumberleModel) this.model, null);
        new NumberleView(numberleModel, controller);
        frame.repaint();
    }

    /**
     * Disables some components of the GUI
     */
    private void disableInputComponents() {
        // Disable all text fields
        for (int i = 0; i < INumberleModel.MAX_ATTEMPTS; i++) {
            for (int j = 0; j < 7; j++) {
                fields[i][j].setEnabled(false);
            }
        }
        // Disable all buttons except for "Restart" and "Set"
        for (String key : buttonMap.keySet()) {
            JButton button = buttonMap.get(key);
            if (!key.equals("Restart") && !key.equals("Set")) {
                button.setEnabled(false);
            }
        }
    }


    /**
     * Updates the color of the specified field and its corresponding button based on the game feedback.
     * This method changes the background and text color to visually indicate the state of the guess.
     *
     * @param index The index of the field and button to update.
     * @param colorName The name of the color to apply, which determines the feedback visual.
     */
    private void updateFieldAndButtonColor(int index, String colorName) {
        JTextField textField = fields[lineNumber][index];
        JButton button = buttonMap.get(textField.getText());

        switch (colorName) {
            case "Gray":
                setFieldAndButtonColor(textField, button, new Color(175, 175, 175), Color.WHITE);
                break;
            case "Orange":
                setFieldAndButtonColor(textField, button, new Color(245, 164, 71), Color.WHITE);
                break;
            case "Green":
                setFieldAndButtonColor(textField, button, new Color(35, 206, 110), Color.WHITE);
                break;
            default:
                break;
        }
    }

    /**
     * Sets the background and foreground colors for a given text field and its corresponding button.
     * This method ensures that the visual state of both the text field and the button reflects the game's feedback,
     * but prevents changes if the button is already set to a winning state (Green).
     *
     * @param textField The text field whose color needs to be updated.
     * @param button The button corresponding to the text field.
     * @param backgroundColor The new background color to set.
     * @param foregroundColor The new foreground color to set.
     */
    private void setFieldAndButtonColor(JTextField textField,JButton button,Color backgroundColor,Color foregroundColor){
        if (!button.getBackground().equals(new Color(35, 206, 110))) {
            button.setBackground(backgroundColor);
            button.setForeground(foregroundColor);
        }
        textField.setBackground(backgroundColor);
        textField.setForeground(foregroundColor);
    }
}
