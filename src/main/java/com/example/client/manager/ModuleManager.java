package com.example.client.manager;

import com.example.client.feature.module.api.Category;
import com.example.client.feature.module.api.Module;
import com.example.client.feature.module.api.ModuleAnnotation;
import com.example.client.utils.client.IMinecraft;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager implements IMinecraft {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        autoRegister("com.example.client.feature.module.impl");
    }

    private void autoRegister(String packageName) {
        String path = packageName.replace('.', '/');
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL resource = cl.getResource(path);
            if (resource != null) {
                scanDir(new File(resource.toURI()), packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanDir(File dir, String pkg) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDir(f, pkg + "." + f.getName());
            } else if (f.getName().endsWith(".class")) {
                String className = pkg + "." + f.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (Module.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ModuleAnnotation.class)) {
                        Module m = (Module) clazz.getDeclaredConstructor().newInstance();
                        modules.add(m);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    public List<Module> getModules() { return modules; }

    public List<Module> getModules(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    public Module getModule(String name) {
        return modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Module> getActiveModules() {
        return modules.stream().filter(Module::isEnabled).collect(Collectors.toList());
    }
}
