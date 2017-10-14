package eventHandlers;

import annotation.AnnotationToolApplication;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Handles if the user presses the escape button.
 * If the user is in a text box, it closes the text box. It returns you to
 * drawing mode if you are in a text box.
 *
 * If you are not in a text box, the program is closed.
 */
public class ShortcutHandler implements EventHandler<KeyEvent>
{
    private  AnnotationToolApplication annotationToolApplication;

    public ShortcutHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }
    public void handle(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ESCAPE) {
            if(annotationToolApplication.getMakingTextBox()) {
                annotationToolApplication.unResetHandlers();
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }
                });
            }
        }
    }
}