package ca._4976.aluminum;

public class RobotState extends edu.wpi.first.wpilibj.RobotState {

	enum state { disabled, operator, autonomous, test }

	 public static state get() {

		if (isDisabled()) return state.disabled;

		if (isAutonomous()) return state.autonomous;

		if (isOperatorControl()) return state.operator;

		if (isTest()) return state.test;

		throw new IllegalStateException();
	}
}
