package com.project.musicianhub;

import android.app.AlertDialog;
import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import com.project.musicianhub.model.User;
import com.project.musicianhub.service.RegisterServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by Jerry on 2/8/2017.
 */
@RunWith(AndroidJUnit4.class)
public class RegisterServiceTest {
    User user;
    String imagePath;
    ImageView imageView;
    Context context;
    AlertDialog.Builder builder;

    RegisterServiceImpl registerService;

    @Test
    public void uploadImageAndRegister() {

        user = new User("Akash Rajbanshi", "akash", "akashrajbanshi@email.com", "male", "123", "123", "43434343");
        imagePath = "/image/path/image/image.png";

        RegisterActivity registerActivity = new RegisterActivity();
        context = registerActivity.getApplicationContext();


        registerService = new RegisterServiceImpl();

        //registerService.uploadImageAndRegister(user, imagePath, imageView, context.getApplicationContext(), builder);

        assertTrue(true);

    }
}
