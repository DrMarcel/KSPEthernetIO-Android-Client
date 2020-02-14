package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import java.util.ArrayList;
import java.util.List;

/**
 * General Purpose event system.
 * Provides observer pattern interfaces for class communication.
 *
 * An event provider extends Events.EventProvider
 * An event listener extends Events.EventListener
 *
 * The provider can hold any number of EventListener.
 * If the provider notifies an event the onEvent function of the listeners is called.
 */
public class Events
{
    /**
     * Event Listener
     */
    public static interface EventListener
    {
        void onEvent(AbstractEvent event);
    }

    /**
     * Event provider
     */
    public static abstract class EventProvider
    {
        List<EventListener> listeners = new ArrayList<EventListener>();

        /**
         * Add event listener.
         *
         * @param l Event listener
         */
        void addEventListener(EventListener l)
        {
            listeners.add(l);
        }

        /**
         * Remove event listener.
         *
         * @param l Event listener
         */
        void removeEventListener(EventListener l)
        {
            listeners.remove(l);
        }

        /**
         * Notify all listeners
         * @param event Event
         */
        void notifyEvent(AbstractEvent event)
        {
            for(EventListener l : listeners) l.onEvent(event);
        }
    }

    /**
     * Abstract event class.
     * The event provider may extend various Event types and extend the with necessary event data.
     * It's possible to distinguish the events by defining a unique event ID.
     */
    public static abstract class AbstractEvent
    {
        protected EventProvider sender;
        protected int id;
        protected Object arg;

        /**
         * Create a new Event.
         *
         * @param sender The class triggering the event
         * @param id Event identifier
         * @param arg Optional Event arguments
         */
        public AbstractEvent(EventProvider sender, int id, Object arg)
        {
            this.sender=sender;
            this.id=id;
            this.arg=arg;
        }

        /**
         * Get event sender.
         *
         * @return EventProvider sender class
         */
        public EventProvider getSender()
        {
            return sender;
        }

        /**
         * Get event identifier.
         *
         * @return Unique event identifier
         */
        public int getID()
        {
            return id;
        }

        /**
         * Get Event arguments.
         *
         * @return Event arguments
         */
        public Object getArg()
        {
            return arg;
        }
    }
}
