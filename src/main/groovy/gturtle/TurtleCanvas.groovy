package gturtle

import javax.swing.ImageIcon
import javax.swing.JComponent
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Stroke

import static java.awt.Color.white
import static java.awt.RenderingHints.KEY_ANTIALIASING
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON

/**
 * User: Eitan Suez
 * Date: Nov 17, 2008
 */
class TurtleCanvas extends JComponent
{
  static ImageIcon turtlesprite
  static ImageIcon bugsprite
  static {
    ClassLoader loader = Thread.currentThread().contextClassLoader
    URL url = loader.getResource("gturtle/kturtle.png")
    URL bugurl = loader.getResource("gturtle/bug_red.png")
    turtlesprite = new ImageIcon((URL) url)
    bugsprite = new ImageIcon((URL) bugurl)
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

