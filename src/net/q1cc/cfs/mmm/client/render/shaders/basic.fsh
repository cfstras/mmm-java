#version 130

uniform sampler2D blockTex;

smooth in float vLight;
smooth in vec2  vTex;
smooth in vec4  vColor;

out vec4 outputColor;

void main () {    
    //vec4 col = texture(blockTex,vTex);
    vec4 col = vec4(0,0,0,1);
    col += vColor;
    col *= vLight;
    outputColor = col;
    
}
