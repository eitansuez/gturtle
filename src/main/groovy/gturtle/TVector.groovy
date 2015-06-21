package gturtle

/**
 * User: Eitan Suez
 * Date: Nov 17, 2008
 */
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
