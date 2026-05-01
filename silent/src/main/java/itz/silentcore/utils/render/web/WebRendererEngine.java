package itz.silentcore.utils.render.web;


public interface WebRendererEngine {

    boolean isAvailable();

    WebRendererInstance create(WebRendererConfig config);

    default void tick() {
        
    }

    default void shutdown() {
        
    }
}
