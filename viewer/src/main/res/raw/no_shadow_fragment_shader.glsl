precision lowp float;
uniform sampler2D texSampler;
varying vec2 texCoord;
uniform float Opacity;
uniform float isTexture;
uniform vec4 uColor;

 void main(){
      gl_FragColor = (isTexture)*texture2D(texSampler, texCoord)* Opacity + (1.0 - isTexture)*uColor;
 }