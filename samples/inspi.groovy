def inspi(side, angle, inc, level=0)
{
    if (level >= 1000)
    {
        return;
    }
    fd side
    rt angle
    inspi(side, angle+inc, inc, level+1)
}

clean()
setpos(-50,-50)
inspi(20, 0, 7)

