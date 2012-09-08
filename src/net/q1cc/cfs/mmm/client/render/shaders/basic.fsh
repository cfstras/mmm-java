#version 130

uniform sampler2D blockTex;

smooth in vec2  vTex;
smooth in vec4  vColor;

out vec4 outputColor;

void main () {    
    vec4 col = texture(blockTex,vTex);
    //vec4 col = vec4(vTex.s,vTex.t,0,1);
    col *= vColor;
    outputColor = col;
    
}