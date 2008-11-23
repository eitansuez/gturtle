/* smell */

foodpoint = [-150,-50]

def drawfoodpoint()
{
    setpos(foodpoint[0], foodpoint[1])
    rt 45
    fd 30
    bk 60
    fd 30
    rt 90
    fd 30
    bk 60
    fd 30
}

def distancefromfood()
{
    def w = foodpoint[0] - pos().x
    def h = foodpoint[1] - pos().y
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
angle = 100

clean()
drawfoodpoint()
home()

distance = distancefromfood()
while (distancefromfood() > 10)
{
    fd step
    if (!strongersmell(distance))
    {
      rt angle
    }
    distance = distancefromfood()
}
