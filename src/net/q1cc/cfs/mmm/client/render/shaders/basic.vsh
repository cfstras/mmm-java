#version 130 

in vec3  inPos;
in vec2  inTex;
in float inTexID;
in vec3  inColor;

smooth out vec2  vTex;
smooth out float vTexID;
smooth out vec3  vColor;

uniform mat4 projMat;
uniform mat4 posChunkMat;

uniform int texRows;
uniform int texColumns;

void main () {
    
    gl_Position = projMat * posChunkMat * vec4(inPos,1);
    
    vTex = inTex;
    vTexID = inTexID;
    vColor = inColor;
}
