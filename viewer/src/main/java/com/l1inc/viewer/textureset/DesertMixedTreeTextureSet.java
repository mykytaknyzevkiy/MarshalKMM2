package com.l1inc.viewer.textureset;

import com.l1inc.viewer.R;

/**
 * Created by Yevhen Paschenko on 12/7/2016.
 */

public class DesertMixedTreeTextureSet extends TextureSet {

	public DesertMixedTreeTextureSet() {
		setTreeTextureSet(R.array.desert_mixed_tree_set);
		setTreeShadowTextureSet(R.array.desert_mixed_tree_shadow_set);
	}

	@Override
	public void mergeTo(final TextureSet otherTextureSet) {
		otherTextureSet.setTreeTextureSet(getTreeTextureSet());
		otherTextureSet.setTreeShadowTextureSet(getTreeShadowTextureSet());
	}
}
