package com.project.musicianhub.util;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

/**
 * Util class for the volley's retry policy
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class VolleyUtil {

    public static void setRetryPolicyForVolley(CustomMultipartRequest req) {
        int x = 0;
        req.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                x, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public static void setRetryPolicyForVolley(JsonObjectRequest postRequest) {
        int x = 0;
        postRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                x, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public static void setRetryPolicyForVolley(StringRequest postRequest) {
        int x = 0;
        postRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                x, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

}
