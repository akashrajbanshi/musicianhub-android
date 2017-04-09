package com.project.musicianhub.customUI;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.project.musicianhub.R;

/**
 * Custom class for creating clearable edit text
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class ClearableEditText extends RelativeLayout {
    LayoutInflater inflater = null;

    EditText edit_text;
    Button btn_clear;

    /**
     * Constructor for the Clearable Edit Text
     *
     * @param context  current context
     * @param attrs    attribute set
     * @param defStyle definition style of the edit text
     */
    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    /**
     * Constructor for the Clearable Edit Text
     *
     * @param context current context
     * @param attrs   attribute set
     */
    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    /**
     * Constructor for the Clearable Edit Text
     *
     * @param context current context
     */
    public ClearableEditText(Context context) {
        super(context);
        initViews();
    }

    /**
     * Initializes the view by initializing edit text and clear button
     */
    void initViews() {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.clearable_edit_text, this, true);
        edit_text = (EditText) findViewById(R.id.clearable_edit);
        btn_clear = (Button) findViewById(R.id.clearable_button_clear);
        btn_clear.setVisibility(RelativeLayout.INVISIBLE);
        clearText();
        showHideClearButton();
    }

    /**
     * Clears the text in the edit text
     */
    void clearText() {
        btn_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_text.setText("");
            }
        });
    }

    /**
     * Show and hide the clear button inside edit text
     */
    void showHideClearButton() {
        edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    btn_clear.setVisibility(RelativeLayout.VISIBLE);
                else
                    btn_clear.setVisibility(RelativeLayout.INVISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Gets the text from editext
     *
     * @return editable text from the edit text
     */
    public Editable getText() {
        Editable text = edit_text.getText();
        return text;
    }
}
