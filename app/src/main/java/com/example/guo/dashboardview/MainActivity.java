package com.example.guo.dashboardview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DashboardView mDashboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDashboardView = findViewById(R.id.dashboardView);
        mDashboardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.dashboardView && mDashboardView != null) {
            ObjectAnimator animator = ObjectAnimator.ofInt(mDashboardView, "mCurrentTmp",
                    mDashboardView.getMCurrentTmp(), new Random().nextInt(100));

            animator.setDuration(500).setInterpolator(new LinearInterpolator());

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mDashboardView.setMCurrentTmp(value);
                }
            });
            animator.start();
        }
    }
}
