def poly(n)
{
  def angle = 360/n
  n.times {
    fd 100
    rt angle
  }
}

clean()
def colors = [green, red, blue]
setpensize 2

setpos(-150, -100)
(4..10).each { n ->
  setpencolor colors[n % 3]
  poly n
}
