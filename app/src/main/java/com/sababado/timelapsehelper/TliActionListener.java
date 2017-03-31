package com.sababado.timelapsehelper;

import com.sababado.timelapsehelper.models.TimeLapseItem;

/**
 * Created by robert on 3/31/17.
 */

public interface TliActionListener {
    void timeLapsePlay(TimeLapseItem tli);

    void timeLapsePause(TimeLapseItem tli);

    void timeLapseStop(TimeLapseItem tli);
}
