t = newturtle('bug')

def branch(length, level)
{
    if(level == 0) return
    t.fd length
    def distance = length/2
    t.lt 45
    branch(distance, level-1)
    t.rt 90
    branch(distance, level-1)
    t.lt 45
    t.bk length
}
clean()
//branch(100,6)


def lbranch(length, angle, level)
{
    def dist = 2*length
    t.fd(dist)
    node(length, angle, level)
    t.bk(dist)
}
def rbranch(length, angle, level)
{
    t.fd length
    node(length, angle, level)
    t.bk length
}
def mbranch(length, angle, level)
{
    t.fd(length*1.5)
    node(length, angle, level)
    t.bk(length*1.5)
}

def node(length, angle, level)
{
    if (level == 0) return;
    t.lt angle
    lbranch(length, angle, level -1)
    t.rt(angle)
//    mbranch(length, angle, level-1)
    t.rt(angle)
    rbranch(length, angle, level-1)
    t.lt angle
}


t.setPenColor(new java.awt.Color(0x008800))
t.setPos(30, -150)
lbranch(25, 20, 7)


