package gturtle

import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * User: Eitan Suez
 * Date: Nov 17, 2008
 */
class TurtleConsole extends JFrame implements Runnable
{
  static String APP_TITLE = "Turtle Console"

  static void main(args)
  {
    try
    {
      UIManager.setLookAndFeel "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
    }
    catch (Exception ex)
    {
      UIManager.setLookAndFeel UIManager.systemLookAndFeelClassName
    }

    def console = new TurtleConsole()
    SwingUtilities.invokeLater(console)
    if (args.length == 1) console.loadScriptFile(args[0] as File)
  }

  MainPane mainPane

  void run()
  {
    title = APP_TITLE
    defaultCloseOperation = EXIT_ON_CLOSE
    mainPane = new MainPane(this)
    contentPane = mainPane
    pack()
    setVisible(true)
  }

  void clearTitle()
  {
    title = APP_TITLE
  }
  void appendTitle(String text)
  {
    title = "${APP_TITLE} - ${text}"
  }

  void loadScriptFile(File file)
  {
    mainPane.scriptText = file.text
  }
}
