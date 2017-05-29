package ca._4976.aluminum;

public class DoubleSolenoid extends edu.wpi.first.wpilibj.DoubleSolenoid implements Evaluable {

	private boolean isExtended = false;
	private long timing = System.currentTimeMillis();

	public DoubleSolenoid(int moduleNumber, int forwardChannel, int reverseChannel) {

		super(moduleNumber, forwardChannel, reverseChannel);

		Resources.register(this);

		Resources.register(new RobotStateChangedListener() {

			@Override void init() { }

			@Override void disabled() { set(Value.kOff); }

			@Override void autonomous() { }

			@Override void teleoperated() { }

			@Override void test() { }
		});
	}

	public void set(boolean extended) {

		isExtended = extended;

		set(extended ? Value.kForward : Value.kReverse);
		timing = System.currentTimeMillis();
	}

	public boolean isExtended() { return isExtended; }

	@Override public void eval() {

		if (System.currentTimeMillis() >= timing + 500 && get() != Value.kOff) set(Value.kOff);
	}
}
