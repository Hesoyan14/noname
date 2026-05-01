#version 150

uniform float time;
uniform vec2 resolution;

out vec4 fragColor;

void main() {
    vec2 uPos = (gl_FragCoord.xy / resolution.xy);
    
    // Корректируем aspect ratio
    uPos.x *= resolution.x / resolution.y;
    
    // Центрируем
    uPos.x -= (resolution.x / resolution.y) / 2.0;
    uPos.y -= 0.5;
    
    vec3 color = vec3(0.0);
    float vertColor = 1.0;
    
    for(float i = 0.0; i < 5.0; ++i) {
        float t = time * 0.9;
        
        uPos.y += sin(uPos.x * i + t + i / 2.0) * 0.1;
        
        float fTemp = abs(1.0 / uPos.y / 100.0);
        vertColor += fTemp;
        
        // Фиолетовая цветовая схема (165, 129, 238) = (0.647, 0.506, 0.933)
        color += vec3(
            fTemp * (10.0 - i) / 10.0 * 0.647,  // R - фиолетовый оттенок
            fTemp * i / 10.0 * 0.506,            // G - меньше зеленого
            pow(fTemp, 1.5) * 1.5 * 0.933        // B - больше синего
        );
    }
    
    fragColor = vec4(color, 1.0);
}
