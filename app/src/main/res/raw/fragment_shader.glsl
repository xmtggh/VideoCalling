precision mediump float;//精度 为float
varying vec2 v_texPo;//纹理位置  接收于vertex_shader
uniform sampler2D sampler_y;//纹理y
uniform sampler2D sampler_u;//纹理u
uniform sampler2D sampler_v;//纹理v

void main() {
    //yuv420->rgb
    float y,u,v;
    y = texture2D(sampler_y,v_texPo).r;
    u = texture2D(sampler_u,v_texPo).r- 0.5;
    v = texture2D(sampler_v,v_texPo).r- 0.5;
    vec3 rgb;
    rgb.r = y + 1.403 * v;
    rgb.g = y - 0.344 * u - 0.714 * v;
    rgb.b = y + 1.770 * u;

    gl_FragColor=vec4(rgb,1);
}
