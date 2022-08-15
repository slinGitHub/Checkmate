package com.example.a20210207_checkmate2;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class Utils {

    public static int percentToMol(Double percent) {
        return (int) round((10.93f * percent - 23.5f), 0);
    }

    public static Double molToPercent(int mol) {
        return round((mol + 23.5) / 10.93f, 1);
    }

    public static int molToDl(Double mol) {
        return (int) round(mol * 18, 0);
    }

    public static Double dlToMol(int dl) {
        return round(dl / 18.0, 1);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double getHba1c_mmol(double hba1c, Boolean switchToMol) {
        if (switchToMol) {
            return percentToMol(hba1c);
        }
        return hba1c;
    }

}
