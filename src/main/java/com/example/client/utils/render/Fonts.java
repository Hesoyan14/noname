package com.example.client.utils.render;

import com.example.client.utils.render.text.Font;

public class Fonts {
    private static Font _sf_pro;
    private static Font _icons;

    public static Font sf_pro() {
        if (_sf_pro == null) {
            _sf_pro = Font.builder().atlas("sf_pro_regular").data("sf_pro_regular").build();
        }
        return _sf_pro;
    }

    public static Font icons() {
        if (_icons == null) {
            _icons = Font.builder().atlas("noname_icons").data("noname_icons").build();
        }
        return _icons;
    }
}
