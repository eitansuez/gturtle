t = newturtle('bug')

def snowflake(size, level)
{
    3.times {
        side(size, level)
        t.rt 120
    }
}

def side(size, level)
{
    if (level==0)
    {
        t.fd size
        return
    }
    side(size/3, level-1)
    t.lt 60
    side(size/3, level-1)
    t.rt 120
    side(size/3, level-1)
    t.lt 60
    side(size/3, level-1)
}

clean()
t.lt 30
t.setPos(0,-100)
snowflake(250, 4)
