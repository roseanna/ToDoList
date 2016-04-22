package com.example.roseanna.sqllitetodolist;

import android.util.Log;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by roseanna on 3/19/16.
 */
public class ToDo implements Serializable {
    private String title;
    private String date;
    private String description;
    private boolean selected;

    public ToDo(String title, String desc, String date){
        this.title          = title;
        this.description    = desc;
        this.date           = date;
        this.selected       = false;
    }

    public ToDo(String title){
        this.title          = title;
        this.description    = "None";
        this.date           = new Date().toString();
        this.selected       = false;
    }

    public String getDescription(){
        return description;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getDate(){
        Log.i("getdate", date.toString());
        return date.toString();
    }
    public void set() {
        this.selected = true;
    }
    public void unset(){
        this.selected = false;
    }
    public boolean isSelected(){return selected;}

}