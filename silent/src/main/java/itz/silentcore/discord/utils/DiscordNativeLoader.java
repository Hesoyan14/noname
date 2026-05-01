package itz.silentcore.discord.utils;

import com.sun.jna.NativeLibrary;

import java.io.*;

public class DiscordNativeLoader {
    private static boolean loaded = false;

    public static void loadLibrary() {
        if (loaded) {
            return;
        }

        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();
            String libPath = null;
            String libExtension = null;

            if (osName.contains("win")) {
                if (arch.contains("64") || arch.contains("amd64")) {
                    libPath = "/win32-x86-64/discord-rpc.dll";
                } else {
                    libPath = "/win32-x86/discord-rpc.dll";
                }
                libExtension = ".dll";
            } else if (osName.contains("linux")) {
                libPath = "/linux-x86-64/discord-rpc.so";
                libExtension = ".so";
            } else if (osName.contains("mac")) {
                libPath = "/macos-x86-64/discord-rpc.dylib";
                libExtension = ".dylib";
            }

            if (libPath != null) {
                // Попытка загрузить встроенную библиотеку
                try (InputStream is = DiscordNativeLoader.class.getResourceAsStream(libPath)) {
                    if (is != null) {
                        File tempFile = File.createTempFile("discord-rpc", libExtension);
                        tempFile.deleteOnExit();

                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }

                        // Устанавливаем права на выполнение для Linux/Mac
                        if (!osName.contains("win")) {
                            tempFile.setExecutable(true, false);
                            tempFile.setReadable(true, false);
                        }

                        NativeLibrary.addSearchPath("discord-rpc", tempFile.getParent());
                        NativeLibrary.getInstance("discord-rpc");
                        loaded = true;
                        System.out.println("Discord RPC: Loaded embedded library");
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Discord RPC: Failed to load embedded library: " + e.getMessage());
                }

                // Если встроенная библиотека не найдена, пытаемся загрузить системную
                if (!loaded && osName.contains("linux")) {
                    try {
                        // Добавляем стандартные пути Linux для поиска библиотеки
                        String[] searchPaths = {
                            "/usr/lib",
                            "/usr/lib64",
                            "/usr/local/lib",
                            "/usr/local/lib64",
                            System.getProperty("user.home") + "/.local/lib"
                        };

                        for (String path : searchPaths) {
                            NativeLibrary.addSearchPath("discord-rpc", path);
                        }

                        NativeLibrary.getInstance("discord-rpc");
                        loaded = true;
                        System.out.println("Discord RPC: Loaded system library");
                    } catch (Exception e) {
                        System.err.println("Discord RPC: Failed to load system library: " + e.getMessage());
                        System.err.println("Discord RPC: Please install discord-rpc library:");
                        System.err.println("  Arch Linux: yay -S discord-rpc или paru -S discord-rpc");
                        System.err.println("  Or download from: https://github.com/discord/discord-rpc/releases");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Discord RPC: Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
