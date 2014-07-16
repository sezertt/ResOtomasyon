package com.res_otomasyon.resotomasyon;

import android.media.Image;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by sezer on 08.07.2014.
 */
public class listGroup {

    public String productGroupName;

    public final List<String> productName = new ArrayList<String>();
    public final List<String> productPrice = new ArrayList<String>();
    public final List<String> productInfo = new ArrayList<String>();
    public final List<Image> productImage = new ArrayList<Image>();

    public listGroup(String productGroupName) {
        this.productGroupName = productGroupName;

    }

}