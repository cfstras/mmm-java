#version 130 

attribute vec3 inPos;
attribute float inLight;
attribute vec3 inTex;
//attribute float inPadding;

smooth out float vLight;
smooth out vec3 vTex;
smooth out vec3 vPos;

uniform mat4 projMat;
uniform mat4 posChunkMat;

void main () {
    
    gl_Position = projMat * posChunkMat * vec4(inPos,1);
    vLight = inLight;
    vTex = inTex;
    vPos = inPos;
}
