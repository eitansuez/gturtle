def snowflake(size, level)
{
    3.times {
        side(size, level)
        rt 120
    }
}

def side(size, level)
{
    if (level==0)
    {
        fd size
        return
    }
    side(size/3, level-1)
    lt 60
    side(size/3, level-1)
    rt 120
    side(size/3, level-1)
    lt 60
    side(size/3, level-1)
}

clean()
lt 30
setpos(0,-100)
snowflake(250, 4)
