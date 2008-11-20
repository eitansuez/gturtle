clean()
def circle()
{
  (1..360).each {
      fd 1
      rt 1
  }
}
def arcr(r, deg)
{
    deg.times {
        fd r
        rt 1
    }
}
def arcl(r, deg)
{
    deg.times {
        fd r
        lt 1
    }
}
def circles (angle)
{
    def totalangle = 0
    arcr 1, 360
    rt angle
    totalangle += angle
    while (totalangle % 360 != 0)
    {
        arcr 1, 360
        rt angle
        totalangle += angle
    }
 }
setpencolor blue
circles(30)

