
def poly(n)
{
   def angle = 360 / n
   n.times {
       fd 100
       rt angle
   }
}

def square()
{
    poly(4)
}

def pentagon()
{
    poly(5)
}


def polyroll(closure, angle)
{
    def totalangle = 0
    
    closure.call()
    rt angle
    totalangle += angle
    
    while (totalangle % 360 != 0)
    {
        closure.call()
        rt angle
        totalangle += angle
    }
}

clean()
setpencolor blue

// polyroll(this.&square, 30)
polyroll(this.&pentagon, 45)


