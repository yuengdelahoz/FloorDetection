package delahoz.floordetection.structured;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by yuengdelahoz on 10/9/15.
 */
public class Camera2Activity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        Button btn = (Button) findViewById(R.id.button);
        if(isMyServiceRunning(Camera2Service.class)) btn.setText("Stop");
        final Intent processImage = new Intent(this,Camera2Service.class);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button bt = (Button) v;
                if (bt.getText().toString().equals("Start")) {
                    startService(processImage);
                    bt.setText("Stop");
                }
                else {
                    stopService(processImage);
                    bt.setText("Start");
                }
            }
        });
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
