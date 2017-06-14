package changeItem;

import annotation.AnnotationToolApplication;
import javafx.stage.Stage;

/**
 * Created by Brennan on 6/13/2017.
 */
public interface ChangeItem
{
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication);
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication);
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication);
}
