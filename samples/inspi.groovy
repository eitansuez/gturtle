t = newturtle('turtle')

def inspi(side, angle, inc, level=0)
{
    if (level >= 1000)
    {
        return;
    }
    t.fd side
    t.rt angle
    inspi(side, angle+inc, inc, level+1)
}

clean()
t.setPos(-50,-50)
inspi(20, 0, 7)

