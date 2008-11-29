
def poly(n)
{
   def angle = 360 / n
   n.times {
       t.fd 100
       t.rt angle
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
    t.rt angle
    totalangle += angle
    
    while (totalangle % 360 != 0)
    {
        closure.call()
        t.rt angle
        totalangle += angle
    }
}

clean()
t = newturtle('bug')

t.setPenColor blue

// polyroll(this.&square, 30)
polyroll(this.&pentagon, 45)


