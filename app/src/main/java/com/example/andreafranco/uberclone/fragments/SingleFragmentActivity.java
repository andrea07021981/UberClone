package com.example.andreafranco.uberclone.fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.andreafranco.uberclone.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    @LayoutRes
    protected int getLayoutResId(){
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragmentContainer = fm.findFragmentById(R.id.fragment_container);
        if (fragmentContainer == null) {
            fragmentContainer = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragmentContainer, fragmentContainer.getClass().getSimpleName())
                    .commit();
        }
    }

    protected abstract Fragment createFragment();
}