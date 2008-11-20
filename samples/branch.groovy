
def branch(length, level)
{
    if(level == 0) return
    fd length
    lt 45
    branch(length/2, level-1)
    rt 90
    branch(length/2, level-1)
    lt 45
    bk length
}

def lbranch(length, angle, level)
{
    fd(2*length)
    node(length, angle, level)
    bk(2*length)
}
def rbranch(length, angle, level)
{
    fd length
    node(length, angle, level)
    bk length
}

def node(length, angle, level)
{
    if (level == 0) return;
    lt angle
    lbranch(length, angle, level -1)
    rt(2*angle)
    rbranch(length, angle, level-1)
    lt angle
}

clean()
//branch(100,6)

setpencolor(new java.awt.Color(0x008800))
setpos(30, -150)
lbranch(25, 20, 7)


