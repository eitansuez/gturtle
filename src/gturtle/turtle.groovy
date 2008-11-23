package gturtle

import javax.swing.*
import java.awt.*
import java.awt.event.*
import jsyntaxpane.DefaultSyntaxKit
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent
import gturtle.ui.ConsolePane
import gturtle.ui.ConsolePiper
import static java.awt.Cursor.*
import javax.swing.border.EmptyBorder

/**
 * User: Eitan Suez
 * Date: Nov 17, 2008
 */

class GSwing
{
  static doLater(Closure c)
  {
    SwingUtilities.invokeLater(c as Runnable)
  }
}

class TurtleConsole extends JFrame implements Runnable
{
  static String _appTitle = "Turtle Console"

  static void main(args)
  {
    try
    {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    }
    catch (UnsupportedLookAndFeelException ex)
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    def console = new TurtleConsole()
    SwingUtilities.invokeLater(console)
    if (args.length == 1) console.loadScriptFile(args[0] as File)
  }

  MainPane mainPane

  TurtleConsole() { }

  void run()
  {
    setTitle(_appTitle)
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    mainPane = new MainPane(this)
    setContentPane(mainPane)
    pack()
    setVisible(true)
  }

  void clearTitle()
  {
    setTitle(_appTitle)
  }
  void appendTitle(String text)
  {
    setTitle("${_appTitle} - ${text}")
  }

  void loadScriptFile(File file)
  {
    mainPane.setScriptText(file.getText())
  }
}

class GAction extends AbstractAction
{
  Closure closure
  GAction(String name, int mnemonic, KeyStroke shortCut, Closure closure)
  {
    putValue (Action.NAME, name)
    putValue (Action.MNEMONIC_KEY, mnemonic)
    putValue (Action.ACCELERATOR_KEY, shortCut)
    this.closure = closure
  }

  public void actionPerformed(ActionEvent e)
  {
    closure.call()
  }
}

class MainPane extends JPanel
{
  JTabbedPane editorTabs
  ConsolePane cPane
  TurtleCanvas turtleCanvas
  JSlider speedSlider
  def editorFileMap = [:]

  static String initText = "5.times { fd 100; rt 144 }\n"

  TurtleConsole frame
  JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"))

  MainPane(TurtleConsole container)
  {
    frame = container;
    setupMenuBar()
    setLayout(new BorderLayout())

    editorTabs = new JTabbedPane()
    editorTabs.setPreferredSize(new Dimension(450, 500))

    editorTabs.addChangeListener({ ChangeEvent evt ->
      if (editorTabs.getTabCount() == 0)
      {
        frame.clearTitle()
        return;
      }
      String text = editorTabs.getTitleAt(editorTabs.getSelectedIndex())
      frame.appendTitle(text)
    } as ChangeListener) 
    
    turtleCanvas = new TurtleCanvas()
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorTabs, turtleCanvas)

    cPane = new ConsolePane()
    new ConsolePiper(cPane).pipeStreams()
    JSplitPane topSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, new JScrollPane(cPane))
    add(topSplitPane, BorderLayout.CENTER)

    // work in progress:
    def slidePane = new JPanel()
    speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 1000)
    speedSlider.addChangeListener({ ChangeEvent e ->
      if (speedSlider.getValueIsAdjusting()) return
      int speed = speedSlider.getValue()
      println("slider value is "+speed)
      turtleCanvas.setDelay(1000-speed)
    } as ChangeListener)
    slidePane.add(new JLabel("Speed:"))
    slidePane.add(speedSlider)
//    add(slidePane, BorderLayout.NORTH)
    
    GSwing.doLater {
      setupAboutDlg()
      newFile()
//      setScriptText(initText)
    }
  }

  void execute(String text)
  {
    frame.setCursor(getPredefinedCursor(WAIT_CURSOR))
    new Thread({
      try
      {
        turtleCanvas.execute(text)
      }
      finally
      {
        GSwing.doLater {
          currentScriptEditor().requestFocus()
          frame.setCursor(getPredefinedCursor(DEFAULT_CURSOR))
        }
      }
    }).start()
  }

  void associateFileToEditor(Component c, File f) { editorFileMap[c] = f }
  File fileForEditor(Component c) { editorFileMap[c] }
  void removeAssociation(Component c) { editorFileMap.remove(c) }
  
  def newFile() { addFileTab(null) }

  def addFileTab(File file)
  {
    String title = (file == null) ? "New File" : file.getName()

    JScrollPane container = newScriptEditor()
    editorTabs.addTab(title, container)

    editorTabs.setSelectedComponent(container)

    if (file != null)
    {
      setScriptText(file.getText())
    }

    associateFileToEditor(container, file)
    setFocusOnEditor()
  }

  def setFocusOnEditor()
  {
    GSwing.doLater { currentScriptEditor().requestFocusInWindow() }
  }

  void setupMenuBar()
  {
    def newAction = new GAction("New", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK), this.&newFile)

    def openAction = new GAction("Open", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK), {
      chooser.setDialogTitle("Open")
      int choice = chooser.showOpenDialog(frame)
      if (choice == JFileChooser.APPROVE_OPTION)
      {
        File file = chooser.getSelectedFile()
        addFileTab(file)
      }
    })
    def saveAction = new GAction("Save", KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK), {
      File file = fileForEditor(editorTabs.getSelectedComponent())
      if (file == null)
      {
        chooser.setDialogTitle("Save")
        int choice = chooser.showSaveDialog(frame)
        if (choice == JFileChooser.APPROVE_OPTION)
        {
          file = chooser.getSelectedFile()
        }
        else
        {
          return
        }
      }
      file.write(getScriptText())
    })
    def saveAsAction = new GAction("Save As", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_S,
            KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK), {
      chooser.setDialogTitle("Save As")
      int choice = chooser.showSaveDialog(frame)
      if (choice == JFileChooser.APPROVE_OPTION)
      {
        File file = chooser.getSelectedFile()
        file.write(getScriptText())
        editorTabs.setTitleAt(editorTabs.getSelectedIndex(), file.getName());
        associateFileToEditor(editorTabs.getSelectedComponent(), file)
      }
    })
    def closeAction = new GAction("Close", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK), {
      removeAssociation(editorTabs.getSelectedComponent())
      editorTabs.removeTabAt(editorTabs.getSelectedIndex())
    })
    def quitAction = new GAction("Quit", KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK), {
      System.exit(0)
    })

    JMenu fileMenu = new JMenu("File")
    fileMenu.setMnemonic('F' as char)
    fileMenu.add(newAction)
    fileMenu.add(openAction)
    fileMenu.add(saveAction)
    fileMenu.add(saveAsAction)
    fileMenu.add(closeAction)
    fileMenu.add(quitAction)

    def runScriptAction = new GAction("Run", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK), {
      execute(currentScriptEditor().getText())
    })
    def runSelectedAction = new GAction("Run Selection", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK
            + KeyEvent.SHIFT_MASK), {
      execute(currentScriptEditor().getSelectedText())
    })

    JMenu scriptMenu = new JMenu("Script")
    scriptMenu.setMnemonic('S' as char)
    scriptMenu.add(runScriptAction)
    scriptMenu.add(runSelectedAction)

    def clearConsoleAction = new GAction("Clear Console", KeyEvent.VK_C,
            KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK), {
              cPane.clear()
            })

    JMenu outputMenu = new JMenu("Output")
    outputMenu.setMnemonic('O' as char)
    outputMenu.add(clearConsoleAction)

    def aboutAction = new GAction("About GTurtle", KeyEvent.VK_A, null, {
      aboutDlg.setVisible(true)
            })

    JMenu helpMenu = new JMenu("Help")
    helpMenu.setMnemonic('H' as char)
    helpMenu.add(aboutAction)

    JMenuBar menuBar = new JMenuBar()
    menuBar.add(fileMenu)
    menuBar.add(scriptMenu)
    menuBar.add(outputMenu)
    menuBar.add(helpMenu)
    frame.setJMenuBar(menuBar)
  }

  JDialog aboutDlg
  void setupAboutDlg()
  {
    def builder = new groovy.swing.SwingBuilder()
    aboutDlg = builder.dialog(
            title: "About GTurtle",
            layout: new FlowLayout(),
            defaultCloseOperation : JDialog.HIDE_ON_CLOSE)
            {
              label('GTurtle, by Eitan Suez (eitan.suez@gmail.com)')
              button(text: 'Ok', actionPerformed: {
                aboutDlg.setVisible(false)
                currentScriptEditor().requestFocus()
              })
            }
    ((JComponent) aboutDlg.getContentPane()).setBorder(new EmptyBorder(10,10,10,10))
    aboutDlg.pack()
    aboutDlg.setLocationRelativeTo(frame)
  }


  JEditorPane currentScriptEditor()
  {
    return (JEditorPane) scrollPaneContents((JScrollPane) editorTabs.getSelectedComponent())
  }
  JComponent scrollPaneContents(JScrollPane scrollPane)
  {
    return (JComponent) scrollPane.getViewport().getView()
  }

  void setScriptText(String scriptText) { currentScriptEditor().setText(scriptText) }
  String getScriptText() { currentScriptEditor().getText() }

  // some magical recipe i discovered works for setting up JSyntaxPane
  JScrollPane newScriptEditor()
  {
    JEditorPane scriptEditor = new JEditorPane()
    JScrollPane editorScrollPane = new JScrollPane(scriptEditor)

    scriptEditor.setPreferredSize(new Dimension(450, 400))
    scriptEditor.setCaretColor(Color.black)
    scriptEditor.setContentType("text/groovy")

    DefaultSyntaxKit.initKit()
    scriptEditor.setContentType("text/groovy")
    scriptEditor.setFont(new Font("sansserif", Font.PLAIN, 12))

    return editorScrollPane
  }
}

class State
{
  TVector v = new TVector(x:0, y:0)
  boolean pendown = true
  Color pencolor = Color.black
  float pensize = 1.0f
}


class TurtleCanvas extends JComponent
{

  static ImageIcon turtlesprite;
  static {
    ClassLoader loader = Thread.currentThread().getContextClassLoader()
    URL url = loader.getResource("gturtle/kturtle.png")
    turtlesprite = new ImageIcon(url)
  }


  TVector heading_v
  def states
  State state

  GroovyShell shell

  TurtleCanvas()
  {
    heading_v = new TVector(x: 0, y: 1)
    reset()
    setupShell()
  }

  void reset()
  {
    states = []
    state = new State()
    setPos(0, 0)
    setHeading(90) 
  }

  void setupShell()
  {
    def context = new Binding()
    Map bindings = [
            "fd" : this.&fd,
            "bk" : this.&bk,
            "rt" : this.&rt,
            "lt" : this.&lt,
            "clean" : this.&clean,
            "home" : this.&home,
            
            "setpos" : this.&setPos,
            "pos" : this.&getPos,
            
            "setheading" : this.&setHeading,
            "heading" : this.heading,

            "setpencolor" : this.&setPenColor,
            "pencolor" : this.state.pencolor,

            "setpensize" : this.&setPenSize,
            "pensize" : this.state.pensize,
            
            "setpendown" : this.&setPenDown.curry(true),
            "setpenup" : this.&setPenDown.curry(false),
            "ispendown" : this.state.pendown
    ]
    bindings.each { entry ->
      context.setProperty(entry.key, entry.value)
    }
    shell = new GroovyShell(context)
  }

  void setPenDown(boolean down)
  {
    state.pendown = down
  }
  
  void setPenColor(Color color)
  {
    state.pencolor = color
  }

  void setPenSize(float value)
  {
    state.pensize = value
  }

  boolean delaySet = false
  long delayMs = 0
  void setDelay(long ms)
  {
    delayMs = ms
    delaySet = (ms > 0)
  }

  protected void paintComponent(Graphics g)
  {
    Graphics2D g2 = (Graphics2D) g
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    g2.setColor(Color.white)
    g2.fillRect(0, 0, getWidth()-1, getHeight()-1)
    
    g2.translate(250, 250)
    g2.scale(1.0, -1.0)

    State s1 = states.first()
    states.tail().each { State s2 ->
      if (s2.pendown)
      {
        g2.setColor(s2.pencolor)
        g2.setStroke(new BasicStroke(s2.pensize))
        g2.drawLine((int) s1.v.x, (int) s1.v.y, (int) s2.v.x, (int) s2.v.y)
      }
      s1 = s2
    }

    drawHeadingIndicator(g2)
  }

  Stroke tris = new BasicStroke(1.0f)
  void drawHeadingIndicator(Graphics2D g2)
  {
    double theta = heading_rads()
    TVector lastPos = states.last().v
    g2.translate(lastPos.x, lastPos.y)
    g2.rotate(theta)

    // originally a triangle indicated heading:
//    g2.setColor(Color.black)
//    g2.setStroke(tris)
//    g2.drawLine(0, -6, 0, 6)
//    g2.drawLine(0, 6, 24, 0)
//    g2.drawLine(24, 0, 0, -6)
    // now..
    g2.rotate(Math.PI/2)
    g2.drawImage(turtlesprite.getImage(), (int) (-turtlesprite.getIconWidth()/2.0), (int) (-turtlesprite.getIconHeight()/2.0), null)
    g2.rotate(-Math.PI/2)

    g2.rotate(-theta)
    g2.translate(-lastPos.x, -lastPos.y)
  }


  Dimension preferredSize = new Dimension(500,500)
  Dimension getPreferredSize() { preferredSize; }

  synchronized void execute(String scriptText)
  {
    Date start = new Date();

    // a nicety.. (there's probably another way to tell the script to pre-import packages..)
    scriptText = "import static java.awt.Color.*\n" +
                 "import static java.lang.Math.*\n" +
                 scriptText
    try
    {
      shell.evaluate(scriptText)
    }
    catch (Exception ex)
    {
      def msg = ex.getCause() ? ex.getCause().getMessage() : ex.getMessage()
      System.err.println msg
    }

    Date end = new Date();
    long time_ms = end.getTime() - start.getTime()
    System.out.println "[time: ${time_ms} ms]"
    repaint()
  }

  boolean wrapOn = true
  
  // commands:
  void fd(double distance)
  {
    state.v = states.last().v + (heading_v * distance)

    if (wrapOn)
    {
      wrap(states.last().v, state.v)
    }
    else
    {
      states << new State(v: state.v, pendown: state.pendown, pencolor: state.pencolor, pensize: state.pensize)
    }

    if (states.size() % 100 == 0)
    {
      repaint()
    }
  }

  protected void wrap(TVector v1, TVector v2)
  {
    states << new State(v: v2, pendown: state.pendown, pencolor: state.pencolor, pensize: state.pensize)
    TVector screen = new TVector(x: getWidth(), y: getHeight())
    TVector nextV = (v2.translate(250) % screen).translate(-250)
    if (nextV != v2)
    {
      TVector p = nextV - (v2 - v1)
      setPos(p.x, p.y)
      states << new State(v: nextV, pendown: state.pendown, pencolor: state.pencolor, pensize: state.pensize)
    }
  }

  protected TVector translate
  
  void bk(double distance) { fd(-distance) }
  void lt(double angleDegrees)
  {
    double radians = deg2rad(angleDegrees)
    heading_v = heading_v.rotate(radians)
  }
  void rt(double angle) { lt(-angle) }

  void clean()
  {
    reset()
  }

  void home()
  {
    setPos(0, 0)
    setHeading(90)
  }

  /**
   * setpos implies that you'll be taking the pen up while you "move" to the new position
   */
  void setPos(double x, double y)
  {
    state.v = new TVector(x: x, y: y)
    states << new State(v: state.v, pendown: false, pencolor: state.pencolor, pensize: state.pensize)
    state.pendown = true
  }
  TVector getPos() { states.last().v }

  private double heading_rads()
  {
    Math.atan(heading_v.y/heading_v.x) + ((heading_v.x>=0) ? 0 : Math.PI);
  }
  double getHeading()
  {
    rad2deg(heading_rads())
  }
  void setHeading(double angleDegrees)
  {
    double rads = deg2rad(angleDegrees)
    heading_v = new TVector(x: Math.cos(rads), y: Math.sin(rads))
  }
  // end-commands

  double deg2rad(double deg) { deg * 2 * Math.PI / 360 }
  double rad2deg(double rad) { rad * 360 / (2 * Math.PI) }
}


class TVector
{
  double x, y
  double length() { Math.sqrt(x*x + y*y) }
  TVector plus(TVector v) { new TVector(x: x+v.x, y: y+v.y) }
  TVector minus(TVector v) { new TVector(x: x-v.x, y: y-v.y) }

  TVector mod(TVector v)
  {
    TVector newV = new TVector(x: x % v.x, y: y % v.y)
    if (newV.x < 0) newV.x += v.x  // expect -3 % 10 to return 7, not -3
    if (newV.y < 0) newV.y += v.y
    newV
  }

  TVector multiply(double k) { new TVector(x: k*x, y: k*y) }
  TVector rotate(double angle) { (this * Math.cos(angle)) + (perp() * Math.sin(angle)) }
  TVector perp() { new TVector(x: -y, y: x) }
  TVector translate(double amt) { new TVector(x: x + amt, y: y+amt) }

  double equalsMargin = 0.000001;
  public boolean equals(Object obj)
  {
    if (obj == null || (!obj instanceof TVector)) return false
    TVector other = (TVector) obj
    return Math.abs(x - other.x) < equalsMargin && Math.abs(y - other.y) < equalsMargin
  }
  public int hashCode()
  {
    return x.hashCode() + 31 * y.hashCode()
  }

  public String toString()
  {
    return "{${x}, ${y}}";
  }

}
