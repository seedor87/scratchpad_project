package eventHandlers;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class SaveTextBoxHandler implements EventHandler<MouseEvent>
{
    private AnnotationToolApplication annotationToolApplication;
    public  SaveTextBoxHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }
    @Override
    public void handle(MouseEvent event)
    {
        if(annotationToolApplication.getSaveTextBox())
        {
            annotationToolApplication.saveTextBox();
            annotationToolApplication.setSaveTextBox(false);
        }
    }
}
