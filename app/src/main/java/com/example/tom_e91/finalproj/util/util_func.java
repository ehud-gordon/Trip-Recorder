package com.example.tom_e91.finalproj.util;

import android.widget.EditText;

public class util_func {
    public static boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }
}
