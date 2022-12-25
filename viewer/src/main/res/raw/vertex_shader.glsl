attribute vec4 vPosition;
attribute lowp vec3 vNormal;

uniform mat4 u_Matrix;
uniform mat4 uMVMatrix;

attribute vec2 vTexCoord;

varying vec2 texCoord;
varying lowp vec3 frag_Normal;
varying lowp vec3 frag_Position;

void main(){

    frag_Normal = (uMVMatrix * vec4(vNormal, 0.0)).xyz;
    frag_Position = (uMVMatrix * vPosition).xyz;

    gl_Position = u_Matrix * vPosition;
    texCoord = vTexCoord;
}