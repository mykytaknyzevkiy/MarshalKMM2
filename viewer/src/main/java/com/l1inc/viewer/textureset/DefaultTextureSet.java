package com.l1inc.viewer.textureset;

import com.l1inc.viewer.R;

/**
 * Created by Yevhen Paschenko on 9/28/2016.
 */

public class DefaultTextureSet extends TextureSet {

	public DefaultTextureSet() {
		setTreeTextureSet(R.array.tree_set);
		setTreeShadowTextureSet(R.array.tree_shadow_set);
		setBackgroundTexture(R.drawable.v3d_gpsmap_background);
		setPerimeterTexture(R.drawable.v3d_gpsmap_background);
		setPerimeterTextureCompressed(R.raw.v3d_gpsmap_background);
		setBackgroundTextureCompressed(R.raw.v3d_gpsmap_background);
		setBackgroundTexture2d(R.drawable.v2d_background);
		setBackgroundTexture2dCompressed(R.raw.v2d_background);
	}

	@Override
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
