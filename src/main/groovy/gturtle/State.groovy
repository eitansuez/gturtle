package gturtle

import java.awt.Color

import static java.awt.Color.black

/**
 * User: Eitan Suez
 * Date: Nov 17, 2008
 */
class State
{
  TVector v = new TVector(x:0, y:0)
  boolean pendown = true
  Color pencolor = black
  float pensize = 1.0f
}

