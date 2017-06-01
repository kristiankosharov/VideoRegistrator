package reg.videoregistrator.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

public class GSensorListener implements SensorEventListener {

    private ArrayList<Double> gForceValues = new ArrayList<>();
    private double gForceLimit = 0;
    private IGForceListener mListener;

    public GSensorListener(Context context, IGForceListener listener) {
        mListener = listener;
        gForceLimit = PreferencesUtils.getGLimit(context);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // Calculate real g force
        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);
        gForceValues.add(gForce);

        if (handleCrash()) {
            mListener.handleCrash();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Check if there diff above {@link #gForceLimit}
     *
     * @return if there is diff
     */
    private boolean handleCrash() {
        if (gForceValues.size() < 15) {
            for (int i = gForceValues.size(); i >= 0; i--) {
                if (checkDiff(i)) return true;
            }
        } else if (gForceValues.size() > 15) {
            for (int i = gForceValues.size(); i >= gForceValues.size() - 15; i--) {
                if (checkDiff(i)) return true;
            }
        }

        return false;
    }

    /**
     * Calculate diff of forces
     *
     * @param position in for
     *
     * @return true if there is diff
     */
    private boolean checkDiff(int position) {
        double diff = 0;
        if (position == 1) {
            diff = Math.abs(gForceValues.get(position) - gForceValues.get(0));
        } else {
            diff = Math.abs(gForceValues.get(position) - gForceValues.get(position - 1));
        }

        if (diff >= gForceLimit) {
            return true;
        }

        return false;
    }
}
