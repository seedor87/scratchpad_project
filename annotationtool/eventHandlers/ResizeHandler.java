package eventHandlers;

import annotation.AnnotationToolApplication;
import org.jnativehook.GlobalScreen;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import utils.WindowInfo;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author armstr
 *
 * Ensures that the annotation window remains attached to any relevant window, snapping to it whenever the mouse button is released.
 */
public class ResizeHandler implements NativeMouseInputListener
{
    private AnnotationToolApplication annotationToolApplication;

    public ResizeHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    public ResizeHandler() {
        // Get the logger for "org.jnativehook" and set the level to warning.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
        WindowInfo windowID = annotationToolApplication.getWindowID();

        if(windowID != null) {
            annotationToolApplication.resnapToWindow(windowID);
        }
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeEvent) {}
    @Override
    public void nativeMousePressed(NativeMouseEvent nativeEvent) {}
    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeEvent) {}
    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeEvent) {}
}
