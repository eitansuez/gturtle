package gturtle

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener

/**
 * User: Eitan Suez
 * Date: Nov 17, 2008
 */
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
