package com.pahaltutorials.pahaltutorials.util;

import android.content.Context;

import com.pahaltutorials.pahaltutorials.R;

public class DataUtil {

    public static String[] getSubjectsContent(Context context, String subject){
        String content[] = getIpContentData(context);
        if (isSubjectMaths(context, subject)){
            content = getMathsContentData(context);
        }else if (isSubjectIp(context, subject)){
            content = getIpContentData(context);
        }else if (isSubjectCs(context, subject)) {
            content = getCsContentData(context);
        }
        return content;
    }

    private static boolean isSubjectMaths(Context context, String subject){
        return subject.equals(context.getResources().getString(R.string.maths));
    }
    private static boolean isSubjectIp(Context context, String subject){
        return subject.equals(context.getResources().getString(R.string.information_practices));
    }
    private static boolean isSubjectCs(Context context, String subject){
        return subject.equals(context.getResources().getString(R.string.computer_science));
    }
    public static String[] getMathsContentData(Context context){
        return context.getResources().getStringArray(R.array.maths_content_list);
    }

    public static String[] getIpContentData(Context context){
        return context.getResources().getStringArray(R.array.ip_content_list);
    }

    public static String[] getCsContentData(Context context){
        return context.getResources().getStringArray(R.array.cs_content_list);
    }

    private static String[] getMathsPreviousYears(Context context){
        return context.getResources().getStringArray(R.array.cbse_12th_maths_previous_years);
    }

    private static String[] getSampleYears(Context context){
        return context.getResources().getStringArray(R.array.cbse_12th_sample_years);
    }

    private static String[] getMathsChapters(Context context){
        return context.getResources().getStringArray(R.array.cbse_12th_maths_chapters);
    }

    private static String[] getIpPreviousYears(Context context){
        return context.getResources().getStringArray(R.array.cbse_12th_IP_previous_years);
    }

    private static String[] getIpChapters(Context context){
        return context.getResources().getStringArray(R.array.cbse_12th_ip_chapters);
    }

    private static String[] getCsPreviousYears(Context context){
        return context.getResources().getStringArray(R.array.cbse_12th_CS_previous_years);
    }

    private static String[] getCsChapters(Context context){
        return context.getResources().getStringArray(R.array.cbse_12th_cs_chapters);
    }

    public static String[] getCurrentListToView(Context context, String currentLocation) {
        int currentLevel = currentLocation.split("/").length;
        String[] words = currentLocation.split("/");
        switch (currentLevel){
            case 1: return getSubjectsContent(context, words[0]);
            case 2: if (isSubjectMaths(context, words[0])){
                        if (words[1].equals("CBSE Previous Year Papers")){
                            return getMathsPreviousYears(context);
                        }else if (words[1].equals("CBSE Sample Papers")){
                            return getSampleYears(context);
                        }else{
                            return getMathsChapters(context);
                        }
                    }else if (isSubjectIp(context, words[0])){
                        if (words[1].equals("CBSE Previous Year Papers")){
                            return getIpPreviousYears(context);
                        }else if (words[1].equals("CBSE Sample Papers")){
                            return getSampleYears(context);
                        }else{
                            return getIpChapters(context);
                        }
                    } else if (isSubjectCs(context, words[0])){
                        if (words[1].equals("CBSE Previous Year Papers")){
                            return getCsPreviousYears(context);
                        }else if (words[1].equals("CBSE Sample Papers")){
                            return getSampleYears(context);
                        }else{
                            return getCsChapters(context);
                        }
                    }
            case 3:

            case 4:

            default:return null;

        }

    }
}
