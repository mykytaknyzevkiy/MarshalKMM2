attribute vec4 vPosition;
uniform mat4 u_Matrix;
attribute vec2 vTexCoord;
varying vec2 texCoord;

void main(){

    gl_Position = u_Matrix * vPosition;
    texCoord = vTexCoord;
}