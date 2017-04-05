package com.sababado.timelapsehelper;

import android.view.View;

import com.sababado.timelapsehelper.models.TimeLapseItem;

/**
 * Created by robert on 3/31/17.
 */

public interface TliActionListener {
    void timeLapsePlay(TimeLapseItem tli);

    void timeLapsePause(TimeLapseItem tli);

    void timeLapseStop(TimeLapseItem tli);

    void onSpfClick(TimeLapseItem tli);

    void onNameClick(TimeLapseItem tli);

    void onMoreClicked(TimeLapseItem tli, View moreButton);
}
