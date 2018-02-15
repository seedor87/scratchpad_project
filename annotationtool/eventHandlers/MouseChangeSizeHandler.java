package eventHandlers;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class MouseChangeSizeHandler implements EventHandler<MouseEvent> {
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

    public MouseChangeSizeHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(MouseEvent event) {
        Stage mouseCatchingStage = annotationToolApplication.getMouseCatchingStage();

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            using = true;

            originalScreenX = mouseCatchingStage.getX();
            originalScreenY = mouseCatchingStage.getY();
            originalScreenWidth = mouseCatchingStage.getWidth();
            originalScreenHeight = mouseCatchingStage.getHeight();

            x = event.getX();
            y = event.getY();
        }
        else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && x > -1 && y > -1) {
            xChange = x - event.getX();
            yChange = y - event.getY();
            annotationToolApplication.resizeAnnotationWindow(
                    originalScreenWidth - xChange,
                    originalScreenHeight - yChange);
        }
        else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED){
            using = false;
            annotationToolApplication.unResetHandlers();
        }
    }
}

