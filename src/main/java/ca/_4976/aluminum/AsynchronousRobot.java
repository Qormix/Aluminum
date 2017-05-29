package ca._4976.aluminum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.Manifest;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.HLUsageReporting;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;
import org.opencv.core.Core;

import edu.wpi.first.wpilibj.hal.FRCNetComm.tInstances;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tResourceType;
import edu.wpi.first.wpilibj.hal.HAL;
import edu.wpi.first.wpilibj.internal.HardwareHLUsageReporting;
import edu.wpi.first.wpilibj.internal.HardwareTimer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.util.WPILibVersion;

public abstract class AsynchronousRobot {

	public static final int ROBOT_TASK_PRIORITY = 101;
	public static final long MAIN_THREAD_ID = Thread.currentThread().getId();

	protected final DriverStation ds;

	 private final ArrayList<RobotStateChangedListener> robotStateChangedListeners = new ArrayList<>();
	 private final ArrayList<Evaluator> evaluators = new ArrayList<>();
	 private final Resources resources;

	private RobotState.state lastState = RobotState.get();

	protected AsynchronousRobot() {

		NetworkTable.setNetworkIdentity("Robot");
		NetworkTable.setPersistentFilename("/home/lvuser/networktables.ini");
		NetworkTable.setServerMode();// must be before b
		ds = DriverStation.getInstance();
		NetworkTable.getTable(""); // forces network tables to initialize
		NetworkTable.getTable("LiveWindow").getSubTable("~STATUS~").putBoolean("LW Enabled", false);

		HAL.observeUserProgramStarting();
		robotStateChangedListeners.forEach(RobotStateChangedListener::init);

		robotStateChangedListeners.add(new RobotStateChangedListener() {

			@Override void init() { }

			@Override void disabled() { evaluators.clear(); }

			@Override void autonomous() { }

			@Override void teleoperated() { }

			@Override void test() { }
		});

		resources = Resources.asFinal();

		robotStateChangedListeners.addAll(Arrays.asList(resources.getListeners()));

		while (!Thread.interrupted()) {

			if (ds.isFMSAttached()) try { tick();

			} catch (Exception e) { e.printStackTrace(); }

			else tick();
		}
	}

	private void tick() {

		ds.waitForData();

		switch (RobotState.get()) {
			case disabled:
				if (lastState != RobotState.state.disabled) robotStateChangedListeners.forEach(RobotStateChangedListener::disabled);
				lastState = RobotState.state.disabled;
				HAL.observeUserProgramDisabled();
				evaluators.forEach(it -> { if (System.currentTimeMillis() >= it.execute) it.evaluable.eval(); });
				break;
			case operator:
				if (lastState != RobotState.state.disabled) robotStateChangedListeners.forEach(RobotStateChangedListener::teleoperated);
				lastState = RobotState.state.operator;
				HAL.observeUserProgramTeleop();
				resources.check();
				evaluators.forEach(it -> { if (System.currentTimeMillis() >= it.execute) it.evaluable.eval(); });
				break;
			case autonomous:
				if (lastState != RobotState.state.disabled) robotStateChangedListeners.forEach(RobotStateChangedListener::autonomous);
				lastState = RobotState.state.autonomous;
				HAL.observeUserProgramAutonomous();
				resources.checkHardware();
				evaluators.forEach(it -> { if (System.currentTimeMillis() >= it.execute) it.evaluable.eval(); });
				break;
			case test:
				if (lastState != RobotState.state.disabled) robotStateChangedListeners.forEach(RobotStateChangedListener::test);
				lastState = RobotState.state.autonomous;
				HAL.observeUserProgramTest();
				resources.check();
				evaluators.forEach(it -> { if (System.currentTimeMillis() >= it.execute) it.evaluable.eval(); });
				break;
		}
	}

	public void addRobotStateChangedListener(RobotStateChangedListener listener) { robotStateChangedListeners.add(listener); }

	public void runNextLoop(Evaluable evaluable) { evaluators.add(new Evaluator(evaluable, 0)); }

	public void runLater(Evaluable evaluable, int delay) { evaluators.add(new Evaluator(evaluable, delay)); }

	public static boolean isSimulation() { return false; }
	public static boolean isReal() { return true; }

	public static void main(String... args) {

		int rv = HAL.initialize(0);
		assert rv == 1;

		Timer.SetImplementation(new HardwareTimer());
		HLUsageReporting.SetImplementation(new HardwareHLUsageReporting());
		RobotState.SetImplementation(DriverStation.getInstance());

		try { System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		} catch (UnsatisfiedLinkError ex) {

			System.out.println("OpenCV Native Libraries could not be loaded.");
			System.out.println("Please try redeploying, or reimage your roboRIO and try again.");
			ex.printStackTrace();
		}

		HAL.report(tResourceType.kResourceType_Language, tInstances.kLanguage_Java);

		String robotName = "";
		Enumeration<URL> resources = null;
		try {
			resources = RobotBase.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		while (resources != null && resources.hasMoreElements()) {
			try {
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				robotName = manifest.getMainAttributes().getValue("Robot-Class");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		RobotBase robot;
		try {
			robot = (RobotBase) Class.forName(robotName).newInstance();
		} catch (Throwable throwable) {
			DriverStation.reportError("ERROR Unhandled exception instantiating robot " + robotName + " "
					+ throwable.toString() + " at " + Arrays.toString(throwable.getStackTrace()), false);
			System.err.println("WARNING: Robots don't quit!");
			System.err.println("ERROR: Could not instantiate robot " + robotName + "!");
			System.exit(1);
			return;
		}

		try {

			final File file = new File("/tmp/frc_versions/FRC_Lib_Version.ini");

			if (file.exists()) file.delete();

			file.createNewFile();

			try (FileOutputStream output = new FileOutputStream(file)) {
				output.write("Java ".getBytes());
				output.write(WPILibVersion.Version.getBytes());
			}

		} catch (IOException ex) { ex.printStackTrace(); }

		boolean errorOnExit = false;
		try {
			System.out.println("********** Robot program starting **********");
			robot.startCompetition();
		} catch (Throwable throwable) {
			DriverStation.reportError(
					"ERROR Unhandled exception: " + throwable.toString() + " at "
							+ Arrays.toString(throwable.getStackTrace()), false);
			errorOnExit = true;
		} finally {
			// startCompetition never returns unless exception occurs....
			System.err.println("WARNING: Robots don't quit!");
			if (errorOnExit) {
				System.err
						.println("---> The startCompetition() method (or methods called by it) should have "
								+ "handled the exception above.");
			} else {
				System.err.println("---> Unexpected return from startCompetition() method.");
			}
		}
		System.exit(1);
	}

	private class Evaluator {

		final Evaluable evaluable;
		final long execute;

		private Evaluator(Evaluable evaluable, int delay) {

			this.evaluable = evaluable;
			execute = System.currentTimeMillis() + delay;
		}
	}
}
