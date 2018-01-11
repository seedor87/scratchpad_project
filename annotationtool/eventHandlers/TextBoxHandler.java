package eventHandlers;

import annotation.AnnotationToolApplication;
import changeItem.AddShape;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Creates a text box at the given location of click. Should be implemented with MouseEvent.MOUSE_CLICKED
 * TextBoxKeyHandler changes the text in the box if needed.
 */
public class TextBoxHandler implements EventHandler<MouseEvent> {
    private Text text;
    private AnnotationToolApplication annotationToolApplication;

    public TextBoxHandler (AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET) {
            annotationToolApplication.getMouseCatchingScene().setCursor(Cursor.OPEN_HAND);
        }
        else if (event.getEventType() == MouseEvent.MOUSE_EXITED_TARGET) {
            annotationToolApplication.getMouseCatchingScene().setCursor(annotationToolApplication.textCursor);
        } else {
            String defaultText = "Text";
            text = new Text(event.getX(), event.getY(), defaultText);
            annotationToolApplication.setText(text);
            text.setFont(new Font(annotationToolApplication.getTextFont(), annotationToolApplication.getTextSize()));
            text.setFill(annotationToolApplication.getPaint());
            //undoStack.push(new AddShape(text));
            annotationToolApplication.commitChange(new AddShape(text));
            annotationToolApplication.getRoot().getChildren().add(text);
            annotationToolApplication.getTextBoxText().delete(0, annotationToolApplication.getTextBoxText().length());
            annotationToolApplication.setSaveTextBox(true);
            annotationToolApplication.getRedoStack().clear();
        }
    }
}
