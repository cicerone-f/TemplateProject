package com.federico.cicerone.contentproviderexample.Activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.federico.cicerone.contentproviderexample.adapter.StoreAdapter;
import com.federico.cicerone.contentproviderexample.Contract.BookContract;
import com.federico.cicerone.contentproviderexample.R;
import com.federico.cicerone.contentproviderexample.model.Book;
import com.federico.cicerone.contentproviderexample.model.Store;
import com.federico.cicerone.contentproviderexample.sqlite.ContentProvider;
import com.federico.cicerone.contentproviderexample.Contract.StoreContract;


public class Stores extends ActionBarActivity {

    private ListView storeList;
    private StoreAdapter storeAdapter;
    private ContentResolver cr;
    private String logtag = "StoresActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);
        cr = getContentResolver();
        insertData();
        storeList = (ListView) findViewById( R.id.store_list );
        Cursor cursor = ContentProvider.getAllStores( cr );
        storeAdapter = new StoreAdapter( this, cursor, 0);
        storeList.setAdapter( storeAdapter );
        StoreClickListener storeClickListener = new StoreClickListener(this,cursor);
        storeList.setOnItemClickListener(storeClickListener);
    }

    private void insertData() {
        Store storeA = new Store( "store A", 12, 40 );
        Store storeB = new Store( "store B", 13, 41 );

        // delete all previously saved stores and books
        cr.delete( ContentProvider.CONTENT_URI_STORE, StoreContract.StoreEntry.COLUMN_NAME_ID + " >= 0", null);
        cr.delete( ContentProvider.CONTENT_URI_BOOK, BookContract.BookEntry.COLUMN_NAME_ID + " >= 0", null);

        cr.insert( ContentProvider.CONTENT_URI_STORE, storeA.getContentValue() );
        cr.insert( ContentProvider.CONTENT_URI_STORE, storeB.getContentValue() );

        Cursor store_crs =  ContentProvider.getAllStores( cr );
        while ( store_crs.moveToNext()) {
            Book book = new Book("The Prince", "Niccoló Machiavelli", store_crs.getInt( store_crs.getColumnIndex( StoreContract.StoreEntry.COLUMN_NAME_ID ) ) );
            cr.insert( ContentProvider.CONTENT_URI_BOOK, book.getContentValue() );
        }
    }

    private String getInfo() {
        String info = "";
        Cursor store_crs =  ContentProvider.getAllStores( cr );
        while ( store_crs.moveToNext()) {
            info += " Store: " + store_crs.getString( store_crs.getColumnIndex( StoreContract.StoreEntry.COLUMN_NAME_NAME ) );
            Cursor book_crs =  ContentProvider.getAllBooksInStore(cr, store_crs.getString(store_crs.getColumnIndex(StoreContract.StoreEntry.COLUMN_NAME_ID)));
            while ( book_crs.moveToNext() ){
                info += " Title: " + book_crs.getString( book_crs.getColumnIndex(BookContract.BookEntry.COLUMN_NAME_TITLE) );
                info += " Author: " + book_crs.getString( book_crs.getColumnIndex(BookContract.BookEntry.COLUMN_NAME_AUTHOR) );
            }
        }
        return info;
    }


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

    private class StoreClickListener implements AdapterView.OnItemClickListener{
        Context context;
        Cursor store_crs;

        private StoreClickListener(Context ctx, Cursor c) {
            context = ctx;
            store_crs = c;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(context, Books.class);
            intent.putExtra( EXTRA.STORE_ID, store_crs.getString( store_crs.getColumnIndex(StoreContract.StoreEntry.COLUMN_NAME_ID)) );
            startActivity(intent);
        }
    }

    // Expose extras constant
    public static class EXTRA {
        public static String STORE_ID = "store_id";
    }
}
