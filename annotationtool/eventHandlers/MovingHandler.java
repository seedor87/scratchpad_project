package eventHandlers;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Handler for moving the stage. should be implemented with MouseEvent.ANY when you add the
 * handler to the mousecatchingscene.
 */
public class MovingHandler implements EventHandler<MouseEvent>
{
    private double originalX = -1;
    private double originalY;
    private double originalStageX;
    private double originalStageY;
    private AnnotationToolApplication annotationToolApplication;
    private Stage mouseCatchingStage;
    private Scene mouseCatchingScene;
    private ImageCursor grabCursor = new ImageCursor(new Image("icons/grab.png"));
    private ImageCursor handCursor = new ImageCursor(new Image("icons/hand.png"));

    public MovingHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(MouseEvent event) {
        if(mouseCatchingStage == null)
        {
            mouseCatchingStage = annotationToolApplication.getMouseCatchingStage();
            mouseCatchingScene = annotationToolApplication.getMouseCatchingScene();
        }
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            originalX = event.getScreenX();
            originalY = event.getScreenY();
            originalStageX = mouseCatchingStage.getX();
            originalStageY = mouseCatchingStage.getY();
            mouseCatchingScene.setCursor(grabCursor);
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            double changeX = event.getScreenX() - originalX;
            double changeY = event.getScreenY() - originalY;
            mouseCatchingStage.setX(originalStageX + changeX);
            mouseCatchingStage.setY(originalStageY + changeY);
        } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            originalX = -1;
            mouseCatchingScene.setCursor(handCursor);
        }
    }
}

