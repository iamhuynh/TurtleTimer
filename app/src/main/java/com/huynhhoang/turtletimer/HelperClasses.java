package com.huynhhoang.turtletimer;

/**
 * Created by huynh on 5/29/17.
 */

public class HelperClasses {
    /**
     * Helper class to strip digits from a string
     * Used for user input
     */
    public static String stripNonDigits(final CharSequence input){
        final StringBuilder sb = new StringBuilder(input.length());
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        return sb.toString();
    }




}
