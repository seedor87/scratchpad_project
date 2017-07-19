package changeItem;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import java.security.Key;


/**
 * Created by Brennan Ringel on 7/5/2017.
 * A subclass of ChangeItem that represents text being changed.  This is created every time a MoveShape is
 * committed with the shape that is moved being text.
 */
public class EditText implements ChangeItem
{
    private String oldString;
    private StringBuilder newString;
    private Text text;
    private EditTextKeyHandler editTextKeyHandler = new EditTextKeyHandler();
    private static EditText lastEditText;

    public EditText(Text text, AnnotationToolApplication annotationToolApplication)
    {
        if(lastEditText != null)
        {
            lastEditText.reset(annotationToolApplication);
        }
        oldString = text.getText();
        this.text = text;
        newString = new StringBuilder(64);
        newString.append(oldString);
        annotationToolApplication.getPictureStage().addEventHandler(KeyEvent.KEY_TYPED, editTextKeyHandler);
        annotationToolApplication.getPictureStage().requestFocus();
        lastEditText = this;
    }
    public EditText(Text text, String newString)
    {
        oldString = text.getText();
        this.text = text;
        text.setText(newString);
    }
    public void reset(AnnotationToolApplication annotationToolApplication)
    {
        annotationToolApplication.getPictureStage().removeEventHandler(KeyEvent.KEY_TYPED, editTextKeyHandler);
    }

    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        text.setText(newString.toString());
    }

    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        text.setText(oldString);
    }

    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        text.setText(newString.toString());
    }
    private class EditTextKeyHandler implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent event)
        {
            char c = event.getCharacter().charAt(0);
            if((( c > 31)&&(c < 127)))
            {
                newString.append(c);
                text.setText(newString.toString());
            }
            else if(c == 8)
            {
                if(newString.length() > 0)
                {
                    newString.deleteCharAt(newString.length()-1);
                    text.setText(newString.toString());
                }
            }
            else if(c == 13)
            {
                newString.append(System.lineSeparator());
                text.setText(newString.toString());
            }
        }
    }
}
