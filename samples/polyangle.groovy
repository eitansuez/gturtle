def poly(size, angle)
{
  def totalangle = 0
  t.fd size
  t.rt angle
  totalangle += angle
  while (totalangle % 360 != 0)
  {
    t.fd size
    t.rt angle
    totalangle += angle
  }
}

def newpoly(side, angle)
{
  def totalangle = 0;
  t.fd side
  t.rt angle
  t.fd side
  t.rt(2*angle)
  totalangle += 3*angle
  while (totalangle % 360 != 0)
  {
    t.fd side
    t.rt angle
    t.fd side
    t.rt(2*angle)
    totalangle += 3*angle
  }
}

clean()
t = newturtle('bug')
t.setPenColor red
t.setPenSize 2
t.setPos(0,-100)
poly(200, 156)
t.setPenColor blue
t.setPos(-40, 85) // eyeballed..
newpoly(30,125)


