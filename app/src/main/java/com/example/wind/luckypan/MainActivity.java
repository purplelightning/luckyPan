package com.example.wind.luckypan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private luckyPan mLuckyPan;
    private ImageView btn_start;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLuckyPan = (luckyPan) findViewById(R.id.lucky_pan);
        btn_start = (ImageView) findViewById(R.id.start_btn);


        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //设置转盘停在各个盘块的概率
                ProbabilityControl pp = new ProbabilityControl(0, 0, 1, 0, 2, 0);
                index = pp.setPro();

                if (!mLuckyPan.isStart()) {
                    mLuckyPan.luckyStart(index);
                    btn_start.setImageResource(R.drawable.stop);
                } else {//转盘在旋转且停止按钮还没按时，按下起作用
                    if (!mLuckyPan.isShouldEnd()) {
                        mLuckyPan.luckyEnd();
                        btn_start.setImageResource(R.drawable.start);
                    }

                }
            }
        });
    }
}
