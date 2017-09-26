/*
 * @author PAD01
 * @doel
 ________________________________________
 / Q: How many elephants can you fit in a \
 | VW Bug? A: Four. Two in the front, two |
 | in the back.                           |
 |                                        |
 | Q: How can you tell if an elephant is  |
 | in your refrigerator? A: There's a     |
 | footprint in the mayo.                 |
 |                                        |
 | Q: How can you tell if two elephants   |
 | are in your refrigerator? A: There's   |
 | two footprints in the mayo.            |
 |                                        |
 | Q: How can you tell if three elephants |
 | are in your refrigerator? A: The door  |
 | won't shut.                            |
 |                                        |
 | Q: How can you tell if four elephants  |
 | are in your refrigerator? A: There's a |
 \ VW Bug in your driveway.               /
  ----------------------------------------
         \   ^__^
          \  (oo)\_______
             (__)\       )\/\
                 ||----w |
                 ||     ||
  */
package appkiller;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppKiller extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("killscherm.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();

    }

}
