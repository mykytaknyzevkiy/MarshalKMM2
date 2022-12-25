package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.GLES20;

import com.l1inc.viewer.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kirill Kartukov on 20.09.2018.
 */
public class CartDrawData extends BaseDrawingObject {

    private Context context;
    private Typeface typeface;
    private Bitmap cartBg = null;

    public Map<Integer, CartMarker> cartMarkerArray = new HashMap<>();

    public CartDrawData(Context context, Typeface typeface) {
        this.context = context;
        this.typeface = typeface;
        cartBg = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cart_location).copy(Bitmap.Config.ARGB_4444, true);
    }

    public void updateCart(Integer idCart, String cartName, Location cartLocation) {
        Location internalLocation = new Location("");
        double currentLat = Layer.transformLat(cartLocation.getLatitude());
        double currentLon = Layer.transformLon(cartLocation.getLongitude());
        internalLocation.setLatitude(currentLat);
        internalLocation.setLongitude(currentLon);
        CartMarker cartMarker = cartMarkerArray.get(idCart);
        if (cartMarker == null) {
            cartMarkerArray.put(idCart, new CartMarker(idCart, cartName, internalLocation));
        } else {
            cartMarker.cartLocation = internalLocation;
        }
    }

    public void removeCart(Integer idCart) {
        cartMarkerArray.remove(idCart);
        TextureCache.destroyById(-idCart);
    }

    public class CartMarker {

        public Integer idCart;
        public String cartname;
        public Location cartLocation;

        public IndexedTexturedSquare cartMarker;

        public CartMarker(Integer id, String n, Location l) {
            this.idCart = id;
            this.cartname = n;
            this.cartLocation = l;
        }

        public void init(){
            cartMarker = new IndexedTexturedSquare(-idCart, cartname, cartBg, typeface, Color.WHITE, GLES20.GL_CLAMP_TO_EDGE);
        }
    }
}
