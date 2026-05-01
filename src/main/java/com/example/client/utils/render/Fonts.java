package com.example.client.utils.render;

import com.example.client.utils.render.text.Font;
import com.google.common.base.Suppliers;

public class Fonts {
    public static final Font sf_pro = Suppliers.memoize(() ->
            Font.builder().atlas("sf_pro_regular").data("sf_pro_regular").build()).get();

    public static final Font icons = Suppliers.memoize(() ->
            Font.builder().atlas("noname_icons").data("noname_icons").build()).get();
}
