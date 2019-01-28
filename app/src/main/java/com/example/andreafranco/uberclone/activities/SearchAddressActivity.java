package com.example.andreafranco.uberclone.activities;

import android.os.Bundle;

import com.example.andreafranco.uberclone.R;
import com.example.andreafranco.uberclone.activities.BaseActivity;

public class SearchAddressActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);
        setupToolbar();
    }
}
