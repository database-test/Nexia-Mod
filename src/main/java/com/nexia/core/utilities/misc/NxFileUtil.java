package com.nexia.core.utilities.misc;

import com.nexia.core.Main;

import java.io.File;

public class NxFileUtil {

    public static String makeDir(String string) {
        new File(string).mkdirs();
        return string;
    }

    public static String addConfigDir(String string) {
        string = Main.modConfigDir + "/" + string;
        new File(string).mkdirs();
        return string;
    }

}