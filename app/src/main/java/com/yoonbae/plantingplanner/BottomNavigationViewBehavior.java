package com.yoonbae.plantingplanner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

public class BottomNavigationViewBehavior extends CoordinatorLayout.Behavior {

    public BottomNavigationViewBehavior() {
    }

    public BottomNavigationViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //CoordinatorLayout의 자손이 중첩 된 스크롤을 시작하려고 할 때 호출됩니다.
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        //스크롤을 시작할 때 발생 SCROLL_AXIS_HORIZONTAL, SCROLL_AXIS_VERTICA
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    //진행 중 스크롤이 갱신되어 타겟이 스크롤 되거나 스크롤 하려고 했을 때 호출됩니다.
    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);

        //Behavior을 가진 View(child)
        //setTranslationY: 상단 위치를 기준으로 수직 위치를 설정합니다.
        //getTranslationY: 상단 위치를 기준으로 하고 Layout에 배치된 위치에 추가하여 개체의 위치를 효과적으로 지정할 수 있습니다.
        //dyConsumed: 타겟의 스크롤 조작으로 인해 소비된 수직 픽셀
        child.setTranslationY(Math.max(0f, Math.min(Float.parseFloat(String.valueOf(child.getHeight())), child.getTranslationY() + dyConsumed)));
    }

}
