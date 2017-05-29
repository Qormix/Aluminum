package ca._4976.aluminum;

public abstract class RobotStateChangedListener {

	abstract void init();

	abstract void  disabled();

	abstract void autonomous();

	abstract void teleoperated();

	abstract void test();
}
