package com.example.drawboard;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.example.drawboard.view.DrawBoardView;

public class MainActivity extends FragmentActivity {

    private DrawBoardView mDrawBoardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mDrawBoardView = (DrawBoardView) findViewById(R.id.draw_board_view);
    }

}
