def poly(n)
{
  def angle = 360/n
  n.times {
    t.fd 100
    t.rt angle
  }
}

clean()
t = newturtle('bug')
def colors = [green, red, blue]
t.setPenSize 2

t.setPos(-150, -100)
(4..10).each { n ->
  t.setPenColor colors[n % 3]
  poly n
}
