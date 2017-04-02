package com.sababado.timelapsehelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.sababado.ezprovider.Contracts;
import com.sababado.timelapsehelper.models.TimeLapseController;
import com.sababado.timelapsehelper.models.TimeLapseItem;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TliActionListener {

    private ListView listView;
    private TliCursorAdapter adapter;
    private Handler refreshHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTli();
                Snackbar.make(view, R.string.adding_time_lapse_message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        refreshHandler = new Handler();

        listView = (ListView) findViewById(R.id.list_view);
        adapter = new TliCursorAdapter(this, null, this);
        getSupportLoaderManager().initLoader(0, null, this);
        listView.setAdapter(adapter);
    }

    private void refreshList() {
        refreshHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (adapter != null && adapter.getCount() > 0) {
                    adapter.notifyDataSetChanged();
                    refreshHandler.postDelayed(this, 250);
                }
            }
        }, 250);
    }

    private void addTli() {
        ContentValues values = new TimeLapseItem().toContentValues();
        Contracts.Contract contract = Contracts.getContract(TimeLapseItem.class);
        getContentResolver().insert(contract.CONTENT_URI, values);
    }

    private void updateTli(TimeLapseItem tli) {
        ContentValues values = tli.toContentValues();
        Contracts.Contract contract = Contracts.getContract(TimeLapseItem.class);
        getContentResolver().update(contract.CONTENT_URI, values,
                "_id=?", new String[]{String.valueOf(tli.getId())});
    }

    @Override
    public void timeLapsePlay(TimeLapseItem tli) {
        TimeLapseController.startTimeLapse(tli);
        updateTli(tli);
    }

    @Override
    public void timeLapsePause(TimeLapseItem tli) {
        TimeLapseController.pauseTimeLapse(tli);
        updateTli(tli);
    }

    @Override
    public void timeLapseStop(TimeLapseItem tli) {
        TimeLapseController.stopTimeLapse(tli);
        updateTli(tli);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Contracts.Contract contract = Contracts.getContract(TimeLapseItem.class);
        return new CursorLoader(this, contract.CONTENT_URI, contract.COLUMNS,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (adapter != null) {
            adapter.swapCursor(data);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    refreshList();
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (adapter != null) {
            adapter.swapCursor(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(0);
    }
}
