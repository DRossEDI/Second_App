package uk.co.derekross.second_app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.derekross.second_app.Service.SlideShowService;
import uk.co.derekross.second_app.retrofit.RetrieveService;


public class MainActivity extends ActionBarActivity {

    private ImageView mImageView;
    final public int SHOW_PICTURE = 0;
    private boolean mBound = false;
    Messenger mServiceMessenger;

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case SlideShowService.BITMAP_TO_DISPLAY :
                    mImageView.setImageResource(android.R.color.transparent);
                    Log.e("Activity", "bitmap received");
                    Bitmap b = (Bitmap) msg.obj;
                    mImageView.setImageBitmap(b);
            }
        }
    }


    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageViewMain);


    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, SlideShowService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }





    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           mServiceMessenger = new Messenger(service);
            mBound = true;
            Message msg = Message.obtain(null, SlideShowService.RECEIVE_ACTIVITY_MESSENGER, mMessenger);
            try {
                mServiceMessenger.send(msg);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
            mBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void showPicture(Bitmap b){
        mImageView.setImageBitmap(b);
    }


}
