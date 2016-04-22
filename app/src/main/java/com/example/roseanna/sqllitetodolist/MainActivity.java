package com.example.roseanna.sqllitetodolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, Serializable {
    TaskAdapter adapter;
    ArrayList<ToDo> tasks = new ArrayList<>();
    public ListView todoLV;
    public EditText newTask;

    public Button addButton, delButton, clearButton, showButton, editButton;

    // For edit
    public boolean returned = false;
    public String newTitle;
    public int oldPosition;
    public String newDesc;
    public String oldDate;

    // For SQL stuff
    private static String tableName = "Todo_Table";
    private SQLiteDatabase sampleDB = null;
    private Cursor cursor           = null;

    // For Button clicks
    @Override
    public void onClick(View v) {
        if(!databaseEmpty()) {
            switch (v.getId()) {
                case R.id.addButton:
                    addClick();
                    break;
                case R.id.deleteButton:
                    deleteClick();
                    break;
                case R.id.clearButton:
                    clearClick();
                    break;
                case R.id.showButton:
                    showClick();
                    break;
                case R.id.editButton:
                    editClick();
                    break;
            }
            clearSelect();
        }
    }
    public void editClick(){
        int count = 0;
        for (int i = 0; i < tasks.size(); i++){
            if(tasks.get(i).isSelected()){
                oldPosition = i;
                count++;
            }
        }
        if (count != 1) {
            Toast.makeText(MainActivity.this, "CHOOSE ONE TASK!", Toast.LENGTH_SHORT).show();
            clearSelect();
            return;
        }
        ToDo toEdit         = tasks.get(oldPosition);
        Intent showActivity = new Intent(MainActivity.this, EditActivity.class);
        Bundle myBundle     = new Bundle();

        myBundle.putString("title", toEdit.getTitle());
        myBundle.putString("description", toEdit.getDescription());
        myBundle.putString("date", toEdit.getDate());

        showActivity.putExtras(myBundle);
        startActivityForResult(showActivity, 200);
    }
    public void showClick() {
        if(dbEmpty()){
            Toast.makeText(MainActivity.this, "Database Empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = 0;
        ToDo chosen = tasks.get(0);
        for(ToDo t : tasks){
            if(t.isSelected()) {
                chosen = t;
                count++;
            }
        }

        if (count != 1) {
            Toast.makeText(MainActivity.this, "CHOOSE ONE TASK!", Toast.LENGTH_SHORT).show();
            clearSelect();
            return;
        }

        Intent showActivity = new Intent(MainActivity.this, ShowActivity.class);
        Bundle myBundle     = new Bundle();
        myBundle.putString("date", chosen.getDate());
        myBundle.putString("title", chosen.getTitle());
        myBundle.putString("description", chosen.getDescription());

        showActivity.putExtras(myBundle);
        startActivityForResult(showActivity, 100);
    }

    public boolean dbEmpty(){
        Cursor cursor = sampleDB.rawQuery("Select * from " + tableName, null);
        if(cursor.getCount() == 0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
    public void clearClick() {
        sampleDB.execSQL("DROP TABLE " + tableName);
        sampleDB.execSQL("CREATE TABLE " + tableName +
                " (Title VARCHAR, " +
                "  Desc VARCHAR, " +
                "  Date VARCHAR);");
        updateList();
        adapter.notifyDataSetChanged();
    }
    public void deleteClick() {
        ArrayList<ToDo> rem = new ArrayList<ToDo>();
        for (ToDo t : tasks) {
            if (t.isSelected())
                rem.add(t);
        }
        for (ToDo a : rem) {
            deleteData(a.getDate());
        }
        updateList();
        if (rem.size() == 0) {
            Toast.makeText(MainActivity.this, "Choose something to delete", Toast.LENGTH_SHORT).show();
        }
    }
    public void addClick() {
        String input = newTask.getText().toString();
        if (!input.isEmpty()) {
            Date d = new Date();
            insertData(input, "None", String.valueOf(d));
            newTask.setText("");
            cursor.close();
        } else {
            Toast.makeText(MainActivity.this, "Add a task!", Toast.LENGTH_SHORT).show();
        }
    }

    // To clear out the selects on return from clicks
    public void clearSelect(){
        for(ToDo i: tasks) {
            i.unset();
        }
        updateList();
    }

    public boolean databaseEmpty(){
        cursor = sampleDB.rawQuery("SELECT Title, Desc, Date FROM " +
                tableName , null);
        if(cursor == null)
            return true;
        return false;
    }

    // Creates Table, sets buttons/items
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoLV      = (ListView) findViewById(R.id.todoLV);
        newTask     = (EditText) findViewById(R.id.newTaskField);

        addButton   = (Button) findViewById(R.id.addButton);
        delButton   = (Button) findViewById(R.id.deleteButton);
        editButton  = (Button) findViewById(R.id.editButton);
        showButton  = (Button) findViewById(R.id.showButton);
        clearButton = (Button) findViewById(R.id.clearButton);

        try{
            sampleDB = openOrCreateDatabase("NAME", MODE_PRIVATE, null);
            createTable();
        }catch(SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        }
    }

    private void createTable() {
        Log.d(getLocalClassName(), "in create table");
        sampleDB.execSQL("CREATE TABLE IF NOT EXISTS " + tableName +
                " (Title VARCHAR, " +
                "  Desc VARCHAR, " +
                "  Date VARCHAR);");
        Log.i("Created Table", "Done");
    }
    private void insertData(String title, String desc, String date) {
        ContentValues values = new ContentValues();
        values.put("Title", title);
        values.put("Desc", desc);
        values.put("Date", date);
        Log.i("Insert Data", title);
        sampleDB.insert(tableName, null, values);
        updateList();
    }

    private void deleteData(String date) {
        sampleDB.delete(tableName, "Date=?", new String[]{date});
    }
    private void editData(String title, String desc, String date){
        deleteData(date);
        insertData(title, desc, date);
    }

    public void updateList(){
        Log.i("update", "list");
        tasks.clear();
        cursor = sampleDB.rawQuery("SELECT Title, Desc, Date FROM " + tableName , null);
        if(cursor != null) {
            for(int i = 0; i < cursor.getCount(); i++){
                cursor.moveToPosition(i);
                String title    = cursor.getString(cursor.getColumnIndex("Title"));
                String desc     = cursor.getString(cursor.getColumnIndex("Desc"));
                String date     = cursor.getString(cursor.getColumnIndex("Date"));
                ToDo newTodo    = new ToDo(title, desc, date);
                tasks.add(newTodo);
                Log.i("update list", String.valueOf(tasks.size()));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        cursor = sampleDB.rawQuery("SELECT Title, Desc, Date FROM " +
                tableName , null);
        if(cursor != null) {
            cursor.moveToFirst();
            while(cursor.moveToNext()){
                String title    = cursor.getString(cursor.getColumnIndex("Title"));
                ToDo newTodo    = new ToDo(title);
                tasks.add(newTodo);
            }
            cursor.close();
            adapter = new TaskAdapter(this, tasks);
        }
        todoLV.setAdapter(adapter);
        addButton.setOnClickListener(this);
        delButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        showButton.setOnClickListener(this);
    }
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume(){
        super.onResume();
        updateList();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if(requestCode == 200){
                Bundle dataBundle   = data.getExtras();
                newTitle            = dataBundle.getString("newTitle");
                newDesc             = dataBundle.getString("newDesc");
                oldDate             = dataBundle.getString("oldDate");
                editData(newTitle, newDesc, oldDate);
                returned = true;
            }
        } catch (Exception e) {
            Log.i("ERROR", String.valueOf(requestCode));
        }
    }
}

