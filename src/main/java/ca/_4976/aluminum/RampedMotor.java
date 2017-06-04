package ca._4976.aluminum;

import edu.wpi.first.wpilibj.SpeedController;

public class RampedMotor<E> implements Evaluable {

    public E motor;

    private double target = 0.0;
    private double ramp = 0.1;
    private double speed = 0.0;
    private boolean paused = false;

    public RampedMotor(E motor, double ramp) {

        Resources.registerAsHardware(this);
        this.motor = motor;
        this.ramp = ramp / 50;
    }

    public void set(double speed) { target = speed; }

    public void setPaused(boolean paused) { this.paused = paused; }

    @Override public void eval() {

        if (!paused) {

            double increment = target - speed;
            if (Math.abs(increment) > ramp)
                increment = (ramp * increment) / Math.abs(increment);
            speed += increment;

            ((SpeedController) motor).set(speed);
        }
    }
}
