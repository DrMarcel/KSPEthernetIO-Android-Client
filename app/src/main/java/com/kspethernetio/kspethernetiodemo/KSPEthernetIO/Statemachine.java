package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;


import java.util.ArrayList;
import java.util.List;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.MyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventListener;

/**
 * Statemachine can start and stop a statemachine made of State-Objects.
 * Provides a Listener interface to recognize statechanges.
 * Acts as ActionListener to forward ActionEvents to the States.
 * 
 * @author Josh Perske
 */
public class Statemachine implements EventListener
{	
	private String name; 
	
	private static final int STANDARD_STEP_DELAY = 20;
	private int stepDelay;
	
	//State data
	private State current;
	private boolean stop;
	
	//List of all states
	private int nextStateID = 1;
	private List<State> states = new ArrayList<State>();
	
	/**
	 * Create a new Statemachine.
	 * @param name Name of the Statemachine
	 */
	public Statemachine(String name)
	{
		this(name, STANDARD_STEP_DELAY);
	}
	/**
	 * Create a new Statemachine.
	 * @param name Name of the Statemachine
	 * @param delay Minimum delay between onExecute() calls in milliseconds
	 */
	public Statemachine(String name, int delay)
	{
		this.current = null;
		this.stop = true;
		this.name = name;
		
		stepDelay = delay;
	}
	
	/**
	 * State as part of a Statemachine.
	 * Each State provides the functions
	 *  - onEnter
	 *  - onExecute
	 *  - onExit
	 * These functions are called automatically after starting the statemachine
	 */
	public static abstract class State
	{
		private String name;
		private Statemachine parent;
		private int id;
		
		/**
		 * Create a new State and assign to a Statemachine.
		 * @param name State name
		 * @param parent Statemachine
		 */
		public State(String name, Statemachine parent)
		{
			this.name = name;
			this.parent = parent;
			//Add to parent statemachine
			id = parent.nextStateID;
			parent.nextStateID++;
			parent.states.add(this);
		}
		
		/**
		 * Returns the State name
		 * @return State name
		 */
		public String getName()
		{
			return name;
		}
		/**
		 * Returns the State ID > 0
		 * @return ID > 0
		 */
		public int getID()
		{
			return id;
		}
		/**
		 * Returns the parent Statemachine
		 * @return Statemachine
		 */
		public Statemachine getStatemachine()
		{
			return parent;
		}
		/**
		 * Return the name of the State, its ID and the parent Statemachine
		 * @return String with State data
		 */
		@Override
		public String toString()
		{
			return "State \"" + name + "\" (ID:" + Integer.toString(id) + ") in \"" + parent.getName();
		}
		
		/**
		 * Once called when the State is entered.
		 */
		public abstract void onEnter();
		/**
		 * Cyclic called while the State is active.
		 * Returns the next state. If return value is 'null',
		 * the Statemachine is stopped and the Finished event
		 * is triggered. If return value is 'this' the
		 * Statemachnine stays in current state.
		 * @param e null or ActionEvent
		 * @return Next State
		 */
		public abstract State onExecute(MyEvent e);
		/**
		 * Once called when the State is finished.
		 */
		public abstract void onExit();
	}

	/**
	 * Start the Statemachine with initial State.
	 * Does nothing if statemachine is already active.
	 * @param initial The initial state after start()
	 */
	public void start(State initial)
	{
		//Check if statemachine is active
		if(!isActive() && initial != null)
		{
			//Start statemachine
			current = initial;
			stop = false;
			clearEventStack();
			triggerStatemachineStarted();
			current.onEnter();
			triggerNextStep();
		}
	}
	/**
	 * Start the Statemachine at State with given id.
	 * Does nothing if statemachine is already active.
	 * @param id The id of the initial state
	 */
	public void start(int id)
	{
		start(getState(id));
	}
	/**
	 * Stop the Statemachine.
	 * Does nothing if statemachine is not active.
	 */
	public void stop()
	{
		if(isActive()) stop = true;
	}
	/**
	 * Returns true if the Statemachine is active.
	 * @return true if active
	 */
	public boolean isActive()
	{
		return !stop;	
	}

	/**
	 * Returns the active State or null.
	 * @return The active State or null.
	 */
	public State getActiveState()
	{
		return current;
	}
	/**
	 * Returns the active State ID.
	 * Return 0 if the Statemachine is not active.
	 * @return The active State ID or 0.
	 */
	public int getActiveStateID()
	{
		if(current != null) return current.getID();
		return -1;
	}
	/**
	 * Returns the State with the given ID.
	 * @return The State or null.
	 */
	public State getState(int id)
	{
		for(State s : states)
			if(s.getID() == id) return s;
		return null;
	}
	
	
	/**
	 * Returns the name of the Statemachine
	 * @return Name of the Statemachine
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * Returns a string with the Statemachine data and a list of the States.
	 * @return String representing the Statemachine.
	 */
	@Override
	public String toString()
	{
		String str = "Statemachine \"" + name + "\" is ";
		if(isActive()) str += "active";
		else str += "inactive";
		for(State s : states)
		{
			if(current == s) str+="\n > ";
			else str+="\n   ";
			str+=s.getName()+" (ID:"+s.getID()+")";
		}
		return str;
	}
	
	/**
	 * Register the next step to be executed later.
	 */
	private void triggerNextStep()
	{
		//if(current != null && isActive()) stepDelay.start();
		Thread tRunStep = new Thread(runStep);
		if(current != null && isActive()) tRunStep.start();
	}
	/**
	 * Execution of the next step
	 */
	private Runnable runStep = new Runnable()
	{		
		@Override
		public void run()
		{
			try
			{
				Thread.sleep(stepDelay);
			}
			catch(InterruptedException e)
			{
			}
			
			//Check if stop is requested
			if(!isActive() || current == null)
			{
				//stop
				if(current != null) current.onExit();
				clearEventStack();
				current = null;
				triggerStatemachineStopped();
			}
			else
			{
				//Execute next step
				State next = current.onExecute(nextEvent());
				
				if(next == null)
				{
					//Statemachine is finished
					current.onExit();
					clearEventStack();
					current = null;
					stop = true;
					triggerStatemachineFinished();
				}
				else if(next != current)
				{
					State old = current;
					//State changed
					current.onExit();
					clearEventStack();
					current = next;
					current.onEnter();
					triggerStatemachineStateChanged(old, current);
				}
				
				//Execute next step
				triggerNextStep();
			}
		}
	};
	
	
	/**
	 * Listeners
	 */
	private List<StatemachineListener> listeners = new ArrayList<StatemachineListener>();
	/**
	 * Add a new StatemachineListener
	 * @param l New Listener
	 */
	public void addStatemachineListener(StatemachineListener l)
	{
		listeners.add(l);
	}
	/**
	 * Remove a StatemachineListener
	 * @param l Listener to remove
	 */
	public void removeStatemachineListener(StatemachineListener l)
	{
		listeners.remove(l);
	}
	
	/**
	 * Listener trigger functions
	 */	
	private void triggerStatemachineStarted()
	{
		for(StatemachineListener l : listeners) l.statemachineStarted(this);
	}
	private void triggerStatemachineFinished()
	{
		for(StatemachineListener l : listeners) l.statemachineFinished(this);
	}
	private void triggerStatemachineStopped()
	{
		for(StatemachineListener l : listeners) l.statemachineStopped(this);
	}
	private void triggerStatemachineStateChanged(State os, State ns)
	{
		for(StatemachineListener l : listeners) l.statemachineStateChanged(this, os, ns);
	}
	
	/**
	 * StatemachineListener prototype
	 */
	public static interface StatemachineListener
	{
		public void statemachineStarted(Statemachine sm);
		public void statemachineFinished(Statemachine sm);
		public void statemachineStopped(Statemachine sm);
		public void statemachineStateChanged(Statemachine sm, State os, State ns);
	}

	/**
	 * Forward Action Events to the States.
	 */
	private List<MyEvent> myEventStack = new ArrayList<MyEvent>();
	private MyEvent nextEvent()
	{
		MyEvent e = null;
		if(myEventStack.size() > 0)
		{
			e = myEventStack.get(myEventStack.size()-1);
			myEventStack.remove(myEventStack.size()-1);
		}
		return e;
	}
	private void clearEventStack()
	{
		myEventStack.clear();
	}
	@Override
	public void onEvent(MyEvent myEvent)
	{
		myEventStack.add(myEvent);
	}
}
