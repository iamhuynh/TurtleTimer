package com.huynhhoang.turtletimer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION_CODES.N;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int previous_notification_interrupt_setting = 3;
    private long timeCountInMilliSeconds = 1 * 60000;
    private int getAlarmSoundSelected = 0;
    private int getAutoTextSelected = 0;
    private final static int MAX_VOLUME = 100;
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;


    private long getTimeLeft;
    private int initHour = 0;
    private int initMin = 0;
    private int initSec = 0;
    private int timerHourLeft = 0;
    private int timerMinuteLeft = 0;
    private int timerSecondLeft = 0;
    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private MediaPlayer alarmSound = null;
    private ProgressBar progressBarCircle;
    private TextView textViewTime;
    private CountDownTimer countDownTimer;
    private Button buttonStartStop;
    private Button buttonCancel;
    private Button buttonCancelTemp;
    private TextView buttonContactList;
    private TextView buttonIgnoreList;
    private NumberPicker pickHour;
    private NumberPicker pickMin;
    private NumberPicker pickSec;
    private LinearLayout initLayout;
    private RelativeLayout timerLayout;
    private Spinner alarmSoundSpinner;
    private Spinner alertSpinner;
    private Spinner autoTextSpinner;
    private Vibrator vibratePhone;
    private TextView textAlarmTime;
    private TextView textAmPm;
    private TextView textAlarmTime2;
    private TextView textAmPm2;
    private EditText autoTextEditBox;
    private Button setAutoTextButton;
    private Button cancelAutoTextButton;
    private TextView textTextReply;
    AudioManager mAudioManager;
    int originalVolume;
    private NotificationManager notificationManager;



    SmsManager smsManager = SmsManager.getDefault();
    private static MainActivity inst;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }




    /**
     * changes numberpicker text color
     */
    public static boolean setNumPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException e) {
                    Log.w("setNumPickerTextColor", e);
                } catch (IllegalAccessException e) {
                    Log.w("setNumPickerTextColor", e);
                } catch (IllegalArgumentException e) {
                    Log.w("setNumPickerTextColor", e);
                }
            }
        }
        return false;
    }


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

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= N
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Turtle Timer needs permissions to access the " +
                    "device\'s \"Do Not Disturb\" feature to work as intended." );
                    alertDialogBuilder.setPositiveButton("Grant",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                    Intent intent = new Intent(
                                            android.provider.Settings
                                                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                                    startActivity(intent);

                                }
                            });

            alertDialogBuilder.setNegativeButton("Ignore",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            alertDialogBuilder.setCancelable(false);

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSIONS_REQUEST);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }



        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);


        textAlarmTime.setText(setClockTime(initHour, initMin, initSec));

        /***************               Alarm Spinner Adapter              *****************/

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.alarmArray, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        alarmSoundSpinner.setAdapter(adapter);


        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        getAlarmSoundSelected = preferences.getInt("sharedAlarmSound", 0);

        alarmSoundSpinner.setSelection(getAlarmSoundSelected);

        alarmSoundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                //use this if i want to get the string
                // getAlarmSoundSelected = parent.getItemAtPosition(position).toString();
                // use this to get the value in the array of the selected spinner
                getAlarmSoundSelected = position;

                editor.putInt("sharedAlarmSound", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });








        /***************         Emergency Alert Spinner Adapter            *****************/

        ArrayAdapter adapterAlert = ArrayAdapter.createFromResource(this, R.array.alertArray, R.layout.spinner_item);
        adapterAlert.setDropDownViewResource(R.layout.spinner_dropdown_item);
        alertSpinner.setAdapter(adapterAlert);


        /***************          Auto Text Spinner Adapter              *****************/

        ArrayAdapter adapterText = ArrayAdapter.createFromResource(this, R.array.autoTextArray, R.layout.spinner_item);
        adapterText.setDropDownViewResource(R.layout.spinner_dropdown_item);
        autoTextSpinner.setAdapter(adapterText);

        getAutoTextSelected = preferences.getInt("sharedAutoText", 0);

        autoTextSpinner.setSelection(getAutoTextSelected);

        autoTextSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                //use this if i want to get the string
                // getAlarmSoundSelected = parent.getItemAtPosition(position).toString();
                // use this to get the value in the array of the selected spinner
                getAutoTextSelected = position;

                if (getAutoTextSelected == 6) {
                    // switch the spinner to edit custom text
                    Log.d("custom1", preferences.getString("sharedCustom"+getAutoTextSelected, ""));
//                    autoTextEditBox.setText(preferences.getString("sharedCustom"+getAutoTextSelected, ""));
                    autoTextEditBox.setText(preferences.getString("sharedCustom"+getAutoTextSelected, ""));
                    switchSpinnerToCustom();

                }

                if (getAutoTextSelected == 7) {
                    // switch the spinner to edit custom text
                    Log.d("custom2", preferences.getString("sharedCustom"+getAutoTextSelected, ""));
                    autoTextEditBox.setText(preferences.getString("sharedCustom"+getAutoTextSelected, ""));
                    switchSpinnerToCustom();
                }

                if (getAutoTextSelected == 8) {
                    // switch the spinner to edit custom text
                    Log.d("custom3", preferences.getString("sharedCustom"+getAutoTextSelected, ""));
                    autoTextEditBox.setText(preferences.getString("sharedCustom"+getAutoTextSelected, ""));
                    switchSpinnerToCustom();
                }



                if (getAutoTextSelected == 9) {
                    // switch the spinner to edit custom text
                    Log.d("custom4", preferences.getString("sharedCustom"+getAutoTextSelected, ""));
                    autoTextEditBox.setText(preferences.getString("sharedCustom"+getAutoTextSelected, ""));
                    switchSpinnerToCustom();
                }


                editor.putInt("sharedAutoText", position);
                editor.apply();


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }


        });

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
        timerLayout = (RelativeLayout) findViewById(R.id.timerLayout);
        alarmSoundSpinner = (Spinner) findViewById(R.id.alarmSoundSpinner);
        alertSpinner = (Spinner) findViewById(R.id.alertSpinner);
        autoTextSpinner = (Spinner) findViewById(R.id.autoTextSpinner);
        vibratePhone = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        textAlarmTime = (TextView) findViewById(R.id.textAlarmTime);
        textAmPm = (TextView) findViewById(R.id.textAmPM);
        textAlarmTime2 = (TextView) findViewById(R.id.textAlarmTime2);
        textAmPm2 = (TextView) findViewById(R.id.textAmPM2);
        autoTextEditBox = (EditText) findViewById(R.id.autoTextEditBox);
        setAutoTextButton = (Button) findViewById(R.id.setAutoTextButton);
        cancelAutoTextButton = (Button) findViewById(R.id.cancelAutoTextButton);
        textTextReply = (TextView) findViewById(R.id.textTextReply);
        buttonContactList = (TextView) findViewById(R.id.buttonContactList);
        buttonIgnoreList = (TextView) findViewById(R.id.buttonIgnoreList);
    }

    /**
     * method to initialize the click listeners
     */
    private void initListeners() {
        buttonStartStop.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
        cancelAutoTextButton.setOnClickListener(this);
        setAutoTextButton.setOnClickListener(this);
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



        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        initHour = preferences.getInt("sharedHour", 0);

        pickHour.setValue(initHour);

        //Set a value change listener for NumberPicker
        pickHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //set initial hour to the new value
                initHour = newVal;

                editor.putInt("sharedHour", newVal);
                editor.apply();


                // change the top clock time
                textAlarmTime.setText(setClockTime(initHour, initMin, initSec));
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        initMin = preferences.getInt("sharedMin", 0);

        pickMin.setValue(initMin);

        //Set a value change listener for NumberPicker
        pickMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //set initial min to the new value
                initMin = newVal;

                editor.putInt("sharedMin", newVal);
                editor.apply();


                // change the top clock time
                textAlarmTime.setText(setClockTime(initHour, initMin, initSec));
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        initSec = preferences.getInt("sharedSec", 0);

        pickSec.setValue(initSec);

        //Set a value change listener for NumberPicker
        pickSec.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //set initial hour to the new value
                initSec = newVal;

                editor.putInt("sharedSec", newVal);
                editor.apply();


                // change the top clock time
                textAlarmTime.setText(setClockTime(initHour, initMin, initSec));
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
            case R.id.cancelAutoTextButton:
                cancelAutoTB();
                break;
            case R.id.setAutoTextButton:
                setAutoTB();
                break;
        }
    }

    /**
     * method to switch screens to contact list
     */

    public void onGoToContactList(View view) {

        Intent getContactScreenIntent = new Intent(this, ContactScreen.class);

        final int result = 1;

        getContactScreenIntent.putExtra("CallingActivity", "MainActivity");

        startActivityForResult(getContactScreenIntent, result);


    }

    /**
     * method to switch screens to block list
     */


    public void onGoToIgnoreList(View view) {

        Intent getIgnoreScreenIntent = new Intent(this, IgnoreScreen.class);

        final int result = 1;

        getIgnoreScreenIntent.putExtra("CallingActivity", "MainActivity");

        startActivityForResult(getIgnoreScreenIntent, result);


    }


    /**
     * method to start and stop count down timer
     */
    private void startStop() {
        if (timerStatus == TimerStatus.STOPPED) {
            /**
             * When the timer is at the initial screen, initializes values according
             * to the number picker.
             */


            // call to initialize the timer values
            setTimerValues();
            // call to initialize the progress bar values
            setProgressBarValues();
            // changing start text to pause text when clicked
            buttonStartStop.setText("PAUSE");
            // changing the timer status to started
            timerStatus = TimerStatus.STARTED;
            // call to initialize clock time
            textAlarmTime2.setText(setClockTime(initHour, initMin, initSec));

            //set visibility of initLayout to Gone; make progressbar visible
            switchInitToTimer();
            cancelTimerGone();

            //check if timer was set (has to be greater than 0 seconds)
            if (timeCountInMilliSeconds == 0) {
                timeCountInMilliSeconds = 1;
            }

            startCountDownTimer();

        } else if (timerStatus == TimerStatus.STARTED) {
            /**
             * When the Timer Status is active, pauses the timer
             */

            // change PAUSE text to START when clicked
            buttonStartStop.setText("CONTINUE");
            // changing the timer status to stopped
            timerStatus = TimerStatus.PAUSED;
            cancelTimerVisible();
            stopCountDownTimer();
            buttonContactList.setEnabled(false);
            buttonIgnoreList.setEnabled(false);

        } else if (timerStatus == TimerStatus.PAUSED) {

            /*
             * When the timer is PAUSED
             * unpauses the timer by:
             * changing status from PAUSE to STARTED
             * disabling the cancel button, continue count down timer
             */

            buttonStartStop.setText("PAUSE");
            timerStatus = TimerStatus.STARTED;
            msUntilFinish(getTimeLeft);
            textAlarmTime2.setText(setClockTime(timerHourLeft, timerMinuteLeft, timerSecondLeft));
            cancelTimerGone();
            buttonContactList.setEnabled(true);
            buttonIgnoreList.setEnabled(true);
            continueCountDownTimer();

        } else if (timerStatus == TimerStatus.STOPALARM) {

            /**
             * When the timer finishes and you click the STOP ALARM button
             * changes the status from STOPALARM to START
             * calls cancelTimer() which returns timer to initial state (initial state of app)
             */
            buttonStartStop.setText("START");
            timerStatus = TimerStatus.STOPPED;
            stopSound();
            enableSpinners();
            cancelTimer();
            buttonContactList.setEnabled(true);
            buttonIgnoreList.setEnabled(true);
        }

    }

    /**
     * When custom auto text reply is initiated, sets the user input to custom string
     * need to do:
     * make list into a hashmap?
     * able to change custom -> (custom1) user entered text here
     * remove custom
     * add custom1 in it's place
     */
    public void setAutoTB() {
        String getCustomInput = autoTextEditBox.getText().toString();

        Log.d("TestHere", getCustomInput);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putString("sharedCustom"+getAutoTextSelected, getCustomInput);
        editor.apply();
        switchCustomToSpinner();
    }

    /**
     * When custom auto reply is initiated, give user the ability to cancel (erases custom)
     */
    public void cancelAutoTB() {
        switchCustomToSpinner();
    }


    /**
     * method to switch layout from TimerLayout to InitialLayout
     */
    private void switchInitToTimer() {
        initLayout.setVisibility(View.GONE);
        timerLayout.setVisibility(View.VISIBLE);
    }

    /**
     * method to switch layout from initialLayout to TimerLayout
     */
    private void switchTimerToInit() {
        timerLayout.setVisibility(View.GONE);
        initLayout.setVisibility(View.VISIBLE);
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

        //disable spinners
        disableSpinners();
        buttonContactList.setEnabled(false);
        buttonIgnoreList.setEnabled(false);
        //disable sound
        SilencePhone();

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
                // save the time left until finish
                getTimeLeft = millisUntilFinished;

            }

            @Override
            public void onFinish() {

                //enable sound
                UnsilencePhone();

                // reset text view and progress bar
                textViewTime.setText("00:00:00");
                progressBarCircle.setProgress(0);
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
                // cancel count down timer
                stopAlarm();
                // Toast "Timer Finished!" to the bottom
                Toast toast = Toast.makeText(getApplicationContext(), "Timer Finished!",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                //chooses sound to play when finished
                managerOfSound(getAlarmSoundSelected);

            }

        }.start();
        countDownTimer.start();
    }

    /**
     * method to stop count down timer
     */
    private void stopCountDownTimer() {
        //enable sounds
        UnsilencePhone();
        //disable spinners and pause countdown
        enableSpinners();
        countDownTimer.cancel();
    }


    /**
     * method to continue count down timer
     */
    private void continueCountDownTimer() {
        //disable sounds
        SilencePhone();
        //disable spinners
        disableSpinners();
        buttonContactList.setEnabled(false);
        buttonIgnoreList.setEnabled(false);

        // sets the countDownTimer to wherever onTick was paused
        countDownTimer = new CountDownTimer(getTimeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //sets text and progressbarcircle
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
                // save the current time
                getTimeLeft = millisUntilFinished;
                // sets the clockFinish to however many minutes left
                msUntilFinish(getTimeLeft);
                textAlarmTime2.setText(setClockTime(timerHourLeft, timerMinuteLeft, timerSecondLeft));

            }

            @Override
            public void onFinish() {
                //enable sound
                UnsilencePhone();
                // reset text view and progress bar
                textViewTime.setText("00:00:00");
                progressBarCircle.setProgress(0);
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
                // cancel count down timer
                stopAlarm();
                // Toast "Timer Finished!" to the bottom
                Toast toast = Toast.makeText(getApplicationContext(), "Timer Finished!",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                //chooses sound to play when finished
                // why this no work
                managerOfSound(getAlarmSoundSelected);

            }

        }.start();
        countDownTimer.start();
    }

    /**
     * method to cancel count down timer
     */
    private void cancelTimer() {

        //enable sound
        UnsilencePhone();
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

        switchTimerToInit();
        cancelTimerGone();

    }

    private void stopAlarm() {
        buttonStartStop.setText("STOP ALARM");
        timerStatus = TimerStatus.STOPALARM;
    }

    private void cancelTimerVisible() {
        buttonCancelTemp.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.VISIBLE);
    }

    private void cancelTimerGone() {
        buttonCancelTemp.setVisibility(View.VISIBLE);
        buttonCancel.setVisibility(View.GONE);
        buttonContactList.setEnabled(true);
        buttonIgnoreList.setEnabled(true);
    }


    /**
     * method to set circular progress bar values
     */
    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
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
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /**
     * Manager of Sounds
     */
    protected void managerOfSound(int theSound) {

        // set the volume to what we want it to be.  In this case it's max volume for the alarm stream.
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * .7), 0);

        if(alarmSound != null) {
            try{
                alarmSound.stop(); //error
                alarmSound.reset();
                alarmSound.release();
            }catch(Exception e){
                Log.d("Nitif Activity", e.toString());
            }
        }


        if (theSound == -1) {
            alarmSound = null;
        } else if (theSound == 0)
            alarmSound = MediaPlayer.create(this, R.raw.synthesizer);
        else if (theSound == 1)
            alarmSound = MediaPlayer.create(this, R.raw.morning);
        else if (theSound == 2)
            alarmSound = MediaPlayer.create(this, R.raw.rhythm);
        else if (theSound == 3)
            alarmSound = MediaPlayer.create(this, R.raw.executive);
        else if (theSound == 4) {
            long[] pattern = {0, 1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000,
                    1500, 1000, 1250, 1000, 1250, 1000, 1250, 1000};
            vibratePhone.vibrate(pattern, -1);
        } else {
            alarmSound = null;
        }

        // only start an alarmSound when a sound is selected
        // need to change this if adding more sounds, starting a alarmSound set to null
        // will crash your app
        if (theSound >= 0 && theSound <= 3) {
            alarmSound.setLooping(true);
            alarmSound.start();
        }

    }

    protected void stopSound() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        vibratePhone.cancel();

        if (alarmSound != null) {
            try {
                alarmSound.setLooping(false);
                if (alarmSound.isPlaying()) {
                    alarmSound.stop();
                }
            }
            catch(Exception e){
                Log.d("Alarm Sound Activity", e.toString());
            }
        }

    }

    private void disableSpinners() {
        alarmSoundSpinner.setEnabled(false);
        alertSpinner.setEnabled(false);
        autoTextSpinner.setEnabled(false);


    }

    private void enableSpinners() {
        alarmSoundSpinner.setEnabled(true);
        alertSpinner.setEnabled(true);
        autoTextSpinner.setEnabled(true);
    }

    public void switchCustomToSpinner() {
        autoTextSpinner.setVisibility(View.VISIBLE);
        textTextReply.setVisibility(View.VISIBLE);
        autoTextEditBox.setVisibility(View.GONE);
        setAutoTextButton.setVisibility(View.GONE);
        cancelAutoTextButton.setVisibility(View.GONE);
        enableSpinners();
        buttonStartStop.setEnabled(true);
        buttonCancel.setEnabled(true);
    }

    /**
     * Switches text reply spinner to editText so user can set custom text message
     */
    public void switchSpinnerToCustom() {
        autoTextSpinner.setVisibility(View.GONE);
        textTextReply.setVisibility(View.GONE);
        autoTextEditBox.setVisibility(View.VISIBLE);
        setAutoTextButton.setVisibility(View.VISIBLE);
        cancelAutoTextButton.setVisibility(View.VISIBLE);
        disableSpinners();
        buttonStartStop.setEnabled(false);
        buttonCancel.setEnabled(false);
    }

    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d:%02d:%02d",
                MILLISECONDS.toHours(milliSeconds),
                MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(MILLISECONDS.toHours(milliSeconds)),
                MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(MILLISECONDS.toMinutes(milliSeconds)));

        return hms;

    }

    private int msUntilFinish(long millisUntilFinish) {
        timerHourLeft = (int) TimeUnit.MILLISECONDS.toHours(millisUntilFinish);
        timerMinuteLeft = (int) TimeUnit.MILLISECONDS.toMinutes(millisUntilFinish) - (timerHourLeft * 60);
        timerSecondLeft = (int) TimeUnit.MILLISECONDS.toSeconds(millisUntilFinish) - (timerMinuteLeft * 60);

        return timerHourLeft;
    }

    /*
     * class to set the top clock time in the app
     * @param addHour, addMinute // taken from global variables
     * when the numberPicker changes, it changes the clock time
     */
    private String setClockTime(int addHour, int addMinute, int addSecond) {

        // initialize what the current time of day is
        int setHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int setMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int setSecond = Calendar.getInstance().get(Calendar.SECOND);
        // 12 hour day; split for formating reasons
        int standardClock = 12;

        //sets the current hour + the hours we want to add
        setHour = setHour + addHour;

        // checks if the current second + added seconds is greater than 60
        // if it is, add 1 to the minute and get seconds % 60
        if (setSecond + addSecond >= 60) {
            setSecond = (setSecond + addSecond) % 60;
            setMinute = setMinute + 1;
        } else {
            setSecond = setSecond + addMinute;
        }


        // checks if the current minute + added minutes is greater than 60
        // if it is, add 1 to the hour and get minute % 60
        if (setMinute + addMinute >= 60) {
            setMinute = (setMinute + addMinute) % 60;
            setHour = setHour + 1;
        } else {
            setMinute = setMinute + addMinute;
        }


        // sets AM/PM depending on the time of day
        if ((setHour / standardClock) == 1 || (setHour / standardClock == 3)) {
            textAmPm.setText("PM");
            textAmPm2.setText("PM");
        } else {
            textAmPm.setText("AM");
            textAmPm2.setText("AM");
        }

        // checks if the hour is 12. if not; set the hour in standard time formatting
        if (setHour % 12 == 0) {
            setHour = 12;
        } else {
            setHour = setHour % standardClock;
        }

        // throws string so that it can set the text
        String hm = String.format("%02d:%02d",
                setHour, setMinute);

        return hm;

    }


    public void onClickCancelAutoText(View view) {
    }

    private enum TimerStatus {
        STARTED,
        PAUSED,
        STOPPED,
        STOPALARM
    }

    private void SilencePhone() {

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            previous_notification_interrupt_setting = notificationManager.getCurrentInterruptionFilter();
            Log.d("TAG", "What is this" + previous_notification_interrupt_setting);
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        }
        else{
            AudioManager am = (AudioManager) getBaseContext().getSystemService(AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    private void UnsilencePhone() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        }
        else{
            AudioManager am = (AudioManager) getBaseContext().getSystemService(AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }


    }

    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
//                refreshSmsInbox();
            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}