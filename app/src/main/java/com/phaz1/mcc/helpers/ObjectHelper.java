package com.phaz1.mcc.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by altaf on 26/05/2017.
 */

public class ObjectHelper {
    public static List<DurationBefore> getDurationBefore()
    {
        List<DurationBefore> durations = new ArrayList<>();
        durations.add(new DurationBefore("5 minutes",5));
        durations.add(new DurationBefore("10 minutes",10));
        durations.add(new DurationBefore("15 minutes",15));
        durations.add(new DurationBefore("20 minutes",20));
        durations.add(new DurationBefore("30 minutes",30));
        durations.add(new DurationBefore("45 minutes",45));
        durations.add(new DurationBefore("1 Hour",60));
        durations.add(new DurationBefore("1 Hour 30 minutes",90));
        durations.add(new DurationBefore("2 Hours",120));
        durations.add(new DurationBefore("2 Hours 30 minutes",150));
        durations.add(new DurationBefore("3 Hours",180));
        return durations;
    }
}