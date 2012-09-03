#version 130

smooth in float vLight;
flat in float vOrient;
smooth in vec3 vPos;

out vec4 outputColor;

void main () {
    //vec4 color = vec4(vTex.x,vTex.y,vTex.z,1.0);
    //vec4 grey = vec4(0.7,0.7,0.7,1.0);
    //vec3 corn = abs(abs(vPos - floor(vPos))-0.5);
    
    int or = int(vOrient);
    float r = (or & 1)/1;
    float g = (or & 2)/2;
    float b = (or & 4)/4;
    float a = (or & 8)/8;
    if(a==1)a=0.5;
    else a=1;
    outputColor = vec4(r*a,g*a,b*a,1);
    
}
