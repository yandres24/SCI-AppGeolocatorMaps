package com.sci.www.sci_appgeolocator.Classes;

import android.graphics.drawable.Drawable;

/**
 * Created by Administrador on 23/06/2015.
 */
public class DrawerItem {

    Drawable itemIcon;
    String itemTitle;

    public DrawerItem(Drawable itemIcon, String itemTitle) {

        this.itemIcon = itemIcon;
        this.itemTitle = itemTitle;

    }

    public Drawable getItemIcon() {
        return itemIcon;
    }

    public String getItemTitle() {
        return itemTitle;
    }
}


