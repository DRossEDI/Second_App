package uk.co.derekross.second_app.retrofit;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.Response;
import uk.co.derekross.second_app.MainActivity;
import uk.co.derekross.second_app.Utils.DownloadImageTask;

/**
 * Created by derek_000 on 18/04/2015.
 */
public class RetrieveService extends AsyncTask<Void, Void,Void>{

    private Context mContext;
    private ImageView mImageView;


    public RetrieveService(Context c, ImageView i){
        mContext = c;
        mImageView = i;

    }




    private Model response;
    private ArrayList<String> ids = new ArrayList<String>();

    @Override
    protected Void doInBackground(Void... params) {


        RestAdapter imgurAdapter = new RestAdapter.Builder()
                .setEndpoint(RetroFitHelper.ImgurEndPoint)
                .build();


        response = imgurAdapter.create(RetroFitHelper.class).getSubReditData("gonewild");



        for(ImageData d : response.getData()){
            ids.add(d.getId());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);


        new DownloadImageTask(mImageView).execute("http://i.imgur.com/4SkRWxc.jpg");




    }
}
