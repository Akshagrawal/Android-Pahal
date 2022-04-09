package com.pahaltutorials.pahaltutorials.util;

import android.annotation.SuppressLint;
import android.content.Context;

import com.pahaltutorials.pahaltutorials.preferences.ClassPreferences;

import java.util.regex.Pattern;

public class Util {

    public static String getNameToShow(String str) {
        if (str == null) return null;
        String[] words = getWordsInString(str);

        StringBuilder text = new StringBuilder();
        try{
            for (String word : words)
                if (!word.equals("") && Pattern.matches("[a-zA-Z]*", word))
                    text.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
                else
                    text.append(word).append(" ");
        }catch(Exception e){
            e.printStackTrace();
        }
        return text.substring(0, text.length() - 1);
    }

    public static String getLink(Context context, String currentLocation){
        String currentClass = ClassPreferences.getCurrentClass(context);
        return String.format("%s/%s", currentClass, currentLocation);
    }

    public static String[] getWordsInString(String str){
        return str.split("[ /]");
    }

    public static String getlastLinkContent(String str){
        String[] words = str.split("[/]");
        return words[words.length-1];
    }

    public static String getLinkToAdd(Context context, String str){
        str = getLink(context, str);
        String[] words = str.split("[/]");
        str = "";
        for (int i = 0;i<words.length-1;i++){
            str += words[i] + "/";
        }
        return str;
    }

    @SuppressLint("DefaultLocale")
    public static String getPageNumber(int position) {
        if (position > 999 && position < 10000)
            return String.format("%d", position);
        else if (position>99 && position < 1000)
            return String.format("0%d", position);
        else if (position > 9 && position < 100)
            return String.format("00%d", position);
        else
            return String.format("000%d", position);
    }
}
