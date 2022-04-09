package com.pahaltutorials.pahaltutorials;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pahaltutorials.pahaltutorials.fragments.HomeCoursesFragment;
import com.pahaltutorials.pahaltutorials.fragments.ListFragment;

public class MainActivity extends AppCompatActivity{

    private Fragment fragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = new HomeCoursesFragment();
        //fragment = new WebViewFile();
        //Launch Fragment for the First Time
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, fragment).commit();
    }
}