package gturtle

/**
 * User: Eitan Suez
 * Date: Nov 17, 2008
 */
class FifoOrderedSet
{
  LinkedList ll
  int size = 5

  FifoOrderedSet(int size)
  {
    ll = new LinkedList()
    this.size = size
  }

  void add(item)
  {
    if (ll.contains(item))
    {
      ll.remove(item)
    }
    ll.addFirst(item)
    if (ll.size() > this.size)
    {
      ll.removeLast()
    }
  }
  void clear() { ll.clear() }
  void remove(item) { ll.remove(item) }
  void each(Closure closure) { ll.each closure }
  void eachWithIndex(Closure closure) { ll.eachWithIndex closure }
  int size() { ll.size() }
  def get(int i) { ll.get(i) }
}
