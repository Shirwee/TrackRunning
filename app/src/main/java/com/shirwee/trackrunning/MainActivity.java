package com.shirwee.trackrunning;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener
{

    private TrackRunningView trackRunningView;
    private List<Item> items = new ArrayList<Item>(){{
        add(new Item(0,0,0));
        add(new Item(1,0,0));
        add(new Item(2,0,0));
        add(new Item(3,0,0));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        trackRunningView = findViewById(R.id.track_running_view);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

        trackRunningView.setItems(items);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                trackRunningView.refreshProgress(new Random().nextInt(items.size()), new Random().nextInt(100));
                break;
            case R.id.button2:
                List<Item> items = new ArrayList<Item>(){{
                    add(new Item(0,0, new Random().nextInt(100)));
                    add(new Item(1,0, new Random().nextInt(100)));
                    add(new Item(2,0, new Random().nextInt(100)));
                    add(new Item(3,0, new Random().nextInt(100)));
                }};
                trackRunningView.refreshProgress(items);
                break;
                default:
                    break;
        }
    }
}
