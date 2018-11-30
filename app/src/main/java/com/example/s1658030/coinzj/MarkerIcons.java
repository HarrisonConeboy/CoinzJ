package com.example.s1658030.coinzj;

//The key which refers String fields to their associated marker, I created custom markers for each
// of the floored coin values, I have associated each of the floored values + the currency to one
// of these markers, this is the class which shows that relation

import java.util.HashMap;

public class MarkerIcons {

    public HashMap<String,Integer> masterKey = new HashMap<String,Integer>();

    MarkerIcons() {
        masterKey.put("SHIL0",R.drawable.shil_zero);
        masterKey.put("SHIL1",R.drawable.shil_one);
        masterKey.put("SHIL2",R.drawable.shil_two);
        masterKey.put("SHIL3",R.drawable.shil_three);
        masterKey.put("SHIL4",R.drawable.shil_four);
        masterKey.put("SHIL5",R.drawable.shil_five);
        masterKey.put("SHIL6",R.drawable.shil_six);
        masterKey.put("SHIL7",R.drawable.shil_seven);
        masterKey.put("SHIL8",R.drawable.shil_eight);
        masterKey.put("SHIL9",R.drawable.shil_nine);

        masterKey.put("DOLR0",R.drawable.dolr_zero);
        masterKey.put("DOLR1",R.drawable.dolr_one);
        masterKey.put("DOLR2",R.drawable.dolr_two);
        masterKey.put("DOLR3",R.drawable.dolr_three);
        masterKey.put("DOLR4",R.drawable.dolr_four);
        masterKey.put("DOLR5",R.drawable.dolr_five);
        masterKey.put("DOLR6",R.drawable.dolr_six);
        masterKey.put("DOLR7",R.drawable.dolr_seven);
        masterKey.put("DOLR8",R.drawable.dolr_eight);
        masterKey.put("DOLR9",R.drawable.dolr_nine);

        masterKey.put("PENY0",R.drawable.peny_zero);
        masterKey.put("PENY1",R.drawable.peny_one);
        masterKey.put("PENY2",R.drawable.peny_two);
        masterKey.put("PENY3",R.drawable.peny_three);
        masterKey.put("PENY4",R.drawable.peny_four);
        masterKey.put("PENY5",R.drawable.peny_five);
        masterKey.put("PENY6",R.drawable.peny_six);
        masterKey.put("PENY7",R.drawable.peny_seven);
        masterKey.put("PENY8",R.drawable.peny_eight);
        masterKey.put("PENY9",R.drawable.peny_nine);

        masterKey.put("QUID0",R.drawable.quid_zero);
        masterKey.put("QUID1",R.drawable.quid_one);
        masterKey.put("QUID2",R.drawable.quid_two);
        masterKey.put("QUID3",R.drawable.quid_three);
        masterKey.put("QUID4",R.drawable.quid_four);
        masterKey.put("QUID5",R.drawable.quid_five);
        masterKey.put("QUID6",R.drawable.quid_six);
        masterKey.put("QUID7",R.drawable.quid_seven);
        masterKey.put("QUID8",R.drawable.quid_eight);
        masterKey.put("QUID9",R.drawable.quid_nine);
    }

}
