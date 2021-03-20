package com.fornadagora.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidaEmail {

    public static boolean validarEmail(String email) {
        boolean ehValido = false;
        if (email != null && email.length() > 0) {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (matcher.matches()) {
                ehValido = true;
            }
        }
        return ehValido;
    }
}
