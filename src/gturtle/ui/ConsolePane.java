package gturtle.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Eitan Suez
 * Date: Nov 19, 2008
 * Time: 3:31:44 PM
 */
public class ConsolePane extends JTextPane
{
   private AbstractDocument doc;

   private SimpleAttributeSet defaultAS;
   private SimpleAttributeSet errorLabelAS;
   private SimpleAttributeSet errorAS;

   public ConsolePane()
   {
      setEditable(false);
      setMargin(new Insets(5, 5, 5, 5));
      
      StyledDocument styledDoc = getStyledDocument();
      doc = (AbstractDocument) styledDoc;
      doc.setDocumentFilter(new DocumentSizeFilter(60000));

      SimpleAttributeSet baseAS = new SimpleAttributeSet();
      StyleConstants.setFontSize(baseAS, 12);
      StyleConstants.setFontFamily(baseAS, "Monospaced");

      defaultAS = new SimpleAttributeSet(baseAS);
      StyleConstants.setForeground(defaultAS, Color.blue);

      errorLabelAS = new SimpleAttributeSet(baseAS);
      StyleConstants.setItalic(errorLabelAS, true);
      StyleConstants.setForeground(errorLabelAS, Color.red);

      errorAS = new SimpleAttributeSet(baseAS);
      StyleConstants.setForeground(errorAS, Color.red);
      StyleConstants.setBackground(errorAS, Color.yellow);

      setPreferredSize(new Dimension(400, 200));
   }

   private void ensureLastLineVisible()
   {
      setCaretPosition(doc.getLength());
   }
   
   public synchronized void normal(String msg)
   {
      if (msg.trim().isEmpty()) return;
      
      try
      {
         String perhapsNewLine = (msg.endsWith("\n")) ? "" : "\n";
         doc.insertString(doc.getLength(), msg + perhapsNewLine, defaultAS);
         ensureLastLineVisible();
      }
      catch (BadLocationException ble) { }
   }

   public synchronized void error(String msg) { error("error", msg); }
   public synchronized void error(String label, String msg)
   {
      if (msg.trim().isEmpty()) return;

      try
      {
         String perhapsNewLine = (msg.endsWith("\n")) ? "" : "\n";
         doc.insertString(doc.getLength(), label + ":  ", errorLabelAS);
         doc.insertString(doc.getLength(), msg + perhapsNewLine, errorAS);
         ensureLastLineVisible();
      }
      catch (BadLocationException ble) { }
   }

   public synchronized void clear()
   {
      setText("");
   }
}

class DocumentSizeFilter extends DocumentFilter
{
   int maxCharacters;

   public DocumentSizeFilter(int maxChars)
   {
      maxCharacters = maxChars;
   }

   public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException
   {
      if (fb.getDocument().getLength() > maxCharacters)
      {
         super.remove(fb, 0, str.length());
      }
      super.insertString(fb, offs, str, a);
   }

   public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException
   {
      if ((fb.getDocument().getLength() + str.length() - length) > maxCharacters)
      {
         super.remove(fb, 0, str.length() - length);
      }
      super.replace(fb, offs, length, str, a);
   }

}
