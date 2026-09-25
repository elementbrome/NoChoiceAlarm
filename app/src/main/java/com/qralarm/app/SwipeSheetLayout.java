package com.qralarm.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

/**
 * Lightweight draggable bottom sheet without external UI dependencies.
 * Starts collapsed, can be expanded upward, collapsed again, or dismissed downward.
 */
public class SwipeSheetLayout extends LinearLayout {
    public interface Callback {
        void onExpandedChanged(boolean expanded);
        void onDismissed();
    }

    private final int touchSlop;
    private int peekHeightPx;
    private float collapsedOffset;
    private float hiddenOffset;
    private float downY;
    private float startTranslation;
    private boolean dragging;
    private boolean expanded;
    private boolean initialized;
    private View scrollChild;
    private Callback callback;

    public SwipeSheetLayout(Context context) {
        this(context, null);
    }

    public SwipeSheetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setClickable(true);
    }

    public void setPeekHeightPx(int peekHeightPx) {
        this.peekHeightPx = Math.max(1, peekHeightPx);
        if (getHeight() > 0) {
            int peek = Math.min(this.peekHeightPx, getHeight());
            collapsedOffset = Math.max(0f, getHeight() - peek);
            hiddenOffset = getHeight() + Ui.dp(getContext(), 24);
            if (!dragging) setTranslationY(expanded ? 0f : collapsedOffset);
        }
        requestLayout();
    }

    public void setScrollChild(View scrollChild) {
        this.scrollChild = scrollChild;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void expand() {
        animateTo(0f, true, false);
    }

    public void collapse() {
        animateTo(collapsedOffset, false, false);
    }

    public void dismissSheet() {
        animateTo(hiddenOffset, false, true);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h <= 0) return;
        int peek = peekHeightPx > 0 ? Math.min(peekHeightPx, h) : h / 2;
        collapsedOffset = Math.max(0f, h - peek);
        hiddenOffset = h + Ui.dp(getContext(), 24);
        if (!initialized) {
            initialized = true;
            setTranslationY(hiddenOffset);
            post(() -> animateTo(collapsedOffset, false, false));
        } else if (!dragging) {
            setTranslationY(expanded ? 0f : collapsedOffset);
        }
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getRawY();
                startTranslation = getTranslationY();
                dragging = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                float dy = ev.getRawY() - downY;
                if (Math.abs(dy) <= touchSlop) return false;

                if (!expanded) {
                    dragging = true;
                    return true;
                }

                // When expanded, allow normal scrolling upward. A downward gesture at
                // the very top of the scroll content controls the sheet.
                if (dy > 0 && (scrollChild == null || !scrollChild.canScrollVertically(-1))) {
                    dragging = true;
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getRawY();
                startTranslation = getTranslationY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float dy = ev.getRawY() - downY;
                if (!dragging && Math.abs(dy) > touchSlop) dragging = true;
                if (dragging) {
                    float next = clamp(startTranslation + dy, 0f, hiddenOffset);
                    setTranslationY(next);
                    return true;
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging) settleAfterDrag();
                dragging = false;
                return true;
            default:
                return true;
        }
    }

    private void settleAfterDrag() {
        float y = getTranslationY();
        if (y > collapsedOffset + Math.max(Ui.dp(getContext(), 64), getHeight() * 0.08f)) {
            dismissSheet();
            return;
        }
        if (y < collapsedOffset * 0.72f) {
            expand();
            return;
        }
        if (expanded && y < collapsedOffset * 0.45f) {
            expand();
        } else {
            collapse();
        }
    }

    private void animateTo(float target, boolean targetExpanded, boolean dismiss) {
        animate().cancel();
        animate()
                .translationY(target)
                .setDuration(230)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    if (dismiss) {
                        if (callback != null) callback.onDismissed();
                        return;
                    }
                    expanded = targetExpanded;
                    if (callback != null) callback.onExpandedChanged(expanded);
                })
                .start();
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
