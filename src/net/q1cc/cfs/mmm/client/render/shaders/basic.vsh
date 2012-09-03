#version 130 

in vec3 inPos;
//in float inLight;
in float inOrient;
//attribute float inPadding;

smooth out float vLight;
flat   out float vOrient;
smooth out vec3  vPos;

uniform mat4 projMat;
uniform mat4 posChunkMat;

void main () {
    
    gl_Position = projMat * posChunkMat * vec4(inPos,1);
    vLight = 1;//inLight;
    vOrient = inOrient;
    vPos = inPos;
}
