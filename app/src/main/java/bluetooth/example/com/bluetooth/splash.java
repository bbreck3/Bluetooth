package bluetooth.example.com.bluetooth;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;


public class splash extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         *          The main screen as now switched to this screen...so the name of the app is now "splash",
         *          I will fix that later, that is a small thing that I am not concerned with at the moment
         *
         *
         *
         */

       setContentView(R.layout.activity_splash);

       // imageview to hold the vcu logo
        final ImageView iv = (ImageView) findViewById(R.id.imageView);
        //ProgressBAr to implement a progressbar to be used to display the loading progress
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        //delay time to start the counter
        final int secondsDelayed = 1;
        //TextView to display the value of the progress loaded...is not working correctly for some reason.....
       // final TextView loadVal = (TextView)findViewById(R.id.load_val);

        /**
         *
         *      The below implements a handler that handles the loadtime....you only want the screen be displayed but for a brief amount of seconds
         *      before you load the main screen
         *
         *      The Handler uses a thread which must be implemented with a Runnable instance Object.
         *
         *
         */
        new Handler().postDelayed(new Runnable() {
            public void run() { // runs the runnable thread --> Makes the screen display
                startActivity(new Intent(splash.this, MainActivity.class));  // Starts the activity on the thread;
                                                                            // --> first parameter: the current screen to display--> the splash activity
                                                                            // --> second parameter --> after the thread is done the next screen to load --> MainActivity

                 //sets the value of the progressbar to the value of secondDelayed
                /**
                 *      The progressbar is the blue spinner that circles under the VCU logo
                 *
                 *      Its range is from 1 - 10000 --> computer judge time in milliseconds
                 *      (Technically nanoseconds but the difference from milliseconds to nanoseconds os margin this purpose)
                 *      1000 milliseconds in a second ---> so loads the splash screen for 10 seconds and then proceeds to the main screen
                 *
                 */
                progressBar.setProgress(secondsDelayed);

                /**
                 *
                 *      My attempt to make give the loading a digital value...in progress
                 *
                 */
                //loadVal.setText(Integer.toString(secondsDelayed));
               // int load_delay_string_val=secondsDelayed/100;

                finish(); // after 10 seconds --> stop the current activity
            }
        }, secondsDelayed * 10000); //--> how long to delay the activity -- by multiplying the secondsDelayed(defined as 1 above) by 10000 the duration is set to 10000 or 10 seconds


        /**
         *
         *              Below is the stuff that you did to make an animation to spin the  vcu logo displayed.....I could not get the vcu logo to spin for waiter ever reason
         *              so I went with a progress bar spinner that is implemented above.
         *
         *              I thought about deleted the below but wanted to wait to see what you wanted.
         *              Therefore, I left it in place although none of the code is active....
         *
         *
         *
         *
         *
         *
         *
         */

        final Animation an = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_shrink_fade_out_from_bottom);
        final Animation an2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_out);

       // iv.startAnimation(an);
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                int secondsDelayed = 1;
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(splash.this,MainActivity.class));
                        finish();
                    }
                }, secondsDelayed * 1000000000);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv.startAnimation(an2);
                finish();
                Intent i;
                i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
