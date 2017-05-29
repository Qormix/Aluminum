package ca._4976.aluminum;

import java.util.ArrayList;

public final class Resources {

	private final static ArrayList<Evaluable> HARDWARD = new ArrayList<>();
	private final static ArrayList<Evaluable> RESOURCE = new ArrayList<>();
	private final static ArrayList<RobotStateChangedListener> STATE_LISTENERS = new ArrayList<>();

	private final Evaluable[] hardware;
	private final Evaluable[] resource;
	private final RobotStateChangedListener[] listeners;

	private Resources() {

		hardware = new Evaluable[HARDWARD.size()];
		for (int i = 0; i < hardware.length; i++) hardware[i] = HARDWARD.get(i);

		resource = new Evaluable[RESOURCE.size()];
		for (int i = 0; i < resource.length; i++) resource[i] = RESOURCE.get(i);

		listeners = new RobotStateChangedListener[STATE_LISTENERS.size()];
		for (int i = 0; i < listeners.length; i++) listeners[i] = STATE_LISTENERS.get(i);
	}

	static Resources asFinal() { return new Resources(); }

	public static void register(Evaluable evaluable) { HARDWARD.add(evaluable); }

	public static void register(RobotStateChangedListener listener) { STATE_LISTENERS.add(listener); }

	public static void registerAsHardware(Evaluable evaluable) { HARDWARD.add(evaluable); }

	void checkHardware() { for (Evaluable evaluable : hardware) { evaluable.eval(); } }

	void check() {

		for (Evaluable evaluable : resource) evaluable.eval();
		checkHardware();
	}

	RobotStateChangedListener[] getListeners() { return listeners; }
}
