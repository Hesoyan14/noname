package itz.silentcore.manager;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.KeyEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.ui.screen.csgui.CsGui;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.client.Log;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import java.io.File;
import java.net.URL;
import java.util.*;

@Getter
public final class ModuleManager implements IMinecraft {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        init();
        SilentCore.getInstance().eventBus.register(this);
    }

    private void init() {
        autoRegisterModules();
    }

    private void autoRegisterModules() {
        try {
            String modules = "itz.silentcore.feature.module.impl";
            List<Class<?>> moduleClasses = findModuleClasses(modules);
            for (Class<?> clazz : moduleClasses) {
                if (Module.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ModuleAnnotation.class)) {
                    try {
                        Module module = (Module) clazz.getDeclaredConstructor().newInstance();
                        registerModule(module);
                    } catch (Exception e) {
                        Log.error("Не удалось зарегистрировать модуль: " + clazz.getSimpleName());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            Log.error("Не удалось зарегистрировать модули");
            e.printStackTrace();
        }
    }

    private List<Class<?>> findModuleClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);
            if (resource != null) {
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    scanDirectory(directory, packageName, classes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    private void scanDirectory(File directory, String packageName, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                }
            }
        }
    }

    private void registerModule(Module module) {
        modules.add(module);
    }

    public Module getModule(String name) {
        return modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Set<Module> getActiveModules() {
        Set<Module> active = new HashSet<>();
        for (Module module : modules) {
            if (module.isEnabled()) active.add(module);
        }
        return active;
    }

    public List<Module> getModules(Category category) {
        List<Module> temp = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                temp.add(module);
            }
        }

        return temp;
    }

    @Subscribe
    public void onKey(KeyEvent event) {
        if (mc.currentScreen != null || event.getAction() != GLFW.GLFW_PRESS) return;
        for (Module module : modules) {
            if (module.getKey() == event.getKey() && module.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                module.cToggle();
            }
        }

        if (event.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            mc.setScreen(new CsGui());
        }
    }

    public int getActiveModules(Category cat) {
        int count = 0;
        for (Module module : modules) {
            if (module.isEnabled() && module.getCategory().equals(cat)) {
                count++;
            }
        }

        return count;
    }

    public int getModulesCount(Category cat) {
        int count = 0;
        for (Module module : modules) {
            if (module.getCategory().equals(cat)) {
                count++;
            }
        }

        return count;
    }
}