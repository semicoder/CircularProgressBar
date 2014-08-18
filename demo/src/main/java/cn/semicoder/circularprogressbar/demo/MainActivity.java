package cn.semicoder.circularprogressbar.demo;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.semicoder.circularprogressbar.CircularProgressBarView;


public class MainActivity extends ActionBarActivity {

    CircularProgressBarView mProgressBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBarView = (CircularProgressBarView) findViewById(R.id.progress);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBarView.setProgressColor(Color.parseColor("#ff223399"));
        mProgressBarView.setProcessingAnimation(false, new CircularProgressBarView.UpdateProgress() {
            public float updateAngle() {
                return percent2Angle(270);
            }
        });
        mProgressBarView.setProgress(270, true);
    }

    private int percent2Angle(int paramInt) {
        return paramInt * 360 / 100;
    }
}
