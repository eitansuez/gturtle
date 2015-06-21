package gturtle

import groovy.swing.SwingBuilder
import gturtle.ui.ConsolePane
import gturtle.ui.ConsolePiper
import jsyntaxpane.DefaultSyntaxKit

import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JEditorPane
import javax.swing.JFileChooser
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSlider
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.KeyStroke
import javax.swing.border.EmptyBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.PopupMenuListener
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.KeyEvent

import static java.awt.Color.black
import static java.awt.Cursor.DEFAULT_CURSOR
import static java.awt.Cursor.WAIT_CURSOR
import static java.awt.Cursor.getPredefinedCursor
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT
import static javax.swing.JSplitPane.VERTICAL_SPLIT

/**
 * User: Eitan Suez
 * Date: Nov 17, 2008
 */
class MainPane extends JPanel
{
  static int CMD_MASK = Toolkit.defaultToolkit.menuShortcutKeyMask
  static String initText = """
t = newturtle('bug')

t.setPenColor blue
t.setPenSize 5

5.times { t.fd 100; t.rt 144 }

t.setPos(-100,0)
"""

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
    def builder = new SwingBuilder()
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
