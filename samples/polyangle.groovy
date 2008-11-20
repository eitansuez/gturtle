def poly(size, angle)
{
  def totalangle = 0
  fd size
  rt angle
  totalangle += angle
  while (totalangle % 360 != 0)
  {
    fd size
    rt angle
    totalangle += angle
  }
}


def newpoly(side, angle)
{
  def totalangle = 0;
  fd side
  rt angle
  fd side
  rt(2*angle)
  totalangle += 3*angle
  while (totalangle % 360 != 0)
  {
    fd side
    rt angle
    fd side
    rt(2*angle)
    totalangle += 3*angle
  }
}

clean()
setpencolor red
setpensize 2
setpos(0,-100)
poly(200, 156)
setpencolor blue
setpos(-40, 85) // eyeballed..
newpoly(30,125)


