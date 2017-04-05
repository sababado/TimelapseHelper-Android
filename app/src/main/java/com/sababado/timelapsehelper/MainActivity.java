package com.sababado.timelapsehelper;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.sababado.ezprovider.Contracts;
import com.sababado.timelapsehelper.models.TimeLapseController;
import com.sababado.timelapsehelper.models.TimeLapseItem;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TliActionListener {

    private ListView listView;
    private TliCursorAdapter adapter;
    private Handler refreshHandler;
    private boolean isRefreshing;
    private boolean shouldStopRefreshing;
    private boolean reloadFromAdd;

    public MainActivity() {
        isRefreshing = false;
        shouldStopRefreshing = false;
        reloadFromAdd = false;
    }

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
                reloadFromAdd = true;
                addTli();
            }
        });

        refreshHandler = new Handler();

        listView = (ListView) findViewById(R.id.list_view);
        adapter = new TliCursorAdapter(this, null, this);
        getSupportLoaderManager().initLoader(0, null, this);
        listView.setAdapter(adapter);
    }

    private void refreshList() {
        if (!isRefreshing) {
            refreshHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (!shouldStopRefreshing && adapter != null && adapter.getCount() > 0) {
                        isRefreshing = true;
                        adapter.notifyDataSetChanged();
                        refreshHandler.postDelayed(this, 250);
                    } else {
                        isRefreshing = false;
                    }
                }
            }, 250);
        }
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
    public void onSpfClick(final TimeLapseItem tli) {
        if (tli.getRunState() == TimeLapseItem.STOPPED) {
            final EditText editText = new EditText(this);
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setHint(String.valueOf(tli.getSecondsPerFrame()));
            new AlertDialog.Builder(this)
                    .setView(editText)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            float spf = Float.parseFloat(editText.getText().toString().trim());
                            tli.setSecondsPerFrame(spf);
                            updateTli(tli);
                            refreshList();
                        }
                    })
                    .create()
                    .show();
        } else {
            Toast.makeText(this, R.string.cannot_change_spf_while_running, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNameClick(final TimeLapseItem tli) {
        final EditText editText = new EditText(this);
        editText.setHint(tli.getName());
        new AlertDialog.Builder(this)
                .setView(editText)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            name = editText.getHint().toString();
                        }
                        tli.setName(name);
                        updateTli(tli);
                        refreshList();
                    }
                })
                .create()
                .show();
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
                    if (reloadFromAdd) {
                        reloadFromAdd = false;
                        Snackbar.make(listView, R.string.adding_time_lapse_message, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
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
    protected void onStart() {
        super.onStart();
        shouldStopRefreshing = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        shouldStopRefreshing = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(0);
    }
}
