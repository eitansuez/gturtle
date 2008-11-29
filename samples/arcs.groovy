t = newturtle('turtle')
clean()
def circle()
{
  (1..360).each {
      t.fd 1
      t.rt 1
  }
}
def arcr(r, deg)
{
    deg.times {
        t.fd r
        t.rt 1
    }
}
def arcl(r, deg)
{
    deg.times {
        t.fd r
        t.lt 1
    }
}
def circles (angle)
{
    def totalangle = 0
    arcr 1, 360
    t.rt angle
    totalangle += angle
    while (totalangle % 360 != 0)
    {
        arcr 1, 360
        t.rt angle
        totalangle += angle
    }
 }
t.setPenColor blue
circles(30)

