package com.l1inc.viewer.textureset;

import com.l1inc.viewer.R;

/**
 * Created by Yevhen Paschenko on 9/28/2016.
 */

public abstract class TextureSet {

	private int treeTextureSet;
	private int treeShadowTextureSet;
	private Integer backgroundTexture;
	private Integer backgroundTextureCompressed;
	private Integer perimeterTexture;
	private Integer perimeterTextureCompressed;
	private Integer backgroundTexture2d;
	private Integer backgroundTexture2dCompressed;

	public abstract void mergeTo(final TextureSet otherTextureSet);

	public int getTreeTextureSet() {
		return treeTextureSet;
	}

	public void setTreeTextureSet(int treeTextureSet) {
		this.treeTextureSet = treeTextureSet;
	}

	public int getTreeShadowTextureSet() {
		return treeShadowTextureSet;
	}

	public void setTreeShadowTextureSet(int treeShadowTextureSet) {
		this.treeShadowTextureSet = treeShadowTextureSet;
	}

	public Integer getBackgroundTexture() {
		return backgroundTexture;
	}

	public void setBackgroundTexture(int backgroundTexture) {
		this.backgroundTexture = backgroundTexture;
	}

	public Integer getPerimeterTexture() {
		return perimeterTexture;
	}

	public void setPerimeterTexture(Integer perimeterTexture) {
		this.perimeterTexture = perimeterTexture;
	}

	public void setBackgroundTexture(Integer backgroundTexture) {
		this.backgroundTexture = backgroundTexture;
	}

	public Integer getBackgroundTextureCompressed() {
		return backgroundTextureCompressed;
	}

	public void setBackgroundTextureCompressed(Integer backgroundTextureCompressed) {
		this.backgroundTextureCompressed = backgroundTextureCompressed;
	}

	public Integer getPerimeterTextureCompressed() {
		return perimeterTextureCompressed;
	}

	public void setPerimeterTextureCompressed(Integer perimeterTextureCompressed) {
		this.perimeterTextureCompressed = perimeterTextureCompressed;
	}

	public Integer getBackgroundTexture2d() {
		return backgroundTexture2d;
	}

	public void setBackgroundTexture2d(Integer backgroundTexture2d) {
		this.backgroundTexture2d = backgroundTexture2d;
	}

	public Integer getBackgroundTexture2dCompressed() {
		return backgroundTexture2dCompressed;
	}

	public void setBackgroundTexture2dCompressed(Integer backgroundTexture2dCompressed) {
		this.backgroundTexture2dCompressed = backgroundTexture2dCompressed;
	}

	public boolean isCustom(){
		return !(getTreeTextureSet() == R.array.tree_set
				&& getTreeShadowTextureSet() == R.array.tree_shadow_set
				&& getBackgroundTexture() == R.drawable.v3d_gpsmap_background
				&& getPerimeterTexture() == R.drawable.v3d_gpsmap_background
				&& getPerimeterTextureCompressed() == R.raw.v3d_gpsmap_background
				&& getBackgroundTextureCompressed() == R.raw.v3d_gpsmap_background
				&& getBackgroundTexture2d() == R.drawable.v2d_background
				&& getBackgroundTexture2dCompressed() == R.raw.v2d_background);
	}
}
