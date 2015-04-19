package uk.co.derekross.second_app.retrofit;

import java.util.ArrayList;

/**
 * Created by derek_000 on 19/04/2015.
 */
public class Model {
   private ArrayList<ImageData> data = new ArrayList<ImageData>();
    private boolean success;
    private int status;

    public Model(){

    }

    public ArrayList<ImageData> getData() {
        return data;
    }

    public void setData(ArrayList<ImageData> data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
