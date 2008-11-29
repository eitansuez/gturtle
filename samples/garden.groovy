/* garden */
t = newturtle('bug')

def arcr(size, angle)
{
    angle.times {
        t.fd size/25
        t.rt 1
    }
}
def arcl(size, angle)
{
    angle.times {
        t.fd size/25
        t.lt 1
    }
}

def qcircle(size)
{
    arcr(size, 90)
}


def petal(size)
{
  qcircle(size/7)
  t.rt 90
  qcircle(size/7)
  t.rt 90
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
    t.setPenColor(randomcolor())
    def angle = 360.0/numpetals
    numpetals.times {
        petal(size)
        t.rt angle
    }
}

def pickrandompos()
{
    def x = random() * 400 - 200
    def y = random() * 200 - 250
    t.setPos(x,y)
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

    t.setPenSize((float) (random() * 1.5 + 0.5))
    flowerhead(size, numpetals)
    t.setHeading(90)
}

def shadeofgreen()
{
    def g = (int) (random() * 155 + 100)
    new java.awt.Color(0, g, 0)
}

def steml(size)
{
    t.setPenColor(shadeofgreen())
    t.setPenSize(3)
    arcl(size, 30)
    t.rt 30
}
def stemr(size)
{
    t.setPenColor(shadeofgreen())
    t.setPenSize(3)
    arcr(size, 30)
    t.lt 30
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
t.setPos(0,0)
