package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import java.util.ArrayList;
import java.util.List;

public class Events
{
    public static interface EventListener
    {
        void onEvent(MyEvent myEvent);
    }

    public static abstract class EventProvider
    {
        List<EventListener> listeners = new ArrayList<EventListener>();

        void addEventListener(EventListener l)
        {
            listeners.add(l);
        }
        void removeEventListener(EventListener l)
        {
            listeners.remove(l);
        }
        void notifyEvent(MyEvent myEvent)
        {
            for(EventListener l : listeners) l.onEvent(myEvent);
        }
    }

    public static abstract class MyEvent
    {
        protected Object sender;
        protected int id;
        protected Object arg;

        public MyEvent(Object sender, int id, Object arg)
        {
            this.sender=sender;
            this.id=id;
            this.arg=arg;
        }

        public Object getSender()
        {
            return sender;
        }
        public int getID()
        {
            return id;
        }
        public Object getArg()
        {
            return arg;
        }
    }
}
