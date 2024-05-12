package Numberle_Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NumberleModelTest {
    private INumberleModel instance;

    @BeforeEach
    void setUp() {
        instance = new NumberleModel(true, true, false);
    }

    @AfterEach
    void tearDown() {instance = null;}

    @Test
    /**
     * Tests the initialization of the NumberleModel to ensure it properly resets the game state.
     * This method checks that after initialization:
     * - The number of remaining attempts is set to the maximum defined by the model.
     * - The game is not in a "won" state.
     *
     * * @invariant invariant()
     *
     * @requires instance != null
     *   // Pre-condition: The instance of NumberleModel must be non-null and properly instantiated.
     *
     * @ensures instance.getRemainingAttempts() == INumberleModel.MAX_ATTEMPTS
     *   // Post-condition: After initialization, the remaining attempts should be reset to the maximum allowed attempts.
     *
     * @ensures !instance.isGameWon()
     *   // Post-condition: After initialization, the game should not be in a won state.
     *
     */
    void testInitialize() {
        instance.initialize();
        assertEquals(INumberleModel.MAX_ATTEMPTS, instance.getRemainingAttempts());
        assertFalse(instance.isGameWon());
    }


    @Test
    /**
     * This test checks the behavior of the NumberleModel when it processes various forms of invalid inputs.
     * The model should correctly reject these inputs and should not decrement the number of remaining attempts.
     *
     * @invariant \forall String input; model.isInputInvalid(input) => !model.processInput(input)
     * @invariant invariant()
     *
     * @requires model != null && model.isInitialized()
     *           // The model must be non-null and properly initialized before testing.
     *
     * @ensures \old(model.getRemainingAttempts()) == model.getRemainingAttempts()
     *          // Ensures that the number of attempts remain unchanged after processing invalid input.
     *
     */
    void testProcessInvalidInput() {
        instance.initialize();
        String[] invalidInputs = {
                "1++2=3",
                "123asds",
                "1234567",
                "123*-67",
                "12+-=67",
                "1+2+3=1",
                "1/3+1=2"
        };

        // Test each invalid input and assert the expected outcomes
        for (String input : invalidInputs) {
            boolean result = instance.processInput(input);
            assertFalse(result, "Expected false for invalid input: " + input);
            assertEquals(INumberleModel.MAX_ATTEMPTS, instance.getRemainingAttempts(),
                    "Attempt count should not decrease for invalid input");
        }
    }

    @Test
    /**
     * This test checks NumberleModel when it processes a series of valid inputs that should lead to a win.
     * The model should correctly accept these inputs and should recognize the win correctly.
     *
     * @invariant invariant()
     *
     * @invariant model.isGameWon() == (\exists String input; model.isWinningInput(input); model.processInput(input))
     *
     * @requires model != null && model.getTargetNumber() != null
     *           // The model must be non-null and properly initialized before testing.
     *
     * @ensures model.isGameWon()
     *          // Ensures that the game is won after the winning input is processed.
     *
     * @ensures \old(model.getRemainingAttempts()) == model.getRemainingAttempts()
     *          // Ensures that the number of attempts remain unchanged after processing winning input.
     */
    void testWinGame() {
        instance.initialize();
        String[] trialInputs = {
                "1+2*3=7", "1-1*1=0", "3+1*1=4", "3+1*4=7", "3+1-1=3"
        };
        String winningInput = "3+4*1=7";
        for (String input : trialInputs) {
            boolean result = instance.processInput(input);
            assertTrue(result, "Processing valid input should return true: " + input);
        }

        boolean winResult = instance.processInput(winningInput);
        assertTrue(winResult, "Processing winning input should return true");
        assertTrue(instance.isGameWon(), "Game should be won after correct guess");
        assertEquals(INumberleModel.MAX_ATTEMPTS, instance.getRemainingAttempts(),
                "Attempt count should remain the same when game is won");
    }
}
