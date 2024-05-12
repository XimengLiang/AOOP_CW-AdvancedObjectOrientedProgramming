import Numberle_Model.INumberleModel;
import Numberle_Model.NumberleModel;

import java.util.Scanner;

public class CLIApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean flag1 = false;
        boolean flag2 = false;
        boolean flag3 = false;
        boolean isValidInput = false;
        while (!isValidInput) {
            System.out.println("Enter 0 or 1 for each flag:");
            System.out.print("Validate Equation (0/1): ");
            int validateEquation = readFlag(scanner);
            System.out.print("Display Target (0/1): ");
            int displayTarget = readFlag(scanner);
            System.out.print("Randomize Target (0/1): ");
            int randomizeTarget = readFlag(scanner);

            if (validateEquation == 0 || validateEquation == 1 &&
                    displayTarget == 0 || displayTarget == 1 &&
                    randomizeTarget == 0 || randomizeTarget == 1) {

                flag1 = validateEquation == 0 ? false : true;
                flag2 = displayTarget == 0 ? false : true;
                flag3 = randomizeTarget == 0 ? false : true;
                isValidInput = true;
            } else {
                System.out.println("Invalid input. Please enter 0 or 1.");
            }
        }
        INumberleModel model = new NumberleModel(flag1, flag2, flag3);


        model.startNewGame();
        System.out.println("Welcome to Numberle - CLI Version. Guess the equation in 6 tries.");
        System.out.println("You have " + model.getRemainingAttempts()
                + " attempts to guess the right equation. Good luck!");

        while (!model.isGameOver()) {
            System.out.print("Enter your guess (7 characters long equation, e.g., '3+5*2=7'): ");
            String input = scanner.nextLine();


            model.processInput(input);

            // 根据游戏状态更新输出
            if (model.isGameOver()) {
                if (model.isGameWon()) {
                    System.out.println("Congratulations! You win! You've guessed the equation correctly: "
                            + model.getTargetNumber());
                } else {
                    System.out.println("Game Over! You lost! You've run out of attempts. The correct equation was: "
                            + model.getTargetNumber());
                }
                break;
            } else {
                System.out.println("Try again. You have " + model.getRemainingAttempts() + " attempts left.");
            }
        }

        scanner.close();
    }

    private static int readFlag(Scanner scanner) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine().trim());
                if (input == 0 || input == 1) {
                    return input;
                } else {
                    System.out.println("Invalid input. Please enter 0 or 1.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter 0 or 1.");
            }
        }
    }
}