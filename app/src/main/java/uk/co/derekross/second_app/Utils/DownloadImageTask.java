package uk.co.derekross.second_app.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by derek_000 on 19/04/2015.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView mImageView;

    public DownloadImageTask(ImageView mImageView) {
        this.mImageView = mImageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String urlImage = params[0];
        Bitmap ImageBitmap = null;

        try {
            InputStream in = new URL(urlImage).openStream();
            ImageBitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return ImageBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        mImageView.setImageBitmap(bitmap);
    }



}
