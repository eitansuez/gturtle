/* smell */

foodpoint = [-150,150]
t = newturtle('bug')

def drawfoodpoint()
{
    t.setPos(foodpoint[0], foodpoint[1])
    t.rt 45
    t.fd 30
    t.bk 60
    t.fd 30
    t.rt 90
    t.fd 30
    t.bk 60
    t.fd 30
}

def distancefromfood()
{
    def w = foodpoint[0] - t.getPos().x
    def h = foodpoint[1] - t.getPos().y
    sqrt(w*w + h*h)
}

def strongersmell(lastdistance)
{
    if (distancefromfood() < lastdistance)
    {
        return true
    }
    else
    {
        return false
    }
}

step = 1
angle = 20

clean()
drawfoodpoint()
t.home()

distance = distancefromfood()
while (distancefromfood() > 10)
{
    t.fd step
    if (!strongersmell(distance))
    {
      t.rt angle
    }
    distance = distancefromfood()
}
