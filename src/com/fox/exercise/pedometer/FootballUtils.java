package com.fox.exercise.pedometer;

import cn.ingenic.indroidsync.SportsApp;

public class FootballUtils {
    public static double getCalories(double dis) {
        int weight = SportsApp.getInstance().getSportUser().getWeight();
        if (weight == 0) {
            weight = 65;
        }
        int speed = 10;//km/h
        double hour = dis / speed;
        return weight * hour * 9.375;
    }
}
