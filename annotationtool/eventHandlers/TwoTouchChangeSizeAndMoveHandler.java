package eventHandlers;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.stage.Stage;

/**
 * Handles touch events that involve two touch points.
 * can be used to move the window with two fingers, and/or to
 * resize it with two fingers
 * should be implemented with TouchEvent.ANY when you add the
 * handler to the mousecatchingscene.
 */
public class TwoTouchChangeSizeAndMoveHandler implements EventHandler<TouchEvent> {
    boolean using = false;
    int topPointIndex;
    int bottomPointIndex;
    int rightPointIndex;
    int leftPointIndex;
    TouchPoint topPoint;
    TouchPoint bottomPoint;
    TouchPoint leftPoint;
    TouchPoint rightPoint;
    double originalScreenX;
    double originalScreenY;
    double originalScreenWidth;
    double originalScreenHeight;

    double rightXChange = 0;
    double topYChange = 0;
    double leftXChange = 0;
    double bottomYChange = 0;

    private AnnotationToolApplication annotationToolApplication;

    public TwoTouchChangeSizeAndMoveHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(TouchEvent event) {
        Stage mouseCatchingStage = annotationToolApplication.getMouseCatchingStage();
        if(event.getTouchCount() == 2) {
            using = true;
                /*
                Sets up some variables to keep track of which first point was which.
                 */
            if(event.getEventType() == TouchEvent.TOUCH_PRESSED) {
                annotationToolApplication.resetHandlers();
                originalScreenX = mouseCatchingStage.getX();
                originalScreenY = mouseCatchingStage.getY();
                originalScreenWidth = mouseCatchingStage.getWidth();
                originalScreenHeight = mouseCatchingStage.getHeight();
                double rightXChange = 0;
                double topYChange = 0;
                double leftXChange = 0;
                double bottomYChange = 0;

                TouchPoint point1 = event.getTouchPoints().get(0);
                TouchPoint point2 = event.getTouchPoints().get(1);
                if(point1.getY() < point2.getY()) {
                    topPointIndex = 0;
                    bottomPointIndex = 1;
                    topPoint = point1;
                    bottomPoint = point2;
                } else {
                    bottomPointIndex = 0;
                    topPointIndex = 1;
                    bottomPoint = point1;
                    topPoint = point2;
                }
                if(point1.getScreenX() > point2.getScreenX()) {
                    rightPointIndex = 0;
                    leftPointIndex = 1;
                    rightPoint = point1;
                    leftPoint = point2;
                } else {
                    rightPointIndex = 1;
                    leftPointIndex = 0;
                    rightPoint = point2;
                    leftPoint = point1;
                }
            } else if(event.getEventType() == TouchEvent.TOUCH_MOVED && event.getTouchCount() == 2) {
                for(TouchPoint touchPoint : event.getTouchPoints()) {
                    if(touchPoint.getState() != TouchPoint.State.STATIONARY) {
                        int index = event.getTouchPoints().indexOf(touchPoint);
                        if(index == rightPointIndex) {
                            rightXChange = touchPoint.getScreenX() - rightPoint.getScreenX();
                        } else if(index == leftPointIndex) {
                            leftXChange = leftPoint.getScreenX() - touchPoint.getScreenX();
                        }
                        if(index == topPointIndex) {
                            topYChange = topPoint.getScreenY() - touchPoint.getScreenY();
                        } else if(index == bottomPointIndex) {
                            bottomYChange = touchPoint.getScreenY() - bottomPoint.getScreenY();
                        }
                        //mouseCatchingStage.setHeight(topYChange + originalScreenHeight + bottomYChange);
                        //mouseCatchingStage.setWidth(rightXChange + leftXChange);
                        annotationToolApplication.resizeAnnotationWindow2(rightXChange  +  originalScreenWidth + leftXChange
                                ,topYChange + originalScreenHeight + bottomYChange);
                        mouseCatchingStage.setX(originalScreenX - leftXChange);
                        mouseCatchingStage.setY(originalScreenY - topYChange);
                    }
                }
            }
        } else if(event.getEventType() == TouchEvent.TOUCH_RELEASED && using) {
            using = false;
            annotationToolApplication.unResetHandlers();
        }

    }
}

