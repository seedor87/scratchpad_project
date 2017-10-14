package eventHandlers;

import annotation.AnnotationToolApplication;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

/**
 * Edits the current text box based on key inputs. should be implemented with KeyEvent.KEY_TYPED
 */
public class TextBoxKeyHandler implements EventHandler<KeyEvent> {
    private AnnotationToolApplication annotationToolApplication;

    public TextBoxKeyHandler(AnnotationToolApplication annotationToolApplication)
    {
        this.annotationToolApplication = annotationToolApplication;
    }

    @Override
    public void handle(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        System.out.println(c);
        Text text = annotationToolApplication.getText();
        StringBuffer textBoxText = annotationToolApplication.getTextBoxText();
        if (((c > 31) && (c < 127))) {
            annotationToolApplication.getTextBoxText().append(c);
            text.setText(textBoxText.toString());
        } else if (c == 8) {
            if (textBoxText.length() > 0) {
                textBoxText.deleteCharAt(textBoxText.length() - 1);
                text.setText(textBoxText.toString());
            }
        } else if (c == 13) {
            textBoxText.append(System.lineSeparator());
            text.setText(textBoxText.toString());
        }
    }
}
