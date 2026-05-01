package itz.silentcore.utils.client;

import org.lwjgl.glfw.GLFW;

public class Keyboard {
    public static String getKeyName(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode <= 0) {
            return "NONE";
        }
        // Try GLFW textual name first (for printable keys)
        String keyName = GLFW.glfwGetKeyName(keyCode, 0);
        if (keyName != null && !keyName.isEmpty()) {
            return keyName.toUpperCase();
        }
        // Fallback mapping for non-printable and modifier keys
        return switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_PAGE_UP -> "PAGEUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PAGEDOWN";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "SCROLL";
            case GLFW.GLFW_KEY_NUM_LOCK -> "NUMLOCK";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "PRTSC";
            case GLFW.GLFW_KEY_PAUSE -> "PAUSE";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            // Modifiers
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "LSUPER";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "RSUPER";
            case GLFW.GLFW_KEY_MENU -> "MENU";
            // Function keys
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_F13 -> "F13";
            case GLFW.GLFW_KEY_F14 -> "F14";
            case GLFW.GLFW_KEY_F15 -> "F15";
            case GLFW.GLFW_KEY_F16 -> "F16";
            case GLFW.GLFW_KEY_F17 -> "F17";
            case GLFW.GLFW_KEY_F18 -> "F18";
            case GLFW.GLFW_KEY_F19 -> "F19";
            case GLFW.GLFW_KEY_F20 -> "F20";
            case GLFW.GLFW_KEY_F21 -> "F21";
            case GLFW.GLFW_KEY_F22 -> "F22";
            case GLFW.GLFW_KEY_F23 -> "F23";
            case GLFW.GLFW_KEY_F24 -> "F24";
            case GLFW.GLFW_KEY_F25 -> "F25";
            // Keypad
            case GLFW.GLFW_KEY_KP_0 -> "KP0";
            case GLFW.GLFW_KEY_KP_1 -> "KP1";
            case GLFW.GLFW_KEY_KP_2 -> "KP2";
            case GLFW.GLFW_KEY_KP_3 -> "KP3";
            case GLFW.GLFW_KEY_KP_4 -> "KP4";
            case GLFW.GLFW_KEY_KP_5 -> "KP5";
            case GLFW.GLFW_KEY_KP_6 -> "KP6";
            case GLFW.GLFW_KEY_KP_7 -> "KP7";
            case GLFW.GLFW_KEY_KP_8 -> "KP8";
            case GLFW.GLFW_KEY_KP_9 -> "KP9";
            case GLFW.GLFW_KEY_KP_DECIMAL -> "KP_DECIMAL";
            case GLFW.GLFW_KEY_KP_DIVIDE -> "KP_DIVIDE";
            case GLFW.GLFW_KEY_KP_MULTIPLY -> "KP_MULTIPLY";
            case GLFW.GLFW_KEY_KP_SUBTRACT -> "KP_SUBTRACT";
            case GLFW.GLFW_KEY_KP_ADD -> "KP_ADD";
            case GLFW.GLFW_KEY_KP_ENTER -> "KP_ENTER";
            case GLFW.GLFW_KEY_KP_EQUAL -> "KP_EQUAL";
            default -> "KEY_" + keyCode;
        };
    }
}
