package com.eagle.sensorcollector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.String;


public class SensorThread extends Thread implements SensorEventListener{

    private final String INFOTAG = "SENSORTHREAD_INFO";
    private final String ERRTAG = "SENSORTHREAD_ERROR";
    float x; //azimuth
    float y;  //pitch
    float z;   //roll
    /*
    float azimuth;
    float pitch;
    float roll;
    */
    MainActivity parent;
    OutputStreamWriter outputStreamWriter;
    BufferedReader reader;
    private boolean stopRequest;

    public SensorThread(MainActivity activity, OutputStream oStream, InputStream iStream){
        parent = activity;
        outputStreamWriter = new OutputStreamWriter(oStream);
        reader = new BufferedReader(new InputStreamReader(iStream));
        stopRequest = false;
    }

    @Override
    public void run(){
        while(!stopRequest){
            try {
                String line = reader.readLine();
                parent.doCommand(line);
            } catch (IOException e) {
                Log.e(ERRTAG, "Error reading input stream");
                return;
            }
        }
        Log.i(INFOTAG, "Finish thread");
    }

    public void clean(){
        stopRequest = true;
        interrupt();
        try {
            Log.i(INFOTAG, "Before in stream close");
            reader.close();
            Log.i(INFOTAG, "Before out stream close");
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(ERRTAG, "Error closing streams");
        }
    }




    float[] mGravity = null;
    float[] mGeomagnetic = null;

    @Override
    public void onSensorChanged(SensorEvent event) {
          String message = ""; //previous vu statement commented by me

         if (event.sensor.getType()== Sensor.TYPE_ACCELEROMETER) {
            mGravity=event.values;
             /* //bharat  start
            String message = "";
            for(float eachAxis : event.values)
            {
                message=message + Float.toString(eachAxis) + ",";
            }
             Log.i("Accel", message);
             */ //bharat end  //(important for debugging)


             //message = Float.toString(event.values[0]) + "," + Float.toString(event.values[1]) + "," + Float.toString(event.values[2]);
        }
        if (event.sensor.getType()== Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
            /* //bharat  start
            String message = "";
            for(float eachAxis : event.values)
            {
                message=message + Float.toString(eachAxis) + ",";
            }
            Log.i("Magnetic", message);
            */ //bharat end  //(important for debugging)


            //message1 = Float.toString(event.values[0]) + "," + Float.toString(event.values[1]) + "," + Float.toString(event.values[2]);;
        }
        if(mGravity != null && mGeomagnetic != null){
            float R[] = new float[9];

            //Log.i("Bharat", "G: " + mGravity.length + ", M:" + mGeomagnetic.length);
            boolean success =  SensorManager.getRotationMatrix(R, null, mGravity, mGeomagnetic);


           if (success) {
              float orientation[] = new float[3];
              SensorManager.getOrientation(R, orientation);

               x = orientation[0];
               y = orientation[1];
               z = orientation[2];
               Log.i(INFOTAG, "x = " + x );
               Log.i(INFOTAG, Float.toString(y) );
               Log.i(INFOTAG,  "z " + z );
               message = Float.toString(x) + "," + Float.toString(y) + "," + Float.toString(z);
               /*
               azimuth = orientation[0];
               pitch = orientation[1];
               roll = orientation[2];
                Log.i(INFOTAG, "azimuth" + azimuth );
               Log.i(INFOTAG, "pitch" + pitch );
               Log.i(INFOTAG, "roll" + roll );
               */

                /* //bharat  start
                Log.i("orientation","Length:" + R.length);
                String message = "";

                for(float eachAxis : R)
                {
                    message=message + Float.toString(eachAxis) + ",";
                }

                Log.i("orientation", message);
                */ //bharat end  //(important for debugging)

            }

        }



       // if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
       //     message = event.sensor.getType() + "," + Long.toString(event.timestamp) + "," + Float.toString(event.values[0]) + "," + Float.toString(event.values[1]) + "," + Float.toString(event.values[2]) + "\n";
       // }else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
       //     message = event.sensor.getType() + "," + Long.toString(event.timestamp) + "," + Float.toString(event.values[0]) + "," + Float.toString(event.values[1]) + "," + Float.toString(event.values[2]) + "\n";
        //}else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
           // message = event.sensor.getType() + "," + Long.toString(event.timestamp) + "," + Float.toString(event.values[0]) + "," + Float.toString(event.values[1]) + "," + Float.toString(event.values[2]) + "\n";
       // }else if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
         //   message = event.sensor.getType() + "," + Long.toString(event.timestamp) + "," + Float.toString(event.values[0]) + "," + Float.toString(event.values[1]) + "," + Float.toString(event.values[2]) + "\n";
        else {
           // message = "";
            Log.e(ERRTAG, "could not get rotation matrix Bharat");
        }
        try {
             outputStreamWriter.write(message);

            outputStreamWriter.flush();
        }catch(IOException e) {
            Log.e(ERRTAG, "Error writing output stream");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
