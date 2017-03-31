package com.sababado.timelapsehelper;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.sababado.timelapsehelper.models.TimeLapseController;
import com.sababado.timelapsehelper.models.TimeLapseItem;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by robert on 3/31/17.
 */

public class TliCursorAdapter extends CursorAdapter {
    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private LayoutInflater inflater;
    private TliActionListener actionListener;

    public TliCursorAdapter(Context context, Cursor c, TliActionListener actionListener) {
        super(context, c, 0);
        inflater = LayoutInflater.from(context);
        this.actionListener = actionListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(R.layout.time_lapse_list_item, parent, false);
        ViewHolder vh = new ViewHolder();

        vh.framesElapsed = (TextView) view.findViewById(R.id.frames_elapsed);
        vh.timeElapsed = (TextView) view.findViewById(R.id.time_elapsed);
        vh.secondsPerFrameLayout = (ViewGroup) view.findViewById(R.id.seconds_per_frame_layout);
        vh.secondsPerFrame = (TextView) vh.secondsPerFrameLayout.findViewById(R.id.seconds_per_frame);
        vh.startTime = (TextView) view.findViewById(R.id.start_time);
        vh.playPauseSwitcher = (ViewSwitcher) view.findViewById(R.id.play_pause_switcher);
        vh.stop = (ImageView) view.findViewById(R.id.stop);

        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder vh = (ViewHolder) view.getTag();
        final TimeLapseItem tli = new TimeLapseItem(cursor);

        int framesElapsed = TimeLapseController.getFramesElapsed(tli);
        vh.framesElapsed.setText(String.valueOf(framesElapsed));

        String timeElapsed = TimeLapseController.getElapsedTime(tli);
        vh.timeElapsed.setText(timeElapsed);

        vh.secondsPerFrame.setText(String.valueOf(tli.getSecondsPerFrame()));

        if (tli.getStartTime() != 0L) {
            Date time = new Date(tli.getStartTime());
            vh.startTime.setText(context.getString(R.string.start_time, START_TIME_FORMAT.format(time)));
        } else {
            vh.startTime.setText(context.getString(R.string.start_time, "--"));
        }

        vh.secondsPerFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, tli.getId() + "Frames Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        vh.playPauseSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentId = vh.playPauseSwitcher.getCurrentView().getId();
                if (currentId == R.id.play) {
                    // Just tapped the play button
                    // Show the pause button, show the stop button
                    actionListener.timeLapsePlay(tli);
                    vh.stop.setVisibility(View.VISIBLE);
                    vh.playPauseSwitcher.showNext();
                } else if (currentId == R.id.pause) {
                    // just tapped the pause button
                    // Show the play button, hide the stop button
                    actionListener.timeLapsePause(tli);
                    vh.stop.setVisibility(View.GONE);
                    vh.playPauseSwitcher.showPrevious();
                }

            }
        });

        vh.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.timeLapseStop(tli);
                vh.stop.setVisibility(View.GONE);
                vh.playPauseSwitcher.showPrevious();
            }
        });
    }

    private class ViewHolder {
        TextView framesElapsed;
        TextView timeElapsed;
        ViewGroup secondsPerFrameLayout;
        TextView secondsPerFrame;
        TextView startTime;
        ViewSwitcher playPauseSwitcher;
        ImageView stop;
    }
}

