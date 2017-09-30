package com.my.dn.util;

import java.util.Random;

public class Utils {
    private static final String TAG = "Utils";

    private static Random random = new Random();

    public static int getRandom(int max) {
        return random.nextInt(max);
    }
}
