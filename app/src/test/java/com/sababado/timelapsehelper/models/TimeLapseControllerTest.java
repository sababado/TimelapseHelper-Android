package com.sababado.timelapsehelper.models;

import com.sababado.timelapsehelper.TestingUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TimeLapseControllerTest {

    @Test
    public void testPlayActions() throws Exception {
        TimeLapseItem tli = new TimeLapseItem();
        long expectedPauseLength = 200;
        long expectedPauseTime = 0L;

        // start
        TimeLapseController.startTimeLapse(tli);
        long expectedStartTime = System.currentTimeMillis();
        assertEquals(expectedStartTime, tli.getStartTime());
        assertEquals(expectedPauseTime, tli.getPauseTime());
        assertEquals(0L, tli.calculatePauseLength());

        TestingUtils.sleep(expectedPauseLength);


        // pause
        TimeLapseController.pauseTimeLapse(tli);
        expectedPauseTime = System.currentTimeMillis();
        assertEquals(expectedStartTime, tli.getStartTime());
        assertEquals(expectedPauseTime, tli.getPauseTime());
        assertEquals(0L, tli.calculatePauseLength());


        // start from pause
        TestingUtils.sleep(expectedPauseLength);
        TimeLapseController.startTimeLapse(tli);
        expectedPauseTime = 0L;
        assertEquals(expectedStartTime, tli.getStartTime());
        assertEquals(expectedPauseTime, tli.getPauseTime());
        assertEquals((double) expectedPauseLength, (double) tli.calculatePauseLength(), 10.0);

        TestingUtils.sleep(expectedPauseLength);


        // stop
        TimeLapseController.stopTimeLapse(tli);
        assertEquals(expectedStartTime, tli.getStartTime());
        assertEquals(expectedPauseTime, tli.getPauseTime());
        assertEquals((double) expectedPauseLength, (double) tli.calculatePauseLength(), 10.0);


        // re-start
        TimeLapseController.startTimeLapse(tli);
        expectedStartTime = System.currentTimeMillis();
        expectedPauseLength = 0L;
        assertEquals(expectedStartTime, tli.getStartTime());
        assertEquals(expectedPauseTime, tli.getPauseTime());
        assertEquals((double) expectedPauseLength, (double) tli.calculatePauseLength(), 10.0);
    }

    @Test
    public void testMultiplePauses() {
        TimeLapseItem tli = new TimeLapseItem();
        long expectedPauseLength = 200;

        TimeLapseController.startTimeLapse(tli);
        TimeLapseController.pauseTimeLapse(tli);
        TestingUtils.sleep(expectedPauseLength);
        TimeLapseController.startTimeLapse(tli);
        assertEquals((double) expectedPauseLength, (double) tli.calculatePauseLength(), 10.0);

        TimeLapseController.pauseTimeLapse(tli);
        TestingUtils.sleep(expectedPauseLength);
        TimeLapseController.startTimeLapse(tli);
        assertEquals((double) expectedPauseLength * 2.0, (double) tli.calculatePauseLength(), 10.0);

        TestingUtils.sleep(expectedPauseLength);
        TimeLapseController.pauseTimeLapse(tli);
        TestingUtils.sleep(expectedPauseLength);
        TimeLapseController.startTimeLapse(tli);
        assertEquals((double) expectedPauseLength * 3.0, (double) tli.calculatePauseLength(), 15.0);
        TimeLapseController.stopTimeLapse(tli);
    }

    @Test
    public void testPlayErrors() {
        TimeLapseItem tli = new TimeLapseItem();
        try {
            TimeLapseController.pauseTimeLapse(tli);
        } catch (UnsupportedOperationException e) {
            return;
        }
        fail();
    }

    @Test
    public void testGetRunningTime() {
        TimeLapseItem tli = new TimeLapseItem();
        long expectedRunningTime = 200;

        TimeLapseController.startTimeLapse(tli);
        TestingUtils.sleep(expectedRunningTime);
        TimeLapseController.pauseTimeLapse(tli);
        assertEquals((double)expectedRunningTime, TimeLapseController.getRunningTime(tli), 10.0);

        TestingUtils.sleep(expectedRunningTime);
        TimeLapseController.startTimeLapse(tli);
        TestingUtils.sleep(expectedRunningTime);
        TimeLapseController.pauseTimeLapse(tli);
        assertEquals((double)expectedRunningTime * 2.0, TimeLapseController.getRunningTime(tli), 10.0);

        TimeLapseController.stopTimeLapse(tli);
        TestingUtils.sleep(expectedRunningTime);
        assertEquals((double)expectedRunningTime * 2.0, TimeLapseController.getRunningTime(tli), 15.0);
    }

    @Test
    public void testGetFramesElapsed() {
        TimeLapseItem tli = new TimeLapseItem();
        float secondsPerFrame = 0.2f;
        tli.setSecondsPerFrame(secondsPerFrame);
        long sleepTime = 100;

        TimeLapseController.startTimeLapse(tli);
        TestingUtils.sleep(sleepTime);
        assertEquals(0, TimeLapseController.getFramesElapsed(tli));

        TestingUtils.sleep(sleepTime);
        assertEquals(1, TimeLapseController.getFramesElapsed(tli));
        TestingUtils.sleep(sleepTime);
        assertEquals(1, TimeLapseController.getFramesElapsed(tli));
        TestingUtils.sleep(sleepTime);
        assertEquals(2, TimeLapseController.getFramesElapsed(tli));

        TimeLapseController.pauseTimeLapse(tli);
        TestingUtils.sleep(sleepTime * 5);
        assertEquals(2, TimeLapseController.getFramesElapsed(tli));

        TimeLapseController.startTimeLapse(tli);
        assertEquals(2, TimeLapseController.getFramesElapsed(tli));
        TestingUtils.sleep(sleepTime);
        assertEquals(2, TimeLapseController.getFramesElapsed(tli));

        TimeLapseController.pauseTimeLapse(tli);
        TestingUtils.sleep(sleepTime);
        assertEquals(2, TimeLapseController.getFramesElapsed(tli));

        TimeLapseController.startTimeLapse(tli);
        assertEquals(2, TimeLapseController.getFramesElapsed(tli));
        TestingUtils.sleep(sleepTime);
        assertEquals(3, TimeLapseController.getFramesElapsed(tli));

        TimeLapseController.stopTimeLapse(tli);
        assertEquals(3, TimeLapseController.getFramesElapsed(tli));
        TestingUtils.sleep(sleepTime);
        assertEquals(3, TimeLapseController.getFramesElapsed(tli));
    }
}