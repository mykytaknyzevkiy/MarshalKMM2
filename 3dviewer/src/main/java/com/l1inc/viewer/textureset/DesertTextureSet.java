package com.l1inc.viewer.textureset;

import com.l1inc.viewer.R;

/**
 * Created by Kirill Kartukov on 14.02.2018.
 */

public class DesertTextureSet extends TextureSet {

    public DesertTextureSet() {
        setTreeTextureSet(R.array.desert_mixed_tree_set);
        setTreeShadowTextureSet(R.array.desert_mixed_tree_shadow_set);
        setBackgroundTexture(R.drawable.v3d_desert_background);
        setPerimeterTexture(R.drawable.v3d_gpsmap_background);
        setPerimeterTextureCompressed(R.raw.v3d_gpsmap_background);
        setBackgroundTextureCompressed(R.raw.v3d_desert_background);
        setBackgroundTexture2d(R.drawable.v3d_desert_background);
        setBackgroundTexture2dCompressed(R.raw.v3d_desert_background);
    }

    public void mergeTo(final TextureSet otherTextureSet) {
        otherTextureSet.setTreeTextureSet(getTreeTextureSet());
        otherTextureSet.setTreeShadowTextureSet(getTreeShadowTextureSet());
        otherTextureSet.setBackgroundTexture(getBackgroundTexture());
        otherTextureSet.setPerimeterTexture(getPerimeterTexture());
        otherTextureSet.setPerimeterTextureCompressed(getPerimeterTextureCompressed());
        otherTextureSet.setBackgroundTextureCompressed(getBackgroundTextureCompressed());
        otherTextureSet.setBackgroundTexture2d(getBackgroundTexture2d());
        otherTextureSet.setBackgroundTexture2dCompressed(getBackgroundTexture2dCompressed());
    }

}
