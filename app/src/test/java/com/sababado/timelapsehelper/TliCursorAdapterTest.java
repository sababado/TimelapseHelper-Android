package com.sababado.timelapsehelper;

import com.sababado.timelapsehelper.models.TimeLapseController;
import com.sababado.timelapsehelper.models.TimeLapseItem;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by robert on 3/31/17.
 */

public class TliCursorAdapterTest {
    @Test
    public void testTimeFormat() {
        TimeLapseItem tli = new TimeLapseItem();
        TimeLapseController.startTimeLapse(tli);
        TestingUtils.sleep(1000);
        TimeLapseController.pauseTimeLapse(tli);

        String expected = "00:00:00:01";
        String actual = TimeLapseController.getElapsedTime(tli);
        assertEquals(expected, actual);
    }
}
