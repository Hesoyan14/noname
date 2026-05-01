package itz.silentcore.utils.simulate;

import itz.silentcore.utils.client.IMinecraft;

public class Simulations implements IMinecraft {
    
    public static double[] calculateDirection(boolean forward, boolean sideways, float speed) {
        if (mc.player == null) return new double[]{0, 0};
        
        float yaw = mc.player.getYaw();
        float moveForward = forward ? 1.0f : 0.0f;
        float moveSideways = sideways ? 1.0f : 0.0f;
        
        if (moveForward == 0 && moveSideways == 0) {
            return new double[]{0, 0};
        }
        
        float angle = yaw;
        if (moveForward < 0) angle += 180;
        if (moveSideways > 0) angle -= 90 * (moveForward != 0 ? moveForward : 1);
        if (moveSideways < 0) angle += 90 * (moveForward != 0 ? moveForward : 1);
        
        double radians = Math.toRadians(angle);
        double x = -Math.sin(radians) * speed;
        double z = Math.cos(radians) * speed;
        
        return new double[]{x, z};
    }
}
