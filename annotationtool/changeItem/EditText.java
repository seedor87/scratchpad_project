package changeItem;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;



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

    /**
     * @return the Text object that was edited that is represented by this EditText object.
     */
    public Text getText()
    {
        return text;
    }

    /**
     * @param text The text object that was edited in the creation of this EditText.
     * @param annotationToolApplication
     */
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

    /**
     * Creates an EditText based on a text object and a new String to change the Text object to.
     * @param text
     * @param newString
     */
    public EditText(Text text, String newString)
    {
        oldString = text.getText();
        this.text = text;
        this.newString = new StringBuilder(newString);
    }
    public void reset(AnnotationToolApplication annotationToolApplication)
    {
        annotationToolApplication.getPictureStage().removeEventHandler(KeyEvent.KEY_TYPED, editTextKeyHandler);
    }

    /**
     * Changes the text object represented by this edit text to have the contents of the newString.
     * @param annotationToolApplication The AnnotationToolApplication that is having the change added to.
     */
    @Override
    public void addChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        text.setText(newString.toString());
    }

    /**
     * Changes the Text represented by this edit text to have the contents of the oldString.
     * @param annotationToolApplication The AnnotationToolApplication that needs to have the change
     */
    @Override
    public void undoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        text.setText(oldString);
    }

    /**
     * Sets the string of the Text object back to the newString.
     * @param annotationToolApplication The AnnotationToolApplication that needs to have the change redone to
     */
    @Override
    public void redoChangeToStage(AnnotationToolApplication annotationToolApplication)
    {
        text.setText(newString.toString());
    }

    /**
     * This handler will take in key events and append them to the end of the newString.
     * After this, it will then set the Text object's contents represented by this EditText to the
     * newString.
     */
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
