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
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sababado.ezprovider.Contracts;
import com.sababado.timelapsehelper.models.TimeLapseController;
import com.sababado.timelapsehelper.models.TimeLapseItem;

import java.text.DecimalFormat;
import java.text.NumberFormat;

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
        listView.setOnItemClickListener(onItemClickListener);
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

    private void deleteTli(TimeLapseItem tli) {
        Contracts.Contract contract = Contracts.getContract(TimeLapseItem.class);
        getContentResolver().delete(contract.CONTENT_URI, "_id=?", new String[]{String.valueOf(tli.getId())});
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Cursor cursor = adapter.getCursor();
            int savedPos = cursor.getPosition();
            cursor.moveToPosition(i);
            TimeLapseItem tli = new TimeLapseItem(cursor);
            cursor.moveToPosition(savedPos);

            showStats(tli);
        }
    };

    private void showStats(TimeLapseItem tli) {
        float framerate = 30.0f;
        int elapsedFrames = TimeLapseController.getFramesElapsed(tli);
        float seconds = elapsedFrames / framerate;
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        String framerateStr = decimalFormat.format(framerate);
        String secondsStr = decimalFormat.format(seconds);
        String msg = getString(R.string.timelapse_will_render, secondsStr, framerateStr, elapsedFrames);
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(msg)
                .setTitle(tli.getName())
                .show();
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

            ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.spf_selection, null, false);
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(viewGroup)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            ListView spfList = (ListView) viewGroup.findViewById(R.id.list_view);
            spfList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    alertDialog.dismiss();
                    String text = ((TextView) view).getText().toString();
                    String number = text.substring(0, text.indexOf("-") - 1).trim();
                    float spf = Float.parseFloat(number);
                    updateSpf(tli, spf);
                }
            });
            final EditText editText = (EditText) viewGroup.findViewById(R.id.editText);

            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setHint(String.valueOf(tli.getSecondsPerFrame()));

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String text = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(text)) {
                        text = editText.getHint().toString();
                    }
                    float spf = Float.parseFloat(text);
                    updateSpf(tli, spf);
                }
            });
            alertDialog.show();
        } else {
            Toast.makeText(this, R.string.cannot_change_spf_while_running, Toast.LENGTH_LONG).show();
        }
    }

    private void updateSpf(TimeLapseItem tli, float spf) {
        float old = tli.getSecondsPerFrame();
        tli.setSecondsPerFrame(spf);
        if (old != spf) {
            tli.reset();
        }
        updateTli(tli);
        refreshList();
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
    public void onMoreClicked(final TimeLapseItem tli, View moreButton) {
        PopupMenu popupMenu = new PopupMenu(this, moreButton);
        popupMenu.inflate(R.menu.tli_popup);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_stats:
                        showStats(tli);
                        return true;
                    case R.id.action_delete:
                        deleteTli(tli);
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
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
