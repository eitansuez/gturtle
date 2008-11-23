/* garden */

def arcr(size, angle)
{
    angle.times {
        fd size/25
        rt 1
    }
}
def arcl(size, angle)
{
    angle.times {
        fd size/25
        lt 1
    }
}

def qcircle(size)
{
    arcr(size, 90)
}


def petal(size)
{
  qcircle(size/7)
  rt 90
  qcircle(size/7)
  rt 90
}
def randomcolor()
{
    int red = (int) (random()*255)
    int green = (int) (random()*255)
    int  blue = (int) (random() * 255)
    new java.awt.Color(red, green, blue)
}
def flowerhead(size, angle)
{
    setpencolor(randomcolor())
    int numtimes = 360/angle + 1
    numtimes.times {
        petal(size)
        rt angle
    }
}

def pickrandompos()
{
    def x = random() * 400 - 200
    def y = random() * 200 - 250
    setpos(x,y)
}
def flower(size, angle)
{
    pickrandompos()
    int randnum = random() * 2
    if (randnum % 2 ==0)
    {
        stemr(size)
    }
    else
    {
      steml(size)
    }

    setpensize(2)
    flowerhead(size, angle)
    setheading(90)
}

def steml(size)
{
    setpencolor(green)
    setpensize(3)
    arcl(size, 30)
    rt 30
}
def stemr(size)
{
    setpencolor(green)
    setpensize(3)
    arcr(size, 30)
    lt 30
}

def garden(number)
{
  number.times {
    def size = random() * 180 + 20
    def angle = random() * 100 + 7
    flower(size, angle)
  }
}

clean()
garden(6)
setpos(0,0)
