package com.l1inc.viewer.drawing;

import com.l1inc.viewer.Course3DRenderer;

import java.util.List;


/**
 * Created by Yevhen Paschenko on 3/30/2017.
 */

public class CreekRenderer extends IDrawable {

    private List<Creek> creekList;

    public CreekRenderer(final List<Creek> creekList) {
        this.creekList = creekList;
    }

    @Override
    public void draw(Course3DRenderer renderer) {
        for (int i = 0; i < creekList.size(); i++) {
            creekList.get(i).draw(renderer);
        }
    }

    @Override
    public void destroy() {
        for (final Creek creek : creekList) {
            creek.destroy();
        }

        creekList.clear();
    }
}
