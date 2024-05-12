import Numberle_Controller.NumberleController;
import Numberle_Model.INumberleModel;
import Numberle_Model.NumberleModel;
import Numberle_View.NumberleView;

public class GUIApp {
    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        createAndShowGUI();
                    }
                }
        );
    }

    public static void createAndShowGUI() {
        INumberleModel model = new NumberleModel(true,false,true);
        NumberleController controller = new NumberleController(model);
        while (!controller.isGameOver()) {
        }
        NumberleView view = new NumberleView(model, controller);
    }
}