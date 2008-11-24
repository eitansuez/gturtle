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
def flowerhead(size, numpetals)
{
    setpencolor(randomcolor())
    def angle = 360.0/numpetals
    numpetals.times {
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
def flower(size, numpetals)
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

    setpensize((float) (random() * 1.5 + 0.5))
    flowerhead(size, numpetals)
    setheading(90)
}

def shadeofgreen()
{
    def g = (int) (random() * 155 + 100)
    new java.awt.Color(0, g, 0)
}

def steml(size)
{
    setpencolor(shadeofgreen())
    setpensize(3)
    arcl(size, 30)
    rt 30
}
def stemr(size)
{
    setpencolor(shadeofgreen())
    setpensize(3)
    arcr(size, 30)
    lt 30
}

def garden(numflowers)
{
  numflowers.times {
    def size = random() * 180 + 20
    def numpetals = (int) (random() * 15 + 3)
    flower(size, numpetals)
  }
}

clean()
garden(6)
setpos(0,0)
