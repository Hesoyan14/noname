package itz.silentcore.utils.render;

import com.google.common.base.Suppliers;
import itz.silentcore.utils.render.text.Font;

public class Fonts {
    public static final Font sf_pro = Suppliers.memoize(() ->
            Font.builder().atlas("sf_pro_regular").data("sf_pro_regular").build()).get();
    public static final Font icons = Suppliers.memoize(() ->
            Font.builder().atlas("silentcoreicons").data("silentcoreicons").build()).get();
}