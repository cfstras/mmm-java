#version 130 

in vec3  inPos;
in float inLight;
in int   inOrient;
in int   inColor;
in int   inBlock;
in float padding;

smooth out float vLight;
smooth out vec2  vTex;
smooth out vec4  vColor;

uniform mat4 projMat;
uniform mat4 posChunkMat;

uniform int texRows;
uniform int texColumns;

void main () {
    
    gl_Position = projMat * posChunkMat * vec4(inPos,1);
    vLight = inLight;
    
    int or = inOrient;
    vec3 normal;
    normal.x = (or & 1)/1;
    normal.y = (or & 2)/2;
    normal.z = (or & 4)/4;
    normal *= 1 - 2*((or & 8)/8);
    
    vTex.s = (or & 16)/16;
    vTex.t = (or & 32)/32;
    
    int vSide;
    if     (normal.y==1 ) vSide=0;//top
    else if(normal.y==-1) vSide=1;//bot
    else if(normal.x==-1) vSide=2;//left
    else if(normal.x==1 ) vSide=3;//right
    else if(normal.z==-1) vSide=4;//front
    else if(normal.z==1 ) vSide=5;//back
    
    vTex.s /= texColumns;
    vTex.t /= texRows;
    vTex.t += inBlock/texRows;
    vTex.s += vSide/texColumns;
    
    vColor.r = float((inColor & 0xff000000)>>(3*8))/255;
    vColor.g = float((inColor & 0x00ff0000)>>(2*8))/255;
    vColor.b = float((inColor & 0x0000ff00)>>(1*8))/255;
    vColor.a = float((inColor & 0x000000ff))/255;
    
}
