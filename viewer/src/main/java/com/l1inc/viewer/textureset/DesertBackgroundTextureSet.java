package com.l1inc.viewer.textureset;

import com.l1inc.viewer.R;

/**
 * Created by Yevhen Paschenko on 12/6/2016.
 */

public class DesertBackgroundTextureSet extends TextureSet {

	public DesertBackgroundTextureSet() {
		setBackgroundTexture(R.drawable.v3d_desert_background);
		setPerimeterTexture(R.drawable.v3d_desert_background);
		setPerimeterTextureCompressed(R.raw.v3d_desert_background);
		setBackgroundTextureCompressed(R.raw.v3d_desert_background);
	}

	public void mergeTo(final TextureSet otherTextureSet) {
		otherTextureSet.setBackgroundTexture(getBackgroundTexture());
		otherTextureSet.setPerimeterTexture(getPerimeterTexture());
		otherTextureSet.setPerimeterTextureCompressed(getPerimeterTextureCompressed());
		otherTextureSet.setBackgroundTextureCompressed(getBackgroundTextureCompressed());
	}
}
