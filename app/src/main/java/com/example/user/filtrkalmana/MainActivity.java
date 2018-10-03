package com.example.user.filtrkalmana;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    Sensor sensorAccelerometr;
    static int element = 0;
    static int n, k = 0;
    static double x_Low, y_Low, z_Low, x0, y0, z0, z02 = 0;
    double dt = (1.0 / 50.0);
    double  RC = 0.35;
     double alpha = dt / (RC + dt);



    GraphView graph;
    private double graph2LastXValue = 5d;
    private double graph2LastYValue = 5d;
    private double graph2LastZValue = 5d;
    private Double[] dataPoints;
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> seriesX;
    LineGraphSeries<DataPoint> seriesZ;
    LineGraphSeries<DataPoint> seriesXX;
    LineGraphSeries<DataPoint> seriesYY;
    LineGraphSeries<DataPoint> seriesZZ;
    private Thread thread;
    private boolean plotData = true;
    float xx;
    float yy;
    float zz;
    private float ALPHA = 0.25f;
    private boolean state;
    private int timer=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        state = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometr = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // acc_x = (acc_x_raw * vdd - acc_zero) / acc_sens;



        graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0),
        });
        series.setColor(Color.GREEN);

        seriesX = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0),

        });
        seriesX.setColor(Color.BLACK);

        seriesZ = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0),
        });
        seriesZ.setColor(Color.RED);

        graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0),
        });
        series.setColor(Color.BLUE);

        seriesXX = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0),

        });
        seriesXX.setColor(Color.YELLOW);

        seriesZZ = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0),
        });
        seriesZZ.setColor(Color.LTGRAY);


        seriesYY=new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0,0),
        });
        seriesYY.setColor(Color.MAGENTA);

        graph.addSeries(seriesXX);
        graph.addSeries(seriesYY);
        graph.addSeries(seriesZZ);
        graph.addSeries(seriesX);
        graph.addSeries(series);
        graph.addSeries(seriesZ);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(20);
        feedMultiple();
    }
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public void addEntry(SensorEvent event) {
        /*     LineGraphSeries<DataPoint> series = new LineGraphSeries<>();*/
        float[] values = event.values;
        // Movement
        float x = values[0];
        System.out.println(x);
        float y = values[1];
        System.out.println(y);
        float z = values[2];
        System.out.println(z);

        if (state) {
            timer++;
            if(timer % 5 == 0) {
                System.out.println(timer);
                // saveText(event);
            }
        }


        graph2LastXValue += 1d;
        graph2LastYValue +=1d;
        graph2LastZValue += 1d;

        x_Low = ((alpha * values[0]) + (1.0 - alpha) * x0);
        y_Low = ((alpha * values[1]) + (1.0 - alpha) * y0);
        z_Low = ((alpha * values[2]) + (1.0 - alpha) * z0);
        x0 = x_Low;
        y0 = y_Low;
        z0 = z_Low;

        // xx = (float) (On_1 + altha * (x - On_1));
        //  yy = (float) (On_1 + altha * (y - On_1));
        // zz = (float) (On_1 + altha * (z - On_1));

        //  series.appendData(new DataPoint(graph2LastYValue, y), true, 20);
        seriesX.appendData(new DataPoint(graph2LastXValue, x), true, 20);
        // seriesZ.appendData(new DataPoint(graph2LastZValue, z), true, 20);
        seriesXX.appendData(new DataPoint(graph2LastXValue,x_Low), true,20);
        // seriesYY.appendData(new DataPoint(graph2LastYValue,yy), true,20);
        //  seriesZZ.appendData(new DataPoint(graph2LastZValue,zz), true,20);
        //   graph.addSeries(series);
        graph.addSeries(seriesX);
        graph.addSeries(seriesXX);
//        graph.addSeries(seriesZ);
//        graph.addSeries(seriesYY);
//        graph.addSeries(seriesZZ);

    }




    private void addDataPoint(double acceleration) {
        dataPoints[499] = acceleration;
    }

    private void feedMultiple() {

        if (thread != null) {
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    plotData = true;
                    try {
                        Thread.sleep(200);
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }



    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
        mSensorManager.unregisterListener(this);

    }




    public void onSensorChanged(final SensorEvent evt) {
        if (plotData) {
//            addEntry(event);
            //
            new Thread(new Runnable() {

                @Override
                public void run() {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry(evt);
                        }
                    });


                }

            }).start();

            //
            plotData = false;
        }


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, sensorAccelerometr, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        mSensorManager.unregisterListener(MainActivity.this);
        thread.interrupt();
        super.onDestroy();
    }




}