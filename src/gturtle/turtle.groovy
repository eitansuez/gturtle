package gturtle

import javax.swing.*
import java.awt.*
import java.awt.event.*
import jsyntaxpane.DefaultSyntaxKit
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent
import gturtle.ui.ConsolePane
import gturtle.ui.ConsolePiper
import javax.swing.border.EmptyBorder
import javax.swing.event.PopupMenuListener
import static java.awt.RenderingHints.*
import static java.awt.Cursor.*
import static java.awt.Color.*
import static javax.swing.JSplitPane.*
import groovy.swing.SwingBuilder

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

  TurtleConsole() { }

  void run()
  {
    title = APP_TITLE
    defaultCloseOperation = JFrame.EXIT_ON_CLOSE
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

class FifoOrderedSet
{
  LinkedList ll
  int size = 5

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

  static int CMD_MASK = java.awt.Toolkit.defaultToolkit.menuShortcutKeyMask
  static String initText = "t = newturtle('bug')\n\n5.times { t.fd 100; t.rt 144 }\n"

  SwingBuilder swing
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
  File settingsFile

  MainPane(TurtleConsole container)
  {
    swing = new SwingBuilder()
    loadSettings()
    frame = container
    setLayout(new BorderLayout())

    editorTabs = swing.tabbedPane(preferredSize: [450, 500])
    editorTabs.addChangeListener({ ChangeEvent evt ->
      if (editorTabs.tabCount == 0)
      {
        frame.clearTitle()
        return
      }
      String text = editorTabs.getTitleAt(editorTabs.getSelectedIndex())
      frame.appendTitle(text)
    } as ChangeListener)

    turtleCanvas = new TurtleCanvas()
    cPane = new ConsolePane()
    
    new ConsolePiper(cPane).pipeStreams()
    JSplitPane topSplitPane = swing.splitPane(orientation: VERTICAL_SPLIT) {
      splitPane(orientation: HORIZONTAL_SPLIT) {
        widget(editorTabs)
        widget(turtleCanvas)
      }
      scrollPane { widget(cPane) }
    }
    
    add(topSplitPane, BorderLayout.CENTER)

    setupMenuBar()

    // work in progress:
//    def slidePane = new JPanel()
//    speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 1000)
//    speedSlider.addChangeListener({ ChangeEvent e ->
//      if (speedSlider.valueIsAdjusting) return
//      int speed = speedSlider.value
//      println("slider value is "+speed)
//      turtleCanvas.delay = 1000-speed
//    } as ChangeListener)
//    slidePane.add(new JLabel("Speed:"))
//    slidePane.add(speedSlider)
//    add(slidePane, BorderLayout.NORTH)
    
    swing.doLater {
      setupAboutDlg()
      newFile()
      setScriptText(initText)
    }
  }

  void addToRecentDocs(File file)
  {
    if (file.exists())
    {
      recentDocs.add(file)
    }
  }


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
    chooser.multiSelectionEnabled = true

    String cwd = settings.getProperty("filesDir") ?: System.getProperty("user.dir")  // ?: means ||=
    chooser.currentDirectory = new File(cwd)

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
    settings.setProperty("filesDir", file.parent)
  }
  void saveSettings()
  {
    (1..recentDocs.size()).each { int i ->
      settings.remove("recentdoc.${i}")
    }
    recentDocs.eachWithIndex { File item, int i ->
      settings.setProperty("recentdoc.${i+1}", item.canonicalPath)
    }
    settingsFile.withWriter { Writer writer ->
      settings.store(writer, "gturtle settings")
    }
  }

  void execute(String text)
  {
    frame.setCursor getPredefinedCursor(WAIT_CURSOR)
    Thread.start {
      try
      {
        turtleCanvas.execute(text)
      }
      finally
      {
        swing.doLater {
          currentScriptEditor()?.requestFocus()
          frame.setCursor getPredefinedCursor(DEFAULT_CURSOR)
        }
      }
    }
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
    String title = (file) ? file.name : "New File"

    JScrollPane container = newScriptEditor()
    editorTabs.addTab(title, container)

    editorTabs.selectedComponent = container

    if (file)
    {
      associateFileToEditor(container, file)
      swing.doLater {
        ((JEditorPane) scrollPaneContents(container)).text = file.text
      }
      addToRecentDocs(file)
    }

    focusOnEditor()
  }

  def focusOnEditor()
  {
    swing.doLater { currentScriptEditor()?.requestFocus() }
  }

  def openFile(File file = null)
  {
    if (file)
    {
      addFileTab(file)
    }
    else
    {
      chooser.dialogTitle = "Open"
      int choice = chooser.showOpenDialog(frame)
      if (choice == JFileChooser.APPROVE_OPTION)
      {
        chooser.selectedFiles.each { File f ->
          rememberChooserDirectory(f)
          addFileTab(f)
        }
      }
    }
  }

  void setupMenuBar()
  {
    def newAction = swing.action(name: "New", mnemonic: 'N', accelerator: swing.shortcut('N'),
            closure: { newFile() })

    def openAction = swing.action(name: "Open", mnemonic: 'O', accelerator: swing.shortcut('O'),
            closure: { openFile() } )
    def saveAction = swing.action(name: "Save", mnemonic: 'S', accelerator: swing.shortcut('S'),
            closure: {
      File file = fileForEditor((JComponent) editorTabs.selectedComponent)
      if (!file)
      {
        chooser.dialogTitle = "Save"
        int choice = chooser.showSaveDialog(frame)
        if (choice == JFileChooser.APPROVE_OPTION)
        {
          file = chooser.selectedFile
          rememberChooserDirectory(file)
        }
        else
        {
          return
        }
      }
      file.write(getScriptText())
    })
    def saveAsAction = swing.action(name: "Save As", mnemonic: 'A', accelerator: swing.shortcut('shift S'),
            closure: {
      chooser.dialogTitle = "Save As"
      int choice = chooser.showSaveDialog(frame)
      if (choice == JFileChooser.APPROVE_OPTION)
      {
        File file = chooser.selectedFile
        file.write(getScriptText())
        editorTabs.setTitleAt(editorTabs.selectedIndex, file.name)
        associateFileToEditor((JComponent) editorTabs.selectedComponent, file)
        rememberChooserDirectory(file)
      }
    })
    def closeAction = swing.action(name: "Close", mnemonic: 'C', accelerator: swing.shortcut('W'),
            closure: {
              removeAssociation((JComponent) editorTabs.selectedComponent)
              editorTabs.removeTabAt(editorTabs.selectedIndex)
            })
    def quitAction = swing.action(name: "Exit", mnemonic: 'x', accelerator: swing.shortcut('Q'),
            closure: {
              saveSettings()
              System.exit(0)
            })

    recentDocsSubMenu = swing.menu('Recent Documents', mnemonic: 'D')
    // this is kind of wasteful.  but it's simple.
    recentDocsSubMenu.popupMenu.addPopupMenuListener([
            popupMenuWillBecomeVisible : { rebuildRecentDocsSubMenu() },
            popupMenuWillBecomeInvisible : {},
            popupMenuCanceled : {}
         ] as PopupMenuListener)

    def runScriptAction = swing.action(name: "Run", mnemonic: 'R', accelerator: swing.shortcut('R'),
            closure: { execute(currentScriptEditor().text) })
    
    def runSelectedAction = swing.action(name: "Run Selection", mnemonic: 'e', accelerator:  swing.shortcut('shift R'),
            closure: { execute(currentScriptEditor().selectedText) })

    def decreaseFontAction = swing.action(name: "Decrease font", mnemonic: 'D',
            accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, CMD_MASK),
            closure: { changeFontSize(false) })
    def increaseFontAction = swing.action(name: "Increase font", mnemonic: 'I',
            accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, CMD_MASK),
            closure: { changeFontSize(true) })

    def clearConsoleAction = swing.action(name: "Clear Console", mnemonic: 'C', accelerator:  swing.shortcut('K'),
            closure: { cPane.clear() })

    def newShellAction = swing.action(name:  "New Shell", mnemonic: 'N', closure: { turtleCanvas.newShell() })

    def aboutAction = swing.action(name: "About GTurtle", mnemonic: 'A', closure: { aboutDlg.visible = true })

    frame.JMenuBar = swing.menuBar {
      menu(text: 'File', mnemonic: 'F') {
        menuItem(newAction)
        menuItem(openAction)
        menu(recentDocsSubMenu)
        menuItem(saveAction)
        menuItem(saveAsAction)
        menuItem(closeAction)
        separator()
        menuItem(quitAction)
      }
      menu(text: 'Script', mnemonic: 'S') {
        menuItem(runScriptAction)
        menuItem(runSelectedAction)
        menuItem(decreaseFontAction)
        menuItem(increaseFontAction)
      }
      menu(text: 'Output', mnemonic: 'O') {
        menuItem(clearConsoleAction)
      }
      menu(text: 'Debug', mnemonic: 'D') {
        menuItem(newShellAction)
      }
      menu(text: 'Help', mnemonic: 'H') {
        menuItem(aboutAction)
      }
    }
  }

  void rebuildRecentDocsSubMenu()
  {
    recentDocsSubMenu.removeAll()
    recentDocs.eachWithIndex { File file, int i ->
      def action = swing.action(name: "${i+1}. ${file.name}".toString(),
              mnemonic: KeyEvent.VK_1+i, closure: { openFile(file) })
      recentDocsSubMenu.add(action)
    }
  }

  void changeFontSize(boolean up)
  {
    JEditorPane scriptEditor = currentScriptEditor()
    Font currentFont = scriptEditor.font
    float newSize = currentFont.size + (up ? 1 : -1) 
    Font font = currentFont.deriveFont(newSize)
    scriptEditor.font = font
    revalidate(); repaint();
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
                aboutDlg.visible = false
                focusOnEditor()
              })
            }
    ((JComponent) aboutDlg.contentPane).border = new EmptyBorder(10,10,10,10)
    aboutDlg.pack()
    aboutDlg.locationRelativeTo = frame
  }


  JEditorPane currentScriptEditor()
  {
    return (JEditorPane) scrollPaneContents((JScrollPane) editorTabs.selectedComponent)
  }
  JComponent scrollPaneContents(JScrollPane scrollPane)
  {
    return (JComponent) scrollPane.viewport.view
  }

  void setScriptText(String scriptText)
  {
    swing.doLater { currentScriptEditor().text = scriptText }
  }

  String getScriptText() { currentScriptEditor().text }

  // some magical recipe i discovered works for setting up JSyntaxPane
  JScrollPane newScriptEditor()
  {
    JEditorPane scriptEditor = new JEditorPane()
    JScrollPane editorScrollPane = new JScrollPane(scriptEditor)

    scriptEditor.preferredSize = new Dimension(450, 400)
    scriptEditor.caretColor = black
    scriptEditor.contentType = "text/groovy"

    DefaultSyntaxKit.initKit()
    scriptEditor.contentType = "text/groovy"
    scriptEditor.font = new Font("sansserif", Font.PLAIN, 12)

    return editorScrollPane
  }
}

class State
{
  TVector v = new TVector(x:0, y:0)
  boolean pendown = true
  Color pencolor = black
  float pensize = 1.0f
}

class Turtle implements MouseListener, MouseMotionListener
{
  TVector heading_v
  def states
  State state
  ImageIcon sprite
  TurtleCanvas canvas

  Turtle(ImageIcon sprite, TurtleCanvas canvas)
  {
    this.sprite = sprite
    this.canvas = canvas
    heading_v = new TVector(x: 0, y: 1)
    reset()
    setupTurtleDraggable()
  }

  void reset()
  {
    states = []
    state = new State()
    setPos(0, 0)
    setHeading(90)
  }

  void setupTurtleDraggable()
  {
    canvas.addMouseListener(this)
    canvas.addMouseMotionListener(this)
  }

  boolean indragmode = false
  public void mouseClicked(MouseEvent e) { }
  public void mousePressed(MouseEvent e)
  {
    int w = sprite.iconWidth
    int h = sprite.iconHeight
    TVector pos = getPos()
    Point translated = new Point((int) (pos.x+250), (int) (-pos.y+250))
    Rectangle spriteSpace = new Rectangle((int) (translated.x-w/2), (int) (translated.y-h/2), w, h)
    if (spriteSpace.contains(e.point))
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
      Point converted = new Point((int) (e.x-250), (int) ((e.y-250)*-1))
      setPos(converted.x, converted.y)
      canvas.repaint()
    }
  }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseMoved(MouseEvent e) { }

  boolean wrapOn = true
  void setWrapOn(boolean on)
  {
    this.wrapOn = on
  }
  void setPenDown(boolean down)
  {
    state.pendown = down
//    System.out.println("[ok, pen is ${state.pendown ? 'down' : 'up'}]")
  }

  void setPenColor(Color color)
  {
    state.pencolor = color
//    System.out.println("[ok, pencolor is: ${state.pencolor}]")
  }

  void setPenSize(float value)
  {
    state.pensize = value
//    System.out.println("[ok, pensize is: ${state.pensize}]")
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
      canvas.repaint()
    }
  }

  protected void wrap(TVector v1, TVector v2)
  {
    states << new State(v: v2, pendown: state.pendown, pencolor: state.pencolor, pensize: state.pensize)
    TVector screen = new TVector(x: canvas.width, y: canvas.height)
    TVector nextV = (v2.translate(250) % screen).translate(-250)
    if (nextV != v2)
    {
      TVector p = nextV - (v2 - v1)
      setPos(p.x, p.y)
      states << new State(v: nextV, pendown: state.pendown, pencolor: state.pencolor, pensize: state.pensize)
    }
  }

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
    Math.atan(heading_v.y/heading_v.x) + ((heading_v.x>=0) ? 0 : Math.PI)
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

  double deg2rad(double deg) { deg * 2 * Math.PI / 360 }
  double rad2deg(double rad) { rad * 360 / (2 * Math.PI) }

  def draw(Graphics2D g2)
  {
    State s1 = states.first()
    states.tail().each { State s2 ->
      if (s2.pendown)
      {
        g2.color = s2.pencolor
        g2.stroke = new BasicStroke(s2.pensize)
        g2.drawLine((int) s1.v.x, (int) s1.v.y, (int) s2.v.x, (int) s2.v.y)
      }
      s1 = s2
    }

    drawHeadingIndicator(g2)
  }

  void drawHeadingIndicator(Graphics2D g2)
  {
    double theta = heading_rads()
    TVector lastPos = states.last().v
    g2.translate(lastPos.x, lastPos.y)
    g2.rotate(theta)

    g2.rotate(Math.PI/2)
    g2.drawImage(sprite.image,
            (int) (-sprite.iconWidth/2.0), (int) (-sprite.iconHeight/2.0), null)
    g2.rotate(-Math.PI/2)

    g2.rotate(-theta)
    g2.translate(-lastPos.x, -lastPos.y)
  }

}


class TurtleCanvas extends JComponent
{
  static ImageIcon turtlesprite
  static ImageIcon bugsprite
  static {
    ClassLoader loader = Thread.currentThread().contextClassLoader
    URL url = loader.getResource("gturtle/kturtle.png")
    URL bugurl = loader.getResource("gturtle/bug_red.png")
    turtlesprite = new ImageIcon(url)
    bugsprite = new ImageIcon(bugurl)
  }
  def iconmap = ['turtle' : turtlesprite, 'bug' : bugsprite]

  GroovyShell shell
  def turtles = []

  TurtleCanvas()
  {
    newShell()
  }

  Turtle newturtle(String kind)
  {
    def t = new Turtle(iconmap[kind], this)
    turtles << t
    t
  }

  void newShell()
  {
    def context = new Binding()
    Map bindings = [
        "show" : this.&show,
        "setgridon" : this.&setGridOn.curry(true),
        "setgridoff" : this.&setGridOn.curry(false),
        "newturtle" : this.&newturtle,
        "clean" : this.&clean

    ]
    bindings.each { entry ->
      context.setProperty(entry.key, entry.value)
    }
    shell = new GroovyShell(context)
    System.out.println("[new groovy shell created]")
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

    g2.color = white
    g2.fillRect(0, 0, getWidth()-1, getHeight()-1)
    
    g2.translate(250, 250)
    g2.scale(1.0, -1.0)

    if (gridOn)
    {
      drawCoordinateGrid(g2)
    }

    turtles.each { turtle ->
      turtle.draw(g2)
    }
  }

  void clean()
  {
    turtles.each { turtle ->
      turtle.clean()
    }
  }

  boolean gridOn = true
  void setGridOn(boolean on)
  {
    this.gridOn = on
  }

  Stroke thinStroke = new BasicStroke(0.5f)
  Stroke minorStroke = new BasicStroke(1.0f)
  
  void drawCoordinateGrid(Graphics2D g2)
  {
    Color lineColor = new Color(0x85, 0x96, 0xCB, 0x80)
    Color axisColor = new Color(0xCD, 0x51, 0x5C, 0x80)
    int minorStep = 50
    (-240..240).step(10, { int i ->
      g2.color = (i==0) ? axisColor : lineColor
      boolean minor = (i % minorStep == 0)
      g2.stroke = minor ? minorStroke : thinStroke
      g2.drawLine(i, -250, i, 250)
      g2.drawLine(-250, i, 250, i)
    })
  }

  Dimension preferredSize = new Dimension(500,500)
  Dimension getPreferredSize() { preferredSize }

  synchronized void execute(String scriptText)
  {
    turtles = []
    Date start = new Date()

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
      def msg = ex.cause ? ex.cause.getMessage() : ex.getMessage()
      System.err.println msg
    }

    Date end = new Date()
    long time_ms = end.time - start.time
    System.out.println "[time: ${time_ms} ms]"
    repaint()
  }

  void show(Object o)
  {
    System.out.println o.toString()
  }
  
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

  double equalsMargin = 0.000001
  public boolean equals(Object obj)
  {
    if (!obj || (!obj instanceof TVector)) return false
    TVector other = (TVector) obj
    return Math.abs(x - other.x) < equalsMargin && Math.abs(y - other.y) < equalsMargin
  }
  public int hashCode()
  {
    return x.hashCode() + 31 * y.hashCode()
  }

  public String toString()
  {
    return "{${x}, ${y}}"
  }

}
