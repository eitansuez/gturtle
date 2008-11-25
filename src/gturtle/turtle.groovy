package gturtle

import javax.swing.*
import java.awt.*
import static java.awt.RenderingHints.*
import java.awt.event.*
import jsyntaxpane.DefaultSyntaxKit
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent
import gturtle.ui.ConsolePane
import gturtle.ui.ConsolePiper
import static java.awt.Cursor.*
import javax.swing.border.EmptyBorder
import javax.swing.event.PopupMenuListener

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

class FifoOrderedSet
{
  LinkedList ll
  int size = 5;

  FifoOrderedSet(int size)
  {
    ll = new LinkedList()
    this.size = size
  }

  void add(item)
  {
    if (ll.contains(item))
    {
      ll.remove(item)
    }
    ll.addFirst(item) 
    if (ll.size() > this.size)
    {
      ll.removeLast()
    }
  }
  void clear() { ll.clear() }
  void remove(item) { ll.remove(item) }
  void each(Closure closure) { ll.each closure }
  void eachWithIndex(Closure closure) { ll.eachWithIndex closure }
  int size() { ll.size() }
  def get(int i) { ll.get(i) }
}

class MainPane extends JPanel
{
  JTabbedPane editorTabs
  ConsolePane cPane
  TurtleCanvas turtleCanvas
  JSlider speedSlider
  JDialog aboutDlg
  TurtleConsole frame

  Properties settings
  JFileChooser chooser
  FifoOrderedSet recentDocs
  JMenu recentDocsSubMenu

  static String initText = "5.times { fd 100; rt 144 }\n"

  void addToRecentDocs(File file)
  {
    if (file.exists())
    {
      recentDocs.add(file)
    }
  }

  File settingsFile

  void loadSettings()
  {
    settingsFile = new File(System.getProperty("user.home"), ".gturtle")
    if (!settingsFile.exists())
    {
      settingsFile.createNewFile()
    }
    settings = new Properties()
    settingsFile.withReader {Reader reader ->
      settings.load(reader)
    }
    chooser = new JFileChooser()
    String cwd = settings.getProperty("filesDir")
    if (cwd == null)
    {
      cwd = System.getProperty("user.dir")
    }
    chooser.setCurrentDirectory(new File(cwd))

    recentDocs = new FifoOrderedSet(4)
    int i = 1
    String filePath = settings.getProperty("recentdoc.${i}")
    while (filePath != null)
    {
      addToRecentDocs(new File(filePath))
      i++
      filePath = settings.getProperty("recentdoc.${i}")
    }
  }

  void rememberChooserDirectory(File file)
  {
    settings.setProperty("filesDir", file.getParent())
  }
  void saveSettings()
  {
    (1..recentDocs.size()).each { int i ->
      settings.remove("recentdoc.${i}")
    }
    recentDocs.eachWithIndex { File item, int i ->
      settings.setProperty("recentdoc.${i+1}", item.getCanonicalPath())
    }
    settingsFile.withWriter { Writer writer ->
      settings.store(writer, "gturtle settings")
    }
  }

  MainPane(TurtleConsole container)
  {
    loadSettings()
    frame = container;
    setLayout(new BorderLayout())

    setupEditorTabs()
    turtleCanvas = new TurtleCanvas()
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorTabs, turtleCanvas)

    cPane = new ConsolePane()
    new ConsolePiper(cPane).pipeStreams()
    JSplitPane topSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, new JScrollPane(cPane))
    add(topSplitPane, BorderLayout.CENTER)

    setupMenuBar()

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
      setScriptText(initText)
    }
  }

  void setupEditorTabs()
  {
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
          currentScriptEditor().requestFocusInWindow()
          frame.setCursor(getPredefinedCursor(DEFAULT_CURSOR))
        }
      }
    }).start()
  }

  static String filepropertykey = "backingfile"
  void associateFileToEditor(JComponent c, File f)
  {
    c.putClientProperty(filepropertykey, f)
  }
  File fileForEditor(JComponent c)
  {
    (File) c.getClientProperty(filepropertykey)
  }
  void removeAssociation(JComponent c)
  {
    c.putClientProperty(filepropertykey, null)
  }

  def newFile() { addFileTab(null) }

  def addFileTab(File file)
  {
    String title = (file == null) ? "New File" : file.getName()

    JScrollPane container = newScriptEditor()
    editorTabs.addTab(title, container)

    editorTabs.setSelectedComponent(container)

    if (file != null)
    {
      associateFileToEditor(container, file)
      setScriptText(file.getText())
      addToRecentDocs(file) 
    }

    setFocusOnEditor()
  }

  def setFocusOnEditor()
  {
    GSwing.doLater { currentScriptEditor().requestFocusInWindow() }
  }

  def openFile(File file = null)
  {
    if (file == null)
    {
      chooser.setDialogTitle("Open")
      int choice = chooser.showOpenDialog(frame)
      if (choice == JFileChooser.APPROVE_OPTION)
      {
        file = chooser.getSelectedFile()
        rememberChooserDirectory(file)
        addFileTab(file)
      }
    }
    else
    {
      addFileTab(file)
    }
  }

  void setupMenuBar()
  {
    def newAction = new GAction("New", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK), this.&newFile)

    def openAction = new GAction("Open", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK), this.&openFile)
    def saveAction = new GAction("Save", KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK), {
      File file = fileForEditor((JComponent) editorTabs.getSelectedComponent())
      if (file == null)
      {
        chooser.setDialogTitle("Save")
        int choice = chooser.showSaveDialog(frame)
        if (choice == JFileChooser.APPROVE_OPTION)
        {
          file = chooser.getSelectedFile()
          rememberChooserDirectory(file)
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
        associateFileToEditor((JComponent) editorTabs.getSelectedComponent(), file)
        rememberChooserDirectory(file)
      }
    })
    def closeAction = new GAction("Close", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK), {
      removeAssociation((JComponent) editorTabs.getSelectedComponent())
      editorTabs.removeTabAt(editorTabs.getSelectedIndex())
    })
    def quitAction = new GAction("Exit", KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK), {
      saveSettings()
      System.exit(0)
    })

    recentDocsSubMenu = new JMenu("Recent Documents")
    recentDocsSubMenu.setMnemonic('D' as char)
    // this is kind of wasteful.  but it's simple.
    recentDocsSubMenu.getPopupMenu().addPopupMenuListener([
            popupMenuWillBecomeVisible : { rebuildRecentDocsSubMenu() },
            popupMenuWillBecomeInvisible : {},
            popupMenuCanceled : {}
         ] as PopupMenuListener)

    JMenu fileMenu = new JMenu("File")
    fileMenu.setMnemonic('F' as char)
    fileMenu.add(newAction)
    fileMenu.add(openAction)
    fileMenu.add(recentDocsSubMenu)
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

    def decreaseFontAction = new GAction("Decrease font", KeyEvent.VK_I,
      KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_MASK), this.&changeFontSize.curry(false))
    def increaseFontAction = new GAction("Increase font", KeyEvent.VK_D,
      KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_MASK), this.&changeFontSize.curry(true))

    JMenu scriptMenu = new JMenu("Script")
    scriptMenu.setMnemonic('S' as char)
    scriptMenu.add(runScriptAction)
    scriptMenu.add(runSelectedAction)
    scriptMenu.add(decreaseFontAction)
    scriptMenu.add(increaseFontAction)

    def clearConsoleAction = new GAction("Clear Console", KeyEvent.VK_C,
            KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK), {
              cPane.clear()
            })

    JMenu outputMenu = new JMenu("Output")
    outputMenu.setMnemonic('O' as char)
    outputMenu.add(clearConsoleAction)

    def newShellAction = new GAction("New Shell", KeyEvent.VK_N, null, turtleCanvas.&newShell)

    JMenu debugMenu = new JMenu("Debug")
    debugMenu.setMnemonic('D' as char)
    debugMenu.add(newShellAction)
    
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
    menuBar.add(debugMenu)
    menuBar.add(helpMenu)
    frame.setJMenuBar(menuBar)
  }

  void rebuildRecentDocsSubMenu()
  {
    recentDocsSubMenu.removeAll()
    recentDocs.eachWithIndex { File file, int i ->
      def action = new GAction("${i+1}. ${file.getName()}", KeyEvent.VK_1+i, null, this.&openFile.curry(file))
      recentDocsSubMenu.add(action)
    }
  }

  void changeFontSize(boolean up)
  {
    Font currentFont = currentScriptEditor().getFont()
    float newSize = currentFont.size + (up ? 1 : -1) 
    Font font = currentFont.deriveFont(newSize)
    currentScriptEditor().setFont(font)
  }

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
                currentScriptEditor().requestFocusInWindow()
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

  void setScriptText(String scriptText)
  {
    GSwing.doLater { currentScriptEditor().setText(scriptText) }
  }

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


class TurtleCanvas extends JComponent implements MouseListener, MouseMotionListener
{

  static ImageIcon turtlesprite, bugsprite;
  static {
    ClassLoader loader = Thread.currentThread().getContextClassLoader()
    URL url = loader.getResource("gturtle/kturtle.png")
    URL bugurl = loader.getResource("gturtle/bug_red.png")
    turtlesprite = new ImageIcon(url)
    bugsprite = new ImageIcon(bugurl)
  }


  TVector heading_v
  def states
  State state

  GroovyShell shell

  TurtleCanvas()
  {
    heading_v = new TVector(x: 0, y: 1)
    reset()
    newShell()
    setupTurtleDraggable()
  }

  void setupTurtleDraggable()
  {
    addMouseListener(this)
    addMouseMotionListener(this) 
  }

  boolean indragmode = false
  public void mouseClicked(MouseEvent e) { }
  public void mousePressed(MouseEvent e)
  {
    int w = bugsprite.getIconWidth()
    int h = bugsprite.getIconHeight()
    TVector pos = getPos();
    Point translated = new Point((int) (pos.x+250), (int) (-pos.y+250))
    Rectangle spriteSpace = new Rectangle((int) (translated.x-w/2), (int) (translated.y-h/2), w, h)
    if (spriteSpace.contains(e.getPoint()))
    {
      indragmode = true
    }
  }
  public void mouseReleased(MouseEvent e)
  {
    indragmode = false
  }
  public void mouseDragged(MouseEvent e)
  {
    if (indragmode)
    {
      Point converted = new Point((int) (e.getX()-250), (int) ((e.getY()-250)*-1))
      setPos(converted.x, converted.y)
      repaint()
    }
  }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseMoved(MouseEvent e) { }



  void reset()
  {
    states = []
    state = new State()
    setPos(0, 0)
    setHeading(90) 
  }

  void newShell()
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
            "ispendown" : this.state.pendown,

            "show" : this.&show,
            "setgridon" : this.&setGridOn.curry(true),
            "setgridoff" : this.&setGridOn.curry(false),

            "setwrapon" : this.&setWrapOn.curry(true),
            "setwrapoff" : this.&setWrapOn.curry(false)
    ]
    bindings.each { entry ->
      context.setProperty(entry.key, entry.value)
    }
    shell = new GroovyShell(context)
    System.out.println("[new groovy shell created]")
  }

  void setPenDown(boolean down)
  {
    state.pendown = down
    System.out.println("[ok, pen is ${state.pendown ? 'down' : 'up'}]")
  }
  
  void setPenColor(Color color)
  {
    state.pencolor = color
    System.out.println("[ok, pencolor is: ${state.pencolor}]")
  }

  void setPenSize(float value)
  {
    state.pensize = value
    System.out.println("[ok, pensize is: ${state.pensize}]")
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
    g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)

    g2.setColor(Color.white)
    g2.fillRect(0, 0, getWidth()-1, getHeight()-1)
    
    g2.translate(250, 250)
    g2.scale(1.0, -1.0)

    if(gridOn)
    {
      drawCoordinateGrid(g2)
    }
    
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

  boolean gridOn = true
  void setGridOn(boolean on)
  {
    this.gridOn = on
    repaint()
  }

  Stroke thinStroke = new BasicStroke(0.5f)
  Stroke minorStroke = new BasicStroke(1.0f)
  
  void drawCoordinateGrid(Graphics2D g2)
  {
    Color lineColor = new Color(0x85, 0x96, 0xCB, 0x80)
    Color axisColor = new Color(0xCD, 0x51, 0x5C, 0x80)
    int minorStep = 50
    (-240..240).step(10, { int i ->
      if (i==0)
      {
        g2.setColor(axisColor)
      }
      else
      {
        g2.setColor(lineColor)
      }
      boolean minor = (i % minorStep == 0)
      g2.setStroke(minor ? minorStroke : thinStroke)
      g2.drawLine(i, -250, i, 250)
      g2.drawLine(-250, i, 250, i)
    })
  }

  void drawHeadingIndicator(Graphics2D g2)
  {
    double theta = heading_rads()
    TVector lastPos = states.last().v
    g2.translate(lastPos.x, lastPos.y)
    g2.rotate(theta)

    g2.rotate(Math.PI/2)
//    g2.drawImage(turtlesprite.getImage(), (int) (-turtlesprite.getIconWidth()/2.0), (int) (-turtlesprite.getIconHeight()/2.0), null)
    g2.drawImage(bugsprite.getImage(), (int) (-bugsprite.getIconWidth()/2.0), (int) (-bugsprite.getIconHeight()/2.0), null)
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
  void setWrapOn(boolean on)
  {
    this.wrapOn = on
  }

  // commands:
  void show(Object o)
  {
    System.out.println o.toString()
  }
  
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
