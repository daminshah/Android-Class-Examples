package com.sargent.mark.todolist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.DBHelper;

import static com.sargent.mark.todolist.R.id.recyclerView;

public class MainActivity extends AppCompatActivity implements AddToDoFragment.OnDialogCloseListener, UpdateToDoFragment.OnUpdateDialogCloseListener, AdapterView.OnItemSelectedListener{

    private RecyclerView rv;
    private FloatingActionButton button;
    private static DBHelper helper;
    private Cursor cursor;
    private SQLiteDatabase db;
    ToDoListAdapter adapter;
    private final String TAG = "mainactivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       // setContentView(R.layout.fragment_to_do_adder)setContentView(R.layout.item);


        Log.d(TAG, "oncreate called in main activity");
        button = (FloatingActionButton) findViewById(R.id.addToDo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                AddToDoFragment frag = new AddToDoFragment();
                frag.show(fm, "addtodofragment");
            }
        });



        //To save the state of checkbox
//        final CheckBox checkBox=(CheckBox)findViewById(R.id.done);
//
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        final SharedPreferences.Editor editor = preferences.edit();
//        if(preferences.contains("checked") && preferences.getBoolean("checked",false) == true) {
//            checkBox.setChecked(true);
//        }else {
//            checkBox.setChecked(false);
//
//        }
//
//        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if(checkBox.isChecked()) {
//                    editor.putBoolean("checked", true);
//                    editor.apply();
//                }else{
//                    editor.putBoolean("checked", false);
//                    editor.apply();
//                }
//                editor.clear();
//                editor.commit();
//            }
//
//
//        });

            rv = (RecyclerView) findViewById(recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (db != null) db.close();
        if (cursor != null) cursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();
        cursor = getAllItems(db);

        adapter = new ToDoListAdapter(cursor, new ToDoListAdapter.ItemClickListener() {

            @Override
            public void onItemClick(int pos, String description, String duedate, String category, long id) {
                Log.d(TAG, "item click id: " + id);
                String[] dateInfo = duedate.split("-");
                int year = Integer.parseInt(dateInfo[0].replaceAll("\\s",""));
                int month = Integer.parseInt(dateInfo[1].replaceAll("\\s",""));
                int day = Integer.parseInt(dateInfo[2].replaceAll("\\s",""));

                FragmentManager fm = getSupportFragmentManager();

                UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description,category, id);
                frag.show(fm, "updatetodofragment");



            }
        });




        rv.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                Log.d(TAG, "passing id: " + id);
                removeToDo(db, id);
                adapter.swapCursor(getAllItems(db));
            }
        }).attachToRecyclerView(rv);
    }

    protected static void updatetodostatus(int pos, long id, boolean done)
    {
        //helper = new DBHelper(this);
       SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DONE, (done ? 1 : 0));
        db.update(Contract.TABLE_TODO.TABLE_NAME, cv,
                Contract.TABLE_TODO._ID + "=" + id, null);

    }
    @Override
    public void closeDialog(int year, int month, int day, String description, String category) {
        addToDo(db, description, formatDate(year, month, day),category);
        cursor = getAllItems(db);
        adapter.swapCursor(cursor);
    }

    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }



    private Cursor getAllItems(SQLiteDatabase db) {
        return db.query(
                Contract.TABLE_TODO.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE
        );
    }

    private long addToDo(SQLiteDatabase db, String description, String duedate, String category) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
        return db.insert(Contract.TABLE_TODO.TABLE_NAME, null, cv);
    }

    private boolean removeToDo(SQLiteDatabase db, long id) {
        Log.d(TAG, "deleting id: " + id);
        return db.delete(Contract.TABLE_TODO.TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
    }


    private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description, String category, long id){

        String duedate = formatDate(year, month - 1, day);


        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
        return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);



           }

    @Override
    public void closeUpdateDialog(int year, int month, int day, String description,String category, long id) {
        updateToDo(db, year, month, day, description,category, id);
        adapter.swapCursor(getAllItems(db));
    }


//    public void onCheckboxClicked(View view) {
//
//    }

    //Create Menu for selecting a particular category
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.menuItem);

        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
         spinner.setOnItemSelectedListener(this);
    //spinner.setOnClickListener();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.todo_menu_category, android.R.layout.simple_spinner_item);
       // Log.d(TAG, " Adapter selected------------------ " + adapter);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Set the adapter
        spinner.setAdapter(adapter);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

            String optionSelected=parent.getItemAtPosition(position).toString();


        if ("All".equalsIgnoreCase(optionSelected)) {
            adapter.swapCursor(getAllItems(db));
        }
        else {
            adapter.swapCursor(getOptionSelected(db, optionSelected));
            Log.d(TAG, " Menu item selected------------------ " + optionSelected);

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    //Gets the items from database for the items selected from the menu
    private Cursor getOptionSelected(SQLiteDatabase db, String category) {

        Log.d(TAG, " Category selected------------------ " + category);

        return db.query(Contract.TABLE_TODO.TABLE_NAME,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_CATEGORY + "='" + category + "'",
                null, null, null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE);

    }
}

