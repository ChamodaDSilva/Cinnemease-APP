package com.example.cinnamease;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.cinnamease.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            System.out.println("Item got "+item.toString());
            if (item.toString().equals("Home")) {
                replaceFragment(new HomeFragment());
            } else if (item.toString().equals("Scan"))  {
                replaceFragment(new ScanFragment());
            } else if (item.toString().equals("History"))  {
                replaceFragment(new HistoryFragment());
            }
            return true;
        });


    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

}
