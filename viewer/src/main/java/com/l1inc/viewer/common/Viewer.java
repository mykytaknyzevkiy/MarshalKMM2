package com.l1inc.viewer.common;

import android.graphics.Rect;
import android.support.annotation.Keep;

import com.l1inc.viewer.Course3DRendererBase;
import com.l1inc.viewer.HoleWithinCourse;

/**
 * Created by Kirill Kartukov on 27.03.2018.
 */

public interface Viewer {

    @Keep
    public interface NavigationModeChangedListener {
        void onNavigationModeChanged(Course3DRendererBase.NavigationMode mode);
    }
    @Keep
    public interface GreenPositionChangeListener {
        void onPositionChanged(Rect green);
    }
    @Keep
    public interface FlyoverFinishListener {
        void onFlyoverFinished();
    }
    @Keep
    public interface CurrentHoleChangedListener {
        void onHoleChanged(final HoleWithinCourse hole);
    }
    @Keep
    public interface CurrentCourseChangedListener {
        void onCourseChanged();
    }
    @Keep
    public interface HoleLoadingStateChangedListener {
        void onStartLoading();

        void onFinishLoading();
    }
    @Keep
    public interface FrameRenderListener {
        void onFinishedRender();
    }

}
