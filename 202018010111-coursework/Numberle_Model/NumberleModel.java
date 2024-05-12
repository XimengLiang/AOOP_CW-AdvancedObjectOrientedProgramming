package Numberle_Model;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NumberleModel extends Observable implements INumberleModel {
    private String targetNumber;
    private StringBuilder currentGuess;
    private int remainingAttempts;
    private boolean gameWon;
    private boolean shouldValidateEquation;
    private boolean shouldDisplayTarget;
    private boolean shouldRandomizeTarget;
    private List<String> equations = new ArrayList<>();
    private Set<Character> unusedDigitsAndSymbols = new HashSet<>();
    private Set<Character> presentButIncorrectlyPlaced = new HashSet<>();
    private Set<Character> correctlyPlaced = new HashSet<>();
    private Set<Character> missingFromEquation = new HashSet<>();



    /**
     * Constructs a new NumberleModel with specified settings for equation validation,
     * displaying the target equation, and randomizing the target equation.
     *
     * @param shouldValidateEquation If true, the input equations are validated against mathematical rules and displayed.
     * @param shouldDisplayTarget If true, displays the target equation.
     * @param shouldRandomizeTarget If true, the target equation is generated randomly; otherwise,use default equation
     */
    public NumberleModel(boolean shouldValidateEquation, boolean shouldDisplayTarget, boolean shouldRandomizeTarget) {
        this.shouldValidateEquation = shouldValidateEquation;
        this.shouldDisplayTarget = shouldDisplayTarget;
        this.shouldRandomizeTarget = shouldRandomizeTarget;
        // Generate the initial target equation based on whether randomization is enabled
        targetNumber = generateTargetEquation();
        if (shouldRandomizeTarget) {
            targetNumber = generateTargetEquation();
        } else {
            // Fixed default equation
            targetNumber = "3+4*1=7";
        }
        if (shouldDisplayTarget) {
            System.out.println("Target equation set to: " + targetNumber);
        }
    }

    public boolean invariant(){
        return(targetNumber != null && targetNumber.length() == 7)&&
                (remainingAttempts >= 0 && remainingAttempts <= MAX_ATTEMPTS)&&
                (!gameWon || isGameOver())&&
                ( !shouldValidateEquation || isMathematicallyCorrect(targetNumber))&&
                hasNoIntersection(unusedDigitsAndSymbols, presentButIncorrectlyPlaced) &&
                hasNoIntersection(unusedDigitsAndSymbols, correctlyPlaced) &&
                hasNoIntersection(unusedDigitsAndSymbols, missingFromEquation) &&
                hasNoIntersection(presentButIncorrectlyPlaced, correctlyPlaced) &&
                hasNoIntersection(presentButIncorrectlyPlaced, missingFromEquation) &&
                hasNoIntersection(correctlyPlaced, missingFromEquation);
    }



    /**
     * Determines if two sets of characters have no intersection.
     * This method checks whether there is any common element between two character sets.
     *
     * @param set1 The first set of characters to be compared.
     * @param set2 The second set of characters to be compared.
     * @return true if there is no common character between set1 and set2; otherwise, false.
     */
    private boolean hasNoIntersection(Set<Character> set1, Set<Character> set2) {
        // to check whether the two sets intersect, and return false if they do
        boolean noIntersection = true;

        // Loop through each character in set
        for (char c : set1) {
            if (set2.contains(c)) {
                noIntersection = false;
                break;
            }
        }
        return noIntersection;
    }


    /**
     * Initializes the game state to default values, ensuring the game is ready to start anew.
     * This method sets the initial conditions for a new game round.
     *
     * @invariant invariant()
     *
     * @requires true
     *           // Pre-condition: No specific requirements since this method is used to initialize or reset the game.
     *
     * @ensures \old(targetNumber).equals(targetNumber) &&
     *          currentGuess.length() == 7 &&
     *          currentGuess.toString().trim().isEmpty() &&
     *          remainingAttempts == MAX_ATTEMPTS &&
     *          !gameWon
     *
     */
    @Override
    public void initialize() {
        assert targetNumber != null && targetNumber.length() == 7 :
                "Pre-condition failed: targetNumber should be properly set before re-initialization";
        currentGuess = new StringBuilder("       ");
        remainingAttempts = MAX_ATTEMPTS;
        gameWon = false;
        assert !gameWon : "Game won flag should be reset to false";
        setChanged();
        notifyObservers();
//        System.out.println(targetNumber);
        assert invariant() : "Invariant check failed after initialization";
    }


    /**
     * Processes the input by checking its validity against several rules: length, character content,
     * correct number of equals signs, and mathematical correctness. Updates game state accordingly.
     * @invariant invariant()
     *
     * @requires input != null && input.length() == 7;
     *           "Pre-condition: Input must be exactly 7 characters long and non-null."
     *
     * @ensures \old(remainingAttempts) > 0;
     *          "Pre-condition: There should be at least one remaining attempt before processing input."
     *
     * @ensures \result == true <==> (!\old(gameWon) && remainingAttempts > 0 && isInputValid(input));
     *          "Post-condition: True is returned only if the game was not won previously, there are remaining attempts,
     *          and the input was valid according to game rules."
     *
     * @ensures \result == false <==> input.length() != 7 || input has illegal characters ||
     *          input has != 1 equals sign || !isMathematicallyCorrect(input) || \old(remainingAttempts) <= 0;
     *          "Post-condition: False is returned if the input is invalid due to wrong length, illegal characters,
     *          incorrect number of equals signs, mathematical incorrectness, or no remaining attempts."
     *
     * @ensures (remainingAttempts == \old(remainingAttempts) - 1) || (\old(remainingAttempts) == 0);
     *          "Post-condition: Remaining attempts should decrease by one unless they were already zero."
     *
     */
    @Override
    public boolean processInput(String input) {
        assert invariant() : "Invariants check failed at start of processInput";

        // Check if input is null and the length
        if (input == null || input.length() != 7) {
            setChanged();
            notifyObservers("Invalid Input: The equation must be 7 characters long.");
            System.out.println("Invalid Input: The equation must be 7 characters long.");
            return false;
        }

        assert input.length() == 7 : "Input length must be exactly 7 characters";

        // Check digit and operator
        for (char c : input.toCharArray()) {
            if (!Character.isDigit(c) && !"+-*/=".contains(String.valueOf(c))) {
                if(shouldValidateEquation) {
                    setChanged();
                    notifyObservers("Invalid Input: The equation contains illegal characters.");
                    System.out.println("Invalid Input: The equation contains illegal characters.");
                    return false;
                }
            }
        }
        // Check equal sign
        int equalsCount = 0;
        for (char c : input.toCharArray()) {
            if (c == '=') {
                equalsCount++;
            }
        }
        if (equalsCount != 1) {
            if(shouldValidateEquation) {
                setChanged();
                notifyObservers("Invalid Input: The equation must contain exactly one equals sign (=).");
                System.out.println("Invalid Input: The equation must contain exactly one equals sign (=).");
                return false;
            }
        }else{
            // Check at least one operator
            if (!input.matches(".*[\\+\\-\\*/].*")) {
                if(shouldValidateEquation) {
                    setChanged();
                    notifyObservers("Invalid Input: The equation must contain at least one operator (+, -, *, /).");
                    System.out.println("Invalid Input: The equation must contain at least one operator (+, -, *, /).");
                    return false;
                }
            }else {// Verify whether the left side and right side is equal
                if (!isMathematicallyCorrect(input)) {
                    if(shouldValidateEquation) {
                        setChanged();
                        notifyObservers("Incorrect Equation: The left side is not equal to right side.");
                        System.out.println("Incorrect Equation: The left side is not equal to right side.");
                        return false;

                    }

                }
            }
        }
        // Ensure there are remaining attempts before decrementing
        assert remainingAttempts > 0 : "Remaining attempts should be greater than zero before decrementing";
        remainingAttempts--;
        assert remainingAttempts >= 0 : "Remaining attempts should not go below zero";

        ArrayList<String> answerList = new ArrayList<>();
        assert answerList.isEmpty() : "Answer list should be empty before processing new input";

        // Compare the input with the target number and generate feedback
        if (input.equals(targetNumber)) {
            gameWon = true;
            for(int i = 0; i < input.length(); i++) {
                answerList.add("Green");
                System.out.print("Green ");
            }
            System.out.println();
        } else {
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (i < targetNumber.length() && c == targetNumber.charAt(i)) {
                    answerList.add("Green");
                    System.out.print("Green!");
                } else if (targetNumber.contains(String.valueOf(c))) {
                    answerList.add("Orange");
                    System.out.print("Orange! ");
                } else {
                    answerList.add("Gray");
                    System.out.print("Gray! ");
                }
            }
        }
        setChanged();
        notifyObservers(answerList);
        System.out.println();
        updateSets(input, targetNumber);

        // Check if the game is over and update observers accordingly
        if (isGameOver()) {
            remainingAttempts = INumberleModel.MAX_ATTEMPTS;
            setChanged();
            notifyObservers(gameWon ? "Game Won" : "Game Over");
        } else {
            System.out.println(getFeedbackMessage());
            setChanged();
            notifyObservers("Try Again");
        }
        assert invariant() : "Invariants check failed at end of processInput";
        return true;

    }


    /**
     * Constructs and returns a feedback message that describes the state of the current guesses in terms of
     * unused digits and symbols, present but incorrectly placed, correctly placed, and missing elements from equation.
     * @invariant invariant()
     *
     * @invariant unusedDigitsAndSymbols != null && presentButIncorrectlyPlaced != null &&
     *                  correctlyPlaced != null && missingFromEquation != null;
     *
     * @requires true;
     *           "No specific preconditions. Method should always be callable."
     *
     * @ensures \result.contains("Unused digits and symbols:") &&
     *          \result.contains("Present but incorrectly placed:") &&
     *          \result.contains("Exist and correctly placed:") &&
     *          \result.contains("Not in the equation:");
     *          "Post-condition: The result must contain specific sections describing the state of the guesses."
     *
     */
    public String getFeedbackMessage() {
        assert invariant() : "Invariant check failed at the start of getFeedbackMessage";
        StringBuilder feedback = new StringBuilder();
        feedback.append("Unused digits and symbols: ").append(unusedDigitsAndSymbols).append("\n");
        feedback.append("Present but incorrectly placed: ").append(presentButIncorrectlyPlaced).append("\n");
        feedback.append("Exist and correctly placed: ").append(correctlyPlaced).append("\n");
        feedback.append("Not in the equation: ").append(missingFromEquation).append("\n");
        assert invariant() : "Invariant check failed after getting feedback";
        return feedback.toString();
    }




    /**
     * Updates the sets that track the placement and usage of characters based on the current input compared to the target.
     * This function modifies various sets to reflect the correct placement, presence, and absence of characters from input.
     *
     * @param input The player's current guess input.
     * @param target The target or correct equation that needs to be guessed.
     */
    private void updateSets(String input, String target) {
        for (char c : "0123456789+-*/=".toCharArray()) {
            unusedDigitsAndSymbols.add(c);
        }

        // Compare input and target, upadate
        for (int i = 0; i < input.length(); i++) {
            char inputChar = input.charAt(i);
            char targetChar = target.charAt(i);

            if (inputChar == targetChar) {
                // If character matches the target in the correct position, add to correctlyPlaced and
                // remove from unusedDigitsAndSymbols.
                correctlyPlaced.add(inputChar);
                unusedDigitsAndSymbols.remove(inputChar);
            } else {
                if (target.indexOf(inputChar) >= 0) {
                    // If the character is present in the target but not at the correct position,
                    // add it to presentButIncorrectlyPlaced set and remove from unusedDigitsAndSymbols
                    presentButIncorrectlyPlaced.add(inputChar);
                    unusedDigitsAndSymbols.remove(inputChar);
                } else {
                    // If the character is not present in the target at all,
                    // add it to missingFromEquation set
                    missingFromEquation.add(inputChar);
                }
            }
        }

        // Ensure that no characters are intersected between correctlyPlaced and presentButIncorrectlyPlaced
        presentButIncorrectlyPlaced.removeIf(correctlyPlaced::contains);

        // Finally, ensure unusedDigitsAndSymbols does not contain any characters that have been placed or are missing
        unusedDigitsAndSymbols.removeAll(correctlyPlaced);
        unusedDigitsAndSymbols.removeAll(presentButIncorrectlyPlaced);
        unusedDigitsAndSymbols.removeAll(missingFromEquation);
    }

    /**
     * Evaluates if a mathematical expression is correct by comparing its left and right sides.
     *
     * @param expression The mathematical expression to evaluate.
     * @return true if the left side of the equation equals the right side; otherwise, false.
     */
    private boolean isMathematicallyCorrect(String expression) {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

            // Split the expression around the equals sign and evaluate both sides
            String[] parts = expression.split("\\s*=\\s*", 2);
            return String.valueOf(engine.eval(parts[0])).equals(String.valueOf(engine.eval(parts[1])));
        } catch (ScriptException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }


    /**
     * Generates a target equation either by selecting randomly from a file or using a default value.
     *
     * @return The target equation as a string.
     */
    private String generateTargetEquation() {
        final String fileName = "equations.txt";
        BufferedReader reader = null;
        String targetEquation = null;

        try {
            reader = new BufferedReader(new FileReader(fileName));
            // // Read all lines from the file and add them to the list after trimming white spaces
            String line;
            while ((line = reader.readLine()) != null) {
                equations.add(line.trim());
            }

            if (!equations.isEmpty()) {
                Random rand = new Random();
                targetEquation = equations.get(rand.nextInt(equations.size()));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + fileName);
            e.printStackTrace();
        } finally {
            // Ensure the reader is closed properly
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing file reader for: " + fileName);
                e.printStackTrace();
            }
        }

        // Return a default equation if no equations were loaded from the file
        return targetEquation != null  ? targetEquation : "3+4*1=7";
    }

    public boolean isShouldDisplayTarget() {
        return shouldDisplayTarget;
    }


    @Override
    public boolean isGameOver() {
        return remainingAttempts <= 0 || gameWon;
    }

    @Override
    public boolean isGameWon() {
        return gameWon;
    }

    @Override
    public String getTargetNumber() {
        return targetNumber;
    }

    @Override
    public StringBuilder getCurrentGuess() {
        return currentGuess;
    }

    @Override
    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    @Override
    public void startNewGame() {
        initialize();
    }
}
