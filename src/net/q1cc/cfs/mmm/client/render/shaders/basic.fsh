#version 130

uniform sampler2DArray blockTex;
uniform int numBlocks;

smooth in vec2  vTex;
smooth in vec3  vColor;
smooth in float vTexID;

out vec4 outputColor;

void main () {    
    vec4 col = texture(blockTex,vec3(vTex,vTexID));
    //vec4 col = vec4(vTex,vTexID/numBlocks,1);
    //if(col.a<=0.0001&&col.a>=0) discard;
    col += vec4(vColor,1) - vec4(0.5f,0.5f,0.5f,0);
    outputColor = col;
    vTexID;
}
