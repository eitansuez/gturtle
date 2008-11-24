clean()


def crazybob(size)
{
  colors = [green, blue, red, yellow, orange]
  setpos(-150,-130)
  setpensize 65
  18.times { i ->
      setpencolor colors[i % 5]
      fd size
      rt 100
  }
}

crazybob(300)