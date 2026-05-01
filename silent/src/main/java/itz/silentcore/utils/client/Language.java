package itz.silentcore.utils.client;

import lombok.Getter;
import lombok.Setter;

public enum Language {
    Russian,
    English;

    @Getter
    @Setter
    private static Language currentLanguage = Russian;

    public static boolean isRussian() {
        return currentLanguage == Russian;
    }
    public static boolean isEnglish() {
        return currentLanguage == English;
    }
}
