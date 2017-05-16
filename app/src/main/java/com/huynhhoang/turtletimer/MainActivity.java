package com.huynhhoang.turtletimer;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huynhhoang.turtletimer.R;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static android.R.attr.button;
import static android.R.color.white;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private long timeCountInMilliSeconds = 1 * 60000;

    private long curTime;
    private int initHour;
    private int initMin;
    private int initSec;

    private enum TimerStatus {
        STARTED,
        PAUSED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;

    private ProgressBar progressBarCircle;
    private TextView textViewTime;
    private CountDownTimer countDownTimer;
    private Button buttonStartStop;
    private Button buttonCancel;
    private Button buttonCancelTemp;
    private NumberPicker pickHour;
    private NumberPicker pickMin;
    private NumberPicker pickSec;
    private LinearLayout initLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // method call to initialize the views
        initViews();
        // method call to initialize the listeners
        initListeners();

        initPickHour();
        initPickMin();
        initPickSec();

    }

    /**
     * method to initialize the views
     */
    private void initViews() {
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancelTemp = (Button) findViewById(R.id.buttonCancelTemp);
        pickHour = (NumberPicker) findViewById(R.id.pickHour);
        pickMin = (NumberPicker) findViewById(R.id.pickMin);
        pickSec = (NumberPicker) findViewById(R.id.pickSec);
        initLayout = (LinearLayout) findViewById(R.id.initLayout);
    }

    /**
     * method to initialize the click listeners
     */
    private void initListeners() {
        buttonStartStop.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    private void initPickHour() {
        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum and maximum value of NumberPicker
        pickHour.setMinValue(0);
        pickHour.setMaxValue(23);

        //Set numberpicker text and divider colors
        setNumPickerTextColor(pickHour, Color.WHITE);
        setDividerColor(pickHour, Color.CYAN);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        pickHour.setWrapSelectorWheel(false);

        //Makes all all numbers double digits
        pickHour.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });

        //Set a value change listener for NumberPicker
        pickHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //set initial hour to the new value
                initHour = newVal;
            }
        });
    }

    private void initPickMin() {
        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum and maximum value of NumberPicker
        pickMin.setMinValue(00);
        pickMin.setMaxValue(59);

        //Set numberpicker text and divider colors
        setNumPickerTextColor(pickMin, Color.WHITE);
        setDividerColor(pickMin, Color.CYAN);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        pickMin.setWrapSelectorWheel(true);

        //Makes all all numbers double digits
        pickMin.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });

        //Set a value change listener for NumberPicker
        pickMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //set initial minute to the new value
                initMin = newVal;
            }
        });
    }

    private void initPickSec() {
        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum and maximum value of NumberPicker
        pickSec.setMinValue(00);
        pickSec.setMaxValue(59);

        //Set numberpicker text and divider colors
        setNumPickerTextColor(pickSec, Color.WHITE);
        setDividerColor(pickSec, Color.CYAN);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        pickSec.setWrapSelectorWheel(true);

        //Makes all all numbers double digits
        pickSec.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });

        //Set a value change listener for NumberPicker
        pickSec.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //set initial min to the new value
                initSec = newVal;
            }
        });
    }

    /**
     * implemented method to listen clicks
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonStartStop:
                startStop();
                break;
            case R.id.buttonCancel:
                cancelTimer();
                break;
        }
    }


    /**
     * method to start and stop count down timer
     */
    private void startStop() {
        if (timerStatus == TimerStatus.STOPPED) {

            // call to initialize the timer values
            setTimerValues();
            // call to initialize the progress bar values
            setProgressBarValues();
            // changing start text to pause text when clicked
            buttonStartStop.setText("PAUSE");
            // changing the timer status to started
            timerStatus = TimerStatus.STARTED;
            // call to start the count down timer

            //set visibility of initLayout to Gone; make progressbar visible
            initLayout.setVisibility(View.GONE);
            proBarVisible();
            cancelTimerGone();

            //check if timer was set (has to be greater than 0 seconds)
            if (timeCountInMilliSeconds == 0){
                timeCountInMilliSeconds = 1;
            }

            startCountDownTimer();

        } else if (timerStatus == TimerStatus.STARTED){

            // change PAUSE text to START when clicked
            buttonStartStop.setText("CONTINUE");
            // changing the timer status to stopped
            timerStatus = TimerStatus.PAUSED;
            cancelTimerVisible();
            stopCountDownTimer();

        } else if (timerStatus == TimerStatus.PAUSED) {
            buttonStartStop.setText("PAUSE");
            timerStatus = TimerStatus.STARTED;
            cancelTimerGone();
            continueCountDownTimer();
        }

    }

    private void proBarVisible() {
        progressBarCircle.setVisibility(View.VISIBLE);
        textViewTime.setVisibility(View.VISIBLE);
    }

    private void proBarGone() {
        progressBarCircle.setVisibility(View.GONE);
        textViewTime.setVisibility(View.GONE);
    }

    /**
     * method to initialize the values for count down timer
     */
    private void setTimerValues() {
        int time = 0;
        //set time values
        time = (initHour * 60 * 60) + (initMin * 60) + (initSec);
        // assigning values after converting to milliseconds

        timeCountInMilliSeconds = time * 1000;
    }

    /**
     * method to start count down timer
     */
    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
                // save the current time
                curTime = millisUntilFinished;

            }

            @Override
            public void onFinish() {

                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
                // cancel count down timer
                cancelTimer();
            }

        }.start();
        countDownTimer.start();
    }

    /**
     * method to stop count down timer
     */
    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }


    /**
     * method to continue count down timer
     */
    private void continueCountDownTimer() {
        // sets the countDownTimer to wherever onTick was paused
        countDownTimer = new CountDownTimer(curTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //sets text and progressbarcircle
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
                // save the current time
                curTime = millisUntilFinished;

            }

            @Override
            public void onFinish() {

                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                // call to initialize the progress bar values
                setProgressBarValues();
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
            }

        }.start();
        countDownTimer.start();
    }

    /**
     * method to cancel count down timer
     */
    private void cancelTimer() {

        // call to initialize the timer values
        setTimerValues();
        // call to initialize the progress bar values
        setProgressBarValues();
        // set textViewTime to the initial minutes
        textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
        // changing start text to pause text when clicked
        buttonStartStop.setText("START");
        // changing the timer status to started
        timerStatus = TimerStatus.STOPPED;
        // call to start the count down timer
        countDownTimer.cancel();

        proBarGone();
        cancelTimerGone();
        initLayout.setVisibility(View.VISIBLE);

    }

    private void cancelTimerVisible() {
        buttonCancelTemp.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.VISIBLE);
    }

    private void cancelTimerGone() {
        buttonCancelTemp.setVisibility(View.VISIBLE);
        buttonCancel.setVisibility(View.GONE);
    }


    /**
     * method to set circular progress bar values
     */
    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }

    /**
     * changes numberpicker text color
     */
    public static boolean setNumPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNumPickerTextColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNumPickerTextColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNumPickerTextColor", e);
                }
            }
        }
        return false;
    }


    /**
     * changes numberpicker divider color
     */
    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }


    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;


    }


}