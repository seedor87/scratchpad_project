package changeItem;

import annotation.AnnotationToolApplication;

/**
 * Created by Brennan on 6/13/2017
 */
public interface ChangeItem
{
    /**
     * This method should be called whenever there is a change that
     * needs to be added to a stage for the first time.
     * @param annotationToolApplication The AnnotationToolApplication that is having the change added to.
     */
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication);

    /**
     * This method should be called whenever a change is needed to be undone from
     * an AnnotationToolApplication.
     * @param annotationToolApplication The AnnotationToolApplication that needs to have the change
     *                                  undone from itself.
     */
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication);

    /**
     * This method should be called whenever a change is needed to be redone to an AnnotationToolApplication.
     * @param annotationToolApplication The AnnotationToolApplication that needs to have the change redone to
     *                                  itself
     */
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication);
}
