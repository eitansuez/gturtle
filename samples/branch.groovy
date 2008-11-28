
def branch(length, level)
{
    if(level == 0) return
    fd length
    def distance = length/2
    lt 45
    branch(distance, level-1)
    rt 90
    branch(distance, level-1)
    lt 45
    bk length
}
clean()
//branch(100,6)


def lbranch(length, angle, level)
{
    def dist = 2*length
    fd(dist)
    node(length, angle, level)
    bk(dist)
}
def rbranch(length, angle, level)
{
    fd length
    node(length, angle, level)
    bk length
}
def mbranch(length, angle, level)
{
    fd(length*1.5)
    node(length, angle, level)
    bk(length*1.5)
}

def node(length, angle, level)
{
    if (level == 0) return;
    lt angle
    lbranch(length, angle, level -1)
    rt(angle)
//    mbranch(length, angle, level-1)
    rt(angle)
    rbranch(length, angle, level-1)
    lt angle
}


setpencolor(new java.awt.Color(0x008800))
setpos(30, -150)
lbranch(25, 20, 7)


