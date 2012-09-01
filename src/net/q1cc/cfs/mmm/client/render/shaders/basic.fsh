#version 130

smooth in float vLight;
smooth in vec3 vTex;
smooth in vec3 vPos;

out vec4 outputColor;

void main () {
    vec4 color = vec4(vTex.x,vTex.y,vTex.z,1.0);
    vec4 grey = vec4(0.7,0.7,0.7,1.0);
    vec3 corn = abs(abs(vPos - floor(vPos))-0.5);
    //float a=0;
    //if(corn.x>0.9 || corn.y>0.9 || corn.z>0.9
    //    || corn.x<0.1 || corn.y<0.1 || corn.z<0.1){
    //    a=1;
    //}
    //outputColor = vec4(corn,1);
    if( corn.x>0.48 ) {
        outputColor = grey;
    } else {
        outputColor = color;
    }

    //outputColor = mix(color,grey,a);
}
