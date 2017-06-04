import ca._4976.aluminum.RampedMotor;
public class Robot {

    public static void main(String[] args) throws InterruptedException {

        RampedMotor<Boolean> motor = new RampedMotor<>(true, 0.1);

        motor.set( -1.0);

        while (motor.motor) {

            motor.eval();

            Thread.sleep(20);
        }
    }
}
