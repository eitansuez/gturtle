clean()
t = newturtle('bug')

def crazybob(size)
{
  colors = [green, blue, red, yellow, orange]
  t.setPos(-150,-130)
  t.setPenSize 65
  18.times { i ->
      t.setPenColor colors[i % 5]
      t.fd size
      t.rt 100
  }
}

crazybob(300)