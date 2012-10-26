#version 130

uniform sampler2D blockTex;

smooth in vec2  vTex;
smooth in vec4  vColor;
flat   in int   vTexID;

out vec4 outputColor;

void main () {    
    //vec4 col = texture(blockTex,vTex);
    vec4 col = vec4(vTex.s,vTex.t,0,1);
    //if(col.a<=0.0001&&col.a>=0) discard;
    //col *= vColor;
    outputColor = col;
    vTexID;
}
