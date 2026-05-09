package com.auction.util;

import java.text.NumberFormat;
import java.util.Locale;

public class PriceFormatter {

    public static String formatVND(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VND";
    }
}