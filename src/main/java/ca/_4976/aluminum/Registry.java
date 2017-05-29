package ca._4976.aluminum;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.VictorSP;
import javafx.util.Pair;

import java.util.Collections;
import java.util.Map;

public class Registry {

	private static Map<Integer, Pair<String, Object>> nodes = Collections.emptyMap();
	private static Map<Integer, Pair<String, Object>> pcm = Collections.emptyMap();
	private static Map<Integer, Pair<String, Object>> pwm = Collections.emptyMap();

	public static VictorSP getVictorSP(int pin) {

		if (pwm.containsKey(pin)) {

			Pair pair = pwm.get(pin);

			if (pair.getValue() instanceof VictorSP) {

				DriverStation.reportWarning(new Error("VictorSP already initialized on pin " + pin).getMessage(), true);

				return (VictorSP) pair.getValue();
			}

			DriverStation.reportError(new Error("Can't initialize VictorSP on pin " + pin + " already initialized as " + pair.getKey()).getMessage(), true);
			System.exit(-1);
		}

		VictorSP victorSP = new VictorSP(pin);
		pwm.put(pin, new Pair<>("VictorSP", victorSP));
		return victorSP;
	}

	public static CANTalon getCANTalon(int node) {

		if (nodes.containsKey(node)) {

			Pair pair = nodes.get(node);

			if (pair.getValue() instanceof CANTalon) {

				DriverStation.reportWarning(new Error("TalonSRX already initialized on node " + node).getMessage(), true);

				return (CANTalon) pair.getValue();
			}

			DriverStation.reportError(new Error("Can't initialize TalonSRX on node " + node + " already initialized as " + pair.getKey()).getMessage(), true);
			System.exit(-1);
		}

		CANTalon talon = new CANTalon(node);
		nodes.put(node, new Pair<>("TalonSRX", talon));
		return talon;
	}

	public Compressor getCompressor(int node) {

		if (nodes.containsKey(node)) {

			Pair pair = nodes.get(node);

			if (!pair.getKey().equals("PCM")) {

				DriverStation.reportError(new Error("Can't initialize PCM on node " + node + " already initialized as " + pair.getKey()).getMessage(), true);
				System.exit(-1);
			}

		} else nodes.put(node, new Pair<>("PCM", null));

		if (pcm.containsKey(-1)) {

			Pair pair = pcm.get(-1);

			if (pair.getValue() instanceof Compressor) {

				DriverStation.reportWarning(new Error("Compressor already initialized on node " + node).getMessage(), true);

				return (Compressor) pair.getValue();
			}
		}

		Compressor compressor = new Compressor(node);
		pcm.put(-1, new Pair<>("Compressor", compressor));
		return compressor;
	}

	public DoubleSolenoid getDoubleSolenoid(int node, int extend, int retract) {

		if (nodes.containsKey(node)) {

			Pair pair = nodes.get(node);

			if (!pair.getKey().equals("PCM")) {

				DriverStation.reportError(new Error("Can't initialize PCM on node " + node + " already initialized as " + pair.getKey()).getMessage(), true);
				System.exit(-1);
			}

		} else nodes.put(node, new Pair<>("PCM", null));

		for (int i = extend; true; i = retract) {

			if (!pcm.containsKey(i) && i != retract) continue;

			else if (i == retract) break;

			Pair pair = pcm.get(i);

			if (pair.getValue() instanceof DoubleSolenoid) {

				DriverStation.reportWarning(new Error("DoubleSolenoid already initialized on node " + node).getMessage(), true);

				return (DoubleSolenoid) pair.getValue();

			} else {

				DriverStation.reportError(new Error("Can't initialize DoubleSolenoid on node " + node + " already initialized as " + pair.getKey()).getMessage(), true);
				System.exit(-1);
			}
		}

		DoubleSolenoid doubleSolenoid = new DoubleSolenoid(node, extend, retract);
		pcm.put(extend, new Pair<>("DoubleSolenoid", doubleSolenoid));
		pcm.put(retract, new Pair<>("DoubleSolenoid", doubleSolenoid));
		return doubleSolenoid;
	}
}
