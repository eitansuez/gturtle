// an attempt to try to make the branch algorithm
// produce trees that look more like trees.

// we tweaked the turning angle from 45 to something narrower like 30 degrees
// in order to make it look like the tree prefers to point up
// we also varied the pensize (made it smaller at each increasing level)
// we also varied the color from an initial brown to a final green gradually
// we also played with the ratio of the lengths of a branch and its succeeding
//    branch.  the ratio controls how much we fan out the tree.
// try distance = length/1.1 vs length / 2.0
// we also varied the number of branches from 2 to 3

// for fun, try to make the tree not exactly end where it started by modifying,
//  say, the final lt 30 to lt 29.

t = newturtle('bug')

def branch(length, level, color)
{
    if(level == 0) return
    t.setPenColor(color)
    t.setPenSize(level * 2)
    t.fd length
    def distance = length/1.3
    def blue = 0x15
    def red = color.getRed()
    def green = color.getGreen()
    def newcolor = new java.awt.Color(red-redincrement, green+greenincrement, blue)
    def angle = 30
    t.lt angle
    branch(distance, level-1, newcolor)
    t.rt angle
    branch(distance, level-1, newcolor)
    t.rt angle
    branch(distance, level-1, newcolor)
    t.lt angle  // change to lt 29 and see what happens
    t.setPenColor color
    t.bk length
}

clean()
t.setPos(0,-200)
def brown = new java.awt.Color(0x944F15)
// final green is around ~ 0x1C9415
level = 6
redincrement = (int) ((0x90 - 0x20)/level)
greenincrement = (int) ((0xa0-0x50)/level)
branch(100,level, brown)

