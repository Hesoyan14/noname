package itz.silentcore.utils.client;

import org.lwjgl.glfw.GLFWNativeWin32;
import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.win32.*;

public class WindowStyle {
    public interface DwmApi extends StdCallLibrary {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);
        void DwmSetWindowAttribute(HWND hwnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);
    }

    public static void darkmode(long windowHandle) {
        if (isLinux()) return;
        long hwnd = GLFWNativeWin32.glfwGetWin32Window(windowHandle);
        HWND hwndJna = new HWND(new Pointer(hwnd));
        int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
        Memory darkModeEnabled = new Memory(4);
        darkModeEnabled.setInt(0, 1);
        DwmApi.INSTANCE.DwmSetWindowAttribute(hwndJna, DWMWA_USE_IMMERSIVE_DARK_MODE, darkModeEnabled, 4);
    }

    public static void defaultmode(long windowHandle) {
        if (isLinux()) return;
        long hwnd = GLFWNativeWin32.glfwGetWin32Window(windowHandle);
        HWND hwndJna = new HWND(new Pointer(hwnd));
        int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
        Memory darkModeEnabled = new Memory(4);
        darkModeEnabled.setInt(0, 0);
        DwmApi.INSTANCE.DwmSetWindowAttribute(hwndJna, DWMWA_USE_IMMERSIVE_DARK_MODE, darkModeEnabled, 4);
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux");
    }
}