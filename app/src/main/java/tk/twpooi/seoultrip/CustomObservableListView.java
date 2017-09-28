package tk.twpooi.seoultrip;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;

/**
 * Created by tw on 2016-08-29.
 */
public class CustomObservableListView extends ObservableListView {

    private boolean isPagingEnabled = true;

    public CustomObservableListView(Context context) {
        super(context);
    }

    public CustomObservableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomObservableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }

}
