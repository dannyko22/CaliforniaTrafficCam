package com.californiatrafficcam.app;

/**
 * Created by Danny on 02/07/2014.
 */
public class CamData {

    // private variables
    int _id;
    String Name = "name";
    String Latitude = "Latitude";
    String Longitude = "Longitude";
    String URL = "URL";

    // constructor.  empty data.
    public CamData(){

    }


    public void setID(int id)
    {
        this._id = id;
    }

    public void setName(String _name)
    {
        this.Name = _name;
    }

    public void setLatitude(String _Latitude)
    {
        this.Latitude = _Latitude;
    }

    public void setLongitude(String _Longitude)
    {
        this.Longitude = _Longitude;
    }

    public void setURL(String _URL)
    {
        this.URL = _URL;
    }

    public int getID()
    {
        return _id;
    }

    public String getName()
    {
        return Name;
    }

    public String getLatitude()
    {
        return Latitude;
    }

    public String getLongitude()
    {
        return Longitude;
    }

    public String getURL()
    {
        return URL;
    }

}
