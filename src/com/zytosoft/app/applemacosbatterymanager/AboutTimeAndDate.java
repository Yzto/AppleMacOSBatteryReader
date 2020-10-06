package com.zytosoft.app.applemacosbatterymanager;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class AboutTimeAndDate
{
    public static void main(String[] args)
    {
        AboutTimeAndDate aboutTimeAndDate = new AboutTimeAndDate();

        aboutTimeAndDate.doTimeAndShifting();
    }

    private void doTimeAndShifting()
    {
        final long start = System.currentTimeMillis();

        // Zeroth - Initialization
        LocalTime baseLocalTime = LocalTime.now();
        System.out.println("Base - " + baseLocalTime);

        // 1st way - simply
        LocalTime shiftedLocalTime1 = baseLocalTime.plusMinutes(10L);
        System.out.println("simply - " + shiftedLocalTime1);

        // 2nd way - Duration
        LocalTime shiftedLocalTime2 = baseLocalTime.plus(Duration.ofMinutes(10L));
        System.out.println("Duration - " + shiftedLocalTime2);

        // 3rd way - ChronoUnit
        LocalTime shiftedLocalTime3 = baseLocalTime.plus(10L, ChronoUnit.MINUTES);
        System.out.println("ChronoUnit - " + shiftedLocalTime3);

        final long end = System.currentTimeMillis();

        Duration duration = Duration.ofMillis(end - start);
        System.out.println("TemporalAmount - " + duration.get(ChronoUnit.SECONDS));
    }
}
