package com.project.musicianhub.util;

import android.util.Patterns;
import android.widget.EditText;
import android.widget.Spinner;

import com.project.musicianhub.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Form validation utility for the musician hub different components
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class ValidationUtil {


    /**
     * Validates the empty registration form
     *
     * @param name            user name edit text
     * @param username        username edit text
     * @param email           email edit text
     * @param gender          gender spinner
     * @param password        password edit texrt
     * @param confirmPassword confirm password edit text
     * @param phonenumber     phone number edit text
     * @return true or false whether validation is successful
     */
    public static boolean validateEmptyRegistrationForm(EditText name, EditText username, EditText email, Spinner gender, EditText password, EditText confirmPassword, EditText phonenumber) {
        final User user = new User(name.getText().toString(), username.getText().toString(), email.getText().toString(), gender.getSelectedItem().toString(), password.getText().toString(), confirmPassword.getText().toString(), phonenumber.getText().toString());

        int validationCount = 0;
        if (user.getName().equals("")) {
            name.setError("Please Enter your Full Name.");
            validationCount++;
        }

        if (user.getUsername().equals("")) {
            username.setError("Please Enter your Username.");
            validationCount++;
        }
        if (user.getEmail().equals("")) {
            email.setError("Please Enter your Email.");
            validationCount++;
        }
        if (user.getConfirmPassword().equals("")) {
            confirmPassword.setError("Please Enter your Confirm Password.");
            validationCount++;
        }
        if (user.getPassword().equals("")) {
            password.setError("Please Enter your Password");
            validationCount++;
        }
        if (user.getPhone_no().equals("")) {
            phonenumber.setError("Please Enter your Phone number.");
            validationCount++;
        }
        if (validationCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * Validates the empty login form
     *
     * @param username user name edit text
     * @param password password edit text
     * @return true or false whether validation is successful
     */
    public static boolean validateEmptyLoginForm(EditText username, EditText password) {
        int validationCount = 0;
        if (username.getText().toString().equals("")) {
            username.setError("Please Enter your Username.");
            validationCount++;
        }

        if (password.getText().toString().equals("")) {
            password.setError("Please Enter your Password.");
            validationCount++;
        }
        if (validationCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * Validate empty add edit music form
     *
     * @param title     music title edit text
     * @param genre     music genre edit text
     * @param musicFile music file edit text
     * @return true or false whether validation is successful
     */
    public static boolean validateEmptyAddEditMusicForm(EditText title, EditText genre, EditText musicFile) {
        int validationCount = 0;
        if (title.getText().toString().equals("")) {
            title.setError("Please Enter your Music Title.");
            validationCount++;
        }

        if (genre.getText().toString().equals("")) {
            genre.setError("Please Enter your Music Genre.");
            validationCount++;
        }

        if (validationCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * Validates empty comments
     *
     * @param comment comment edit text
     * @return true or false whether validation is successful
     */
    public static boolean validateEmptyComments(EditText comment) {
        if (comment.getText().toString().equals("")) {
            comment.setError("Please Enter your Comment.");
            return true;
        }
        return false;
    }

    /**
     * Checks the email format
     *
     * @param email email edit text
     * @return true or false whether validation is successful
     */
    public static boolean checkEmailFormat(EditText email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError("Please Enter valid Email.");
            return true;
        }
        return false;
    }

    /**
     * Checks password format
     *
     * @param password password edit text
     * @return true or false whether validation is successful
     */
    public static boolean checkPasswordFormat(final EditText password) {
        Pattern pattern;
        Matcher matcher;

        String passwordPattern =
                "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

        pattern = Pattern.compile(passwordPattern);

        matcher = pattern.matcher(password.getText().toString());
        if (!matcher.matches()) {
            password.setError("Password Must be atleast 6 and atmost 20 characters which contains Uppercase, Lowercase, one special symbol[@#$%], and a digit from 0-9");
            return true;
        }
        return false;
    }
}
