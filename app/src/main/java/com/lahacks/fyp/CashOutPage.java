package com.lahacks.fyp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class CashOutPage extends AppCompatActivity {
    public static final String TAG = "CashOutPage";
    private TextView text_view_countdown;
    private Button button_start_pause;
    private Button button_refresh;
    private CountDownTimer countdown_timer;

    private long startTimeInMillis;
    private boolean timerRunning;
    private long timeLeftInMillis;
    private long endTime;
    private long millisLeft;

    // app suspension declarations
    DevicePolicyManager dpm;
    DeviceAdminReceiver dar;
    Button killButton;
    Button unkillButton;
    private ComponentName compName;
    ImageView appSelection;
    public static final int RESULT_ENABLE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_out_page);

        // attaching views to timer stuff
        text_view_countdown = findViewById(R.id.text_view_countdown);
        button_start_pause = findViewById(R.id.button_start_pause);
        button_refresh = findViewById(R.id.button_refresh);

        // initialize device policy manager
        dpm = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        dar = new DeviceAdminReceiver();
        compName = new ComponentName(this, MyAdmin.class);

        // initialize buttons
        killButton = findViewById(R.id.killButton);
        unkillButton = findViewById(R.id.unkillButton);
        appSelection = findViewById(R.id.appSelection);

        // apps to ban/unban
        String[] listOfPackages = {"com.google.android.youtube", "com.zhiliaoapp.musically"};

        // move to app selection
        appSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CashOutPage.this, AppSelect.class);
                startActivity(intent);
            }
        });

        // logic for the killButton
        killButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                boolean active = dpm.isAdminActive(compName);

                // kill tik tok!
                if (active) {
                    try {
                        // suspend package
                        dpm.setPackagesSuspended(compName, listOfPackages, true);
                        Toast.makeText(CashOutPage.this, "Apps Successfully Blocked", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d(TAG, "Security Exception " + e);
                    }
                }

                // ask for admin privilege
                else {

                    // create intent to go to page where you ask for admin permission
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "We need " +
                            "this permission to block apps");
                    startActivityForResult(intent, RESULT_ENABLE);
                }
            }
        });

        unkillButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                boolean active = dpm.isAdminActive(compName);

                // unkill tik tok!
                if (active) {
                    try {
                        // suspend package
                        dpm.setPackagesSuspended(compName, listOfPackages, false);
                        Toast.makeText(CashOutPage.this, "Apps Successfully Unblocked", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d(TAG, "Security Exception " + e);
                    }
                }

                // ask for admin privilege
                else {

                    // create intent to go to page where you ask for admin permission
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "We need " +
                            "this permission to block apps");
                    startActivityForResult(intent, RESULT_ENABLE);
                }
            }
        });

        button_start_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        button_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* // Potential code for the implementation of new minutes
                if(newMins != 0) {
                    oldMillisLeft = millisLeft;
                    millisLeft = (newMins*60000) + oldMillisLeft;
                } else {
                    Toast noMin = Toast.makeText(CashOutPage.this, "You have no minutes!", Toast.LENGTH_SHORT);
                    noMin.setGravity(Gravity.CENTER, 0, 0);
                    noMin.show();
                }
                */

                if (millisLeft == 0) {
                    /*Toast noMin = Toast.makeText(CashOutPage.this, "You have no minutes!", Toast.LENGTH_SHORT);
                    noMin.setGravity(Gravity.CENTER, 0, 0);
                    noMin.show();
                    return;*/

                    /* FOR TESTING...
                    long newMins = 2;
                    millisLeft = (newMins * 60000);
                    */
                }
                setTime(millisLeft);
            }
        });
    }

    //setting time of timer seen by user...
    private void setTime(long milliseconds) {
        startTimeInMillis = milliseconds;
        resetTimer();
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMillis;

        countdown_timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                millisLeft = timeLeftInMillis; // updating the start time every tick - this prevents users from getting more mins if they don't have new minutes added
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                timerRunning = false;
                millisLeft = 0; // sets millisLeft to 0, since time has ran out
                updateButtons();
            }
        }.start();

        timerRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        countdown_timer.cancel();
        timerRunning = false;
        updateButtons();
    }

    private void resetTimer() {
        timeLeftInMillis = startTimeInMillis;
        updateCountDownText();
        updateButtons();
    }

    private void updateButtons() {
        if(timerRunning) {
            button_start_pause.setText("Pause");
            button_refresh.setVisibility(View.INVISIBLE);
        } else {
            button_start_pause.setText("Start");
            button_refresh.setVisibility(View.VISIBLE);
            if(timeLeftInMillis < 1000) {
                button_start_pause.setVisibility(View.INVISIBLE);
            } else {
                button_start_pause.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateCountDownText() {
        int hours = (int) (timeLeftInMillis/1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if(hours > 0){
            timeLeftFormatted = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        text_view_countdown.setText(timeLeftFormatted);
    }

    //For configuration changes & when app is started/closed
    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", millisLeft); // saves millisLeft..
        editor.putLong("timeLeft", timeLeftInMillis);
        editor.putBoolean("timerRunning", timerRunning);
        editor.putLong("endTime", endTime);

        editor.apply();

        if (countdown_timer != null) {
            countdown_timer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        timeLeftInMillis = prefs.getLong("timeLeft", startTimeInMillis);
        millisLeft = prefs.getLong("millisLeft", timeLeftInMillis);
        timerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateButtons();

        if (timerRunning) {
            endTime = prefs.getLong("endTime", 0);
            timeLeftInMillis = endTime - System.currentTimeMillis();
            if (timeLeftInMillis < 0) {
                timeLeftInMillis = 0;
                timerRunning = false;
                updateCountDownText();
                updateButtons();
            } else {
                startTimer();
            }
        }
    }
}