package com.l1inc.viewer.textureset;

import com.l1inc.viewer.R;

/**
 * Created by Yevhen Paschenko on 12/7/2016.
 */

public class DesertWithRoughTextureSet extends TextureSet {

	public DesertWithRoughTextureSet() {
		setBackgroundTexture(R.drawable.v3d_desert_background);
		setPerimeterTexture(R.drawable.v3d_gpsmap_background);
		setPerimeterTextureCompressed(R.raw.v3d_gpsmap_background);
		setBackgroundTextureCompressed(R.raw.v3d_desert_background);
		setBackgroundTexture2d(R.drawable.v3d_desert_background);
		setBackgroundTexture2dCompressed(R.raw.v3d_desert_background);
	}

	@Override
	public void mergeTo(final TextureSet otherTextureSet) {
		otherTextureSet.setBackgroundTexture(getBackgroundTexture());
		otherTextureSet.setPerimeterTexture(getPerimeterTexture());
		otherTextureSet.setPerimeterTextureCompressed(getPerimeterTextureCompressed());
		otherTextureSet.setBackgroundTextureCompressed(getBackgroundTextureCompressed());
		otherTextureSet.setBackgroundTexture2d(getBackgroundTexture2d());
		otherTextureSet.setBackgroundTexture2dCompressed(getBackgroundTexture2dCompressed());
	}
}
