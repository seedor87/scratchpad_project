package eventHandlers;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class BorderShapeResizeHandler implements EventHandler<MouseEvent> {

    boolean using = false;

    double originalScreenX;
    double originalScreenY;
    double originalScreenWidth;
    double originalScreenHeight;
    double xChange = 0;
    double yChange = 0;

    double x = -1;
    double y = -1;

    private AnnotationToolApplication annotationToolApplication;

    public BorderShapeResizeHandler(AnnotationToolApplication annotationToolApplication) {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(MouseEvent event) {
        Stage mouseCatchingStage = annotationToolApplication.getMouseCatchingStage();

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            mouseCatchingStage.getScene().setCursor(Cursor.CLOSED_HAND);

            using = true;

            originalScreenX = mouseCatchingStage.getX();
            originalScreenY = mouseCatchingStage.getY();
            originalScreenWidth = mouseCatchingStage.getWidth();
            originalScreenHeight = mouseCatchingStage.getHeight();

            x = event.getX();
            y = event.getY();
        }
        else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            xChange = x - event.getX();
            yChange = y - event.getY();
            annotationToolApplication.resizeAnnotationWindow(
                    originalScreenWidth - xChange,
                    originalScreenHeight - yChange);
        }
        else if (event.getEventType() == MouseEvent.MOUSE_RELEASED){
            using = false;
            annotationToolApplication.unResetHandlers();
        } else if(event.getEventType() == MouseEvent.MOUSE_EXITED_TARGET) {
            mouseCatchingStage.getScene().setCursor(Cursor.DEFAULT);
        } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET) {
            mouseCatchingStage.getScene().setCursor(Cursor.OPEN_HAND);
        }
    }
}
