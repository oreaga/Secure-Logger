package share;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//import java.util.IdentityHashMap.EntryIterator.Entry;

public class DB implements Serializable
{
  private ArrayList<Entry> entries;

  private static final int ERRCODE = 255;
  private static final int PASS = 0;
  private static final int FAIL = -1;
  private static final boolean debug = false;

  public DB()
  {
    entries = new ArrayList<Entry>();
  }

  public ArrayList<Entry> getEntries()
  {
    return entries;
  }

  public int add(Entry e)
  {
    int ret = e.validateState();

    if (ret == FAIL)
    {
      return FAIL;
    }
    else
    {
      entries.add(e);

      return PASS;
    }
  }

  public int add(int ti, int r, String n, String ty, String e)
  {
    Entry entry = new Entry(ti, r, n, ty, e);

    int ret = entry.validateState();

    if (ret == FAIL)
    {
      return FAIL;
    }
    else
    {
      entries.add(entry);

      return PASS;
    }
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    
    for (int i = 0; i < entries.size(); i++)
    {
      Entry e = entries.get(i);

      sb.append( i
               + " :"
               + " timestamp: "
               + e.getTimestamp()
               + " name: "
               + e.getName()
               + " type: "
               + e.getType()
               + " event: "
               + e.getEvent()
               + " room: "
               + e.getRoom()
               + "\n"
               );
    }

    return sb.toString();
  }

  private void err()
  {
    System.out.println("invalid");
    System.exit(ERRCODE);
  }

  private void err(String s, int i)
  {
    System.out.println(s);
    System.exit(i);
  }

  public class Entry implements Serializable
  {
    private final int MINTIME = 1;
    private final int MAXTIME = 1073741823;
    private final int MINROOM = 0;
    private final int MAXROOM = 1073741823;

    private int timestamp;  // timestamp for this entry
    private int room;       // room for this entry
    private String name;    // name of person
    private String type;    // type of person: employee | guest
    private String event;   // type of event: arrive | depart | enter | leave

    public Entry(int ti, int r, String n, String ty, String e)
    {
      /*
      setTimestamp(ti);
      setType(ty);
      setName(n);
      setEvent(e);
      setRoom(r);
      */
      this.timestamp = ti;
      this.type = ty;
      this.name = n;
      this.event = e;
      this.room = r;
    }
/*
    public Entry(int ti, int r, String n, String ty)
    {
      setTimestamp(ti);
      setType(ty);
      setName(n);
      setEvent("arrive");
      setRoom(r);
    }
*/
    /**
     * Used for copying the entries list, avoid validation failures
     *
     * @param e
     *  Entry to copy fields from
     */
    public Entry(Entry e)
    {
      this.timestamp = e.getTimestamp();
      this.name = e.getName();
      this.event = e.getEvent();
      this.room = e.getRoom();
      this.type = e.getType();
    }

    public int getTimestamp()
    {
      return timestamp;
    }

    public int getRoom()
    {
      return room;
    }

    public String getName()
    {
      return name;
    }

    public String getType()
    {
      return type;
    }

    public String getEvent()
    {
      return event;
    }

    public int setTimestamp(int t)
    {
      int ret = validateTimestamp(t);

      if (ret == FAIL)
      {
        return FAIL;
      }
      else
      {
        timestamp = t;

        return PASS;
      }
    }

    public int setRoom(int r)
    {
      int ret = validateRoom(r);

      if (ret == FAIL)
      {
        return FAIL;
      }
      else
      {
        room = r;

        return PASS;
      }
    }

    public int setName(String n)
    {
      int ret = validateName(n);

      if (ret == FAIL)
      {
        return FAIL;
      }
      else
      {
        name = n;

        return PASS;
      }
    }

    public void setType(String t)
    {
      if (debug)
      {
        validateType(t);
      }

      type = t;
    }

    public void setEvent(String e)
    {
      if (debug)
      {
        validateEvent(e);
      }

      event = e;
    }

    private int validateState()
    {
      int ret = validateTimestamp(timestamp);
      
      if (ret == FAIL)
      {
        return FAIL;
      }

      ret = validateName(name);
      
      if (ret == FAIL)
      {
        return FAIL;
      }

      ret = validateRoom(room);
      
      if (ret == FAIL)
      {
        return FAIL;
      }
/*
      ret = validateType(type);
      
      if (ret == FAIL)
      {
        return FAIL;
      }

      ret = validateEvent(event);
      
      if (ret == FAIL)
      {
        return FAIL;
      }
      else
      {
        return PASS;
      }
*/
      return PASS;
    }

    /**
     * Valid timestamp must be between 1 and 1,073,741,823 inclusive.
     * Also must increase from the last recorded event.
     *
     * @param t
     *  Timestamp to validate
     */
    private int validateTimestamp(int t)
    {
      if ((t >= MINTIME) && (t <= MAXTIME))
      {
        int size = entries.size();
        
        if (size >= 1)
        {
          Entry prev = entries.get(size - 1);

          if (t <= prev.getTimestamp())
          {
            if (debug)
            {
              err("invalid : DB : validateTimestamp : 1", ERRCODE);
            }
            else
            {
              return FAIL;
            }
          }
        }
      }
      else
      {
        if (debug)
        {
          err("invalid : DB : validateTimestamp : 2", ERRCODE);
        }
        else
        {
          return FAIL;
        }
      }

      return PASS;
    }

    /**
     * Valid room must be between 0 and 1,073,741,823 inclusive.
     * Leading 0's should be dropped (automatic with int type).
     * Employees / guests cannot leave a room without first entering it.
     * Employees / guests cannot enter a room without leaving the previously
     *  entered room.
     * If a room number is not specified, the event is for the entire gallery.
     *
     * @param r
     *  Room number to validate
     */
    private int validateRoom(int r)
    {
      if ((event.equals("enter")) || (event.equals("leave")))
      {
        if ((r < MINROOM) || (r > MAXROOM))
        {
          if (debug)
          {
            err("invalid : DB : validateRoom : invalid rooom number", ERRCODE);
          }
          else
          {
            return FAIL;
          }
        }
      }

      // use reverse list to find most recent entry first for <name>
      ArrayList<Entry> clone = new ArrayList<Entry>(entries.size());

      for (Entry e : entries)
        clone.add(new Entry(e));

      Collections.reverse(clone);

      Entry[] arr = clone.toArray(new Entry[clone.size()]);

      // Find most recent entry for specified person
      Entry lastEntry = Arrays.stream(arr)
                              .filter(x -> (x.getName().equals(name)))
                              .filter(x -> (x.getType().equals(type)))
                              .findFirst()
                              .orElse(null);

      if (lastEntry == null)
      {
        switch (event)
        {
          case "arrive" : break;
          case "depart" : return FAIL;  // must first arrive
          case "enter"  : return FAIL;  // must first arrive
          case "leave"  : return FAIL;  // must first arrive and enter a room
        }
      }
      else
      {
        switch (lastEntry.getEvent())
        {
          case "arrive" : switch (event)
                          {
                            case "arrive" : return FAIL;  // arrival after arrival
                            case "depart" : break;
                            case "enter"  : break;
                            case "leave"  : return FAIL;  // leaving room without entering
                          }
                          break;
          case "depart" : switch (event)
                          {
                            case "arrive" : break;
                            case "depart" : return FAIL;  // departure after departure
                            case "enter"  : return FAIL;  // entering room after leaving gallery
                            case "leave"  : return FAIL;  // leaving room after leaving gallery
                          }
                          break;
          case "enter"  : switch (event)
                          {
                            case "arrive" : return FAIL;  // arrival after entering room
                            case "depart" : return FAIL;  // must leave room first
                            case "enter"  : return FAIL;  // already in room
                            case "leave"  : if (lastEntry.getRoom() != r)
                                            {
                                              return FAIL;  // leaving room that haven't entered
                                            }
                                            break;
                          }
                          break;
          case "leave"  : switch (event)
                          {
                            case "arrive" : return FAIL;  // already arrived
                            case "depart" : break;
                            case "enter"  : break;
                            case "leave"  : return FAIL;  // not in room
                          }
                          break;
        }
      }

      return PASS;
    }

    /*
     * Valid names include only alphabetic characters.
     *
     * @param n
     *  Name to be validated
     */
    private int validateName(String n)
    {
      if (!(n.matches("[a-zA-Z]+")))
      {
        if (debug)
        {
          err("invalid : DB : validateName : invalid characters in name", ERRCODE);
        }
        else
        {
          return FAIL;
        }
      }

      return PASS;
    }

    /*
     ********** Unnecessary with current implementation
     *
     * Valid person types include 'employee,' 'guest'
     *
     * @param t
     *  Person type to be validated
     */
    private void validateType(String t)
    {
      if (!((t.equals("employee")) || (t.equals("guest"))))
      {
        if (debug)
        {
          err("invalid : DB : validateType : type not 'employee' or 'guest'", ERRCODE);
        }
        else
        {
          err();
        }
      }
    }

    /**
     ********** Unnecessary with current implementation
     *
     * Valid event types include 'arrive,' indicating gallery arrival
     *                           'depart,' indicating gallery departure
     *                           'enter,' indicating room entry
     *                           'leave,' indicating room leave
     *
     * @param e
     *  Event to be validated
     */
    private void validateEvent(String e)
    {
      if (!((e.equals("arrive"))
            || (e.equals("depart"))
            || (e.equals("enter"))
            || (e.equals("leave"))))
      {
        if (debug)
        {
          err("invalid : DB : validateEvent : invalid event type", ERRCODE);
        }
        else
        {
          err();
        }
      }
    }

    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException
    {
      ois.defaultReadObject();
      //validateState();
    }

    private void writeObject(ObjectOutputStream oos)
      throws IOException
    {
      oos.defaultWriteObject();
    }
  }
}
