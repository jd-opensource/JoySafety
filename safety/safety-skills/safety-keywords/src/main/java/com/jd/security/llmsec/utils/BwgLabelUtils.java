package com.jd.security.llmsec.utils;

public class BwgLabelUtils {
    public static Integer getBwgLabelFromHandleStrategy(String handleStrategy) {

        Integer bwgLabel = 0;
        switch (handleStrategy) {
            case "black":
                bwgLabel = 1;
                break;
            case "white":
                bwgLabel = 2;
                break;
            case "grey":
                bwgLabel = 3;
                break;
            default:
                bwgLabel = 0;
                break;
        }
        return bwgLabel;
    }
}
