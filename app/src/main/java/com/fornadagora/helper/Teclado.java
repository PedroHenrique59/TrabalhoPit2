package com.fornadagora.helper;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Teclado {
    public static void fecharTeclado(View view){
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
