package com.sababado.timelapsehelper;

import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sababado.timelapsehelper.models.TimeLapseController;
import com.sababado.timelapsehelper.models.TimeLapseItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sababado.timelapsehelper.models.TimeLapseItem.PAUSED;
import static com.sababado.timelapsehelper.models.TimeLapseItem.RUNNING;
import static com.sababado.timelapsehelper.models.TimeLapseItem.STOPPED;

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

        vh.name = (TextView) view.findViewById(R.id.name);
        vh.framesElapsed = (TextView) view.findViewById(R.id.frames_elapsed);
        vh.timeElapsed = (TextView) view.findViewById(R.id.time_elapsed);
        vh.secondsPerFrameLayout = (ViewGroup) view.findViewById(R.id.seconds_per_frame_layout);
        vh.secondsPerFrame = (TextView) vh.secondsPerFrameLayout.findViewById(R.id.seconds_per_frame);
        vh.startTime = (TextView) view.findViewById(R.id.start_time);
        vh.play = (ImageView) view.findViewById(R.id.play);
        vh.pause = (ImageView) view.findViewById(R.id.pause);
        vh.stop = (ImageView) view.findViewById(R.id.stop);
        vh.menu = (ImageView) view.findViewById(R.id.more);

        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder vh = (ViewHolder) view.getTag();
        final TimeLapseItem tli = new TimeLapseItem(cursor);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                switch (id) {
                    case R.id.name:
                        actionListener.onNameClick(tli);
                        break;
                    case R.id.seconds_per_frame:
                        actionListener.onSpfClick(tli);
                        break;
                    case R.id.play:
                        actionListener.timeLapsePlay(tli);
                        break;
                    case R.id.pause:
                        actionListener.timeLapsePause(tli);
                        break;
                    case R.id.stop:
                        actionListener.timeLapseStop(tli);
                        break;
                    case R.id.more:
                        actionListener.onMoreClicked(tli, vh.menu);
                        break;
                }
            }
        };

        if (TextUtils.isEmpty(tli.getName())) {
            int pos = cursor.getPosition() + 1;
            tli.setName(context.getString(R.string.camera_num, pos));
        }
        vh.name.setText(tli.getName());
        vh.name.setOnClickListener(onClickListener);

        int framesElapsed = TimeLapseController.getFramesElapsed(tli);
        vh.framesElapsed.setText(String.valueOf(framesElapsed));

        String timeElapsed = TimeLapseController.getElapsedTime(tli);
        vh.timeElapsed.setText(timeElapsed);

        vh.secondsPerFrame.setText(String.valueOf(tli.getSecondsPerFrame()));
        vh.secondsPerFrame.setOnClickListener(onClickListener);
        vh.secondsPerFrame.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString();
                if (!TextUtils.isEmpty(str)) {
                    float num = Float.parseFloat(str);
                    String newString = DecimalFormat.getNumberInstance().format(num);
                    if (!str.equals(newString)) {
                        editable.clear();
                        editable.append(newString);
                    }
                }
            }
        });

        if (tli.getStartTime() != 0L) {
            Date time = new Date(tli.getStartTime());
            vh.startTime.setText(context.getString(R.string.start_time, START_TIME_FORMAT.format(time)));
        } else {
            vh.startTime.setText(context.getString(R.string.start_time, "--"));
        }

        switch (tli.getRunState()) {
            case RUNNING:
                vh.stop.setVisibility(View.VISIBLE);
                vh.pause.setVisibility(View.VISIBLE);
                vh.play.setVisibility(View.GONE);
                break;
            case PAUSED:
                vh.stop.setVisibility(View.VISIBLE);
                vh.pause.setVisibility(View.GONE);
                vh.play.setVisibility(View.VISIBLE);
                break;
            case STOPPED:
                vh.stop.setVisibility(View.GONE);
                vh.pause.setVisibility(View.GONE);
                vh.play.setVisibility(View.VISIBLE);
                break;
        }

        vh.secondsPerFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, tli.getId() + "Frames Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        vh.play.setOnClickListener(onClickListener);
        vh.pause.setOnClickListener(onClickListener);
        vh.stop.setOnClickListener(onClickListener);
        vh.menu.setOnClickListener(onClickListener);
    }

    private class ViewHolder {
        TextView name;
        TextView framesElapsed;
        TextView timeElapsed;
        ViewGroup secondsPerFrameLayout;
        TextView secondsPerFrame;
        TextView startTime;
        ImageView play;
        ImageView pause;
        ImageView stop;
        ImageView menu;
    }
}

