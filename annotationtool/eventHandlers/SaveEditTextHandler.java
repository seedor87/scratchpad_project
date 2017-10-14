package eventHandlers;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class SaveEditTextHandler implements EventHandler<MouseEvent>
{
    private AnnotationToolApplication annotationToolApplication;
    public SaveEditTextHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }
    @Override
    public void handle(MouseEvent event)
    {

        if(annotationToolApplication.getSaveEditText())
        {
            annotationToolApplication.saveEditText();
            annotationToolApplication.setSaveEditText(false);
        }
    }
}

