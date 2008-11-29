
clean()
def turtle_army()
{
	ts = []
	h = -150
	8.times {
	    ts << newturtle('bug')
	    //ts.last().setHeading(h)
	    ts.last().setPos(h, 0)
	    h = h + 50
	}
	ts.each { turtle ->
	  turtle.fd 100
	//  turtle.lt 90
	//  turtle.fd 100
	}
}
  
def turtle_heads()
{
	ts = []
	h = 0
	8.times {
	    ts << newturtle('bug')
	    ts.last().setHeading(h)
	    h = h + 45
	}
	ts.each { turtle ->
	  turtle.fd 100
	  turtle.lt 90
	}
}

turtle_heads()