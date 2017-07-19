package com.sargent.mark.todolist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sargent.mark.todolist.data.Contract;

/**
 * Created by mark on 7/4/17.
 */



public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ItemHolder> {

    private Cursor cursor;
    private ItemClickListener listener;
    private String TAG = "todolistadapter";
   // private CheckBox checkBox;
  //  boolean done;



    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public interface ItemClickListener {
        void onItemClick(int pos, String description, String duedate, String category, long id);
    }

    public ToDoListAdapter(Cursor cursor, ItemClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
       // this.checkBox=checkBox;
    }

    public void swapCursor(Cursor newCursor){
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }



    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView descr;
        TextView due;
        TextView cat;
        String duedate;
        String description;
        String category;
        long id;

        boolean done;

      //  CheckBox status;
        Button status;
     //   status.setText("Done");

        ItemHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);
            cat = (TextView) view.findViewById(R.id.categorylist);
          //  status=(CheckBox)view.findViewById(R.id.done);
            status=(Button)view.findViewById(R.id.done);
            view.setOnClickListener(this);
        }

        public void bind(ItemHolder holder, int pos) {
            cursor.moveToPosition(pos);
            id = cursor.getLong(cursor.getColumnIndex(Contract.TABLE_TODO._ID));
            Log.d(TAG, "deleting id: " + id);

            duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
            description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));
            //Get the values for category and boolean done using the cursor.getString
            category = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY));
            done=cursor.getInt(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DONE))==1;
            descr.setText(description);
            due.setText(duedate);

            //Set the values of categories in a textview
            cat.setText(category);
            holder.itemView.setTag(id);

            updatestatus();

                //Button clicklistener when the button is clicked,sets the text for the button  and calls the updatestatus and updatetodostatus
            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos=getAdapterPosition();

                    done=!done;
                    Log.d(TAG,"Value is-------"+done);
                    if (done==true)
                    {
                        status.setText("UnDone");
                    }
                    else {
                        status.setText("Done");

                    }

//                    if(status.isChecked())
//                    {
//                        done=true;
//                    }
//                    else {
//                        done=false;
//                    }

                    updatestatus();
                    MainActivity.updatetodostatus(pos,id,done);
                }
            });

//
        }

            //Method to strike through when a checkbox is checked
            private void updatestatus()
            {
                if (done) {
                    descr.setPaintFlags(
                            descr.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    due.setPaintFlags(
                            due.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    cat.setPaintFlags(
                            cat.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    status.setText("Undone");
                } else {
                    descr.setPaintFlags(
                            descr.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    due.setPaintFlags(
                            due.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    cat.setPaintFlags(
                            cat.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

                    status.setText("Done");
                }

            }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            listener.onItemClick(pos, description, duedate, category, id);
        }




    }

}
