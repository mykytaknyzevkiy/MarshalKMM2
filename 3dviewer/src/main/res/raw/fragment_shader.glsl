precision lowp float;
uniform sampler2D texSampler;
varying vec2 texCoord;
uniform float Opacity;
uniform float isTexture;
uniform vec4 uColor;

varying lowp vec3 frag_Normal;
varying lowp vec3 frag_Position;

struct Light {
    vec3 Color;
    float AmbientIntensity;
    float DiffuseIntensity;
    vec3 Direction;
    float SpecularIntensity;
    float Shininess;
};

uniform Light u_Light;

 void main(){

      // Ambient
          lowp vec3 AmbientColor = u_Light.Color * u_Light.AmbientIntensity;

          // Diffuse
          lowp vec3 Normal = normalize(frag_Normal);
          lowp float DiffuseFactor = max(-dot(Normal, u_Light.Direction), 0.0);
          lowp vec3 DiffuseColor = u_Light.Color * u_Light.DiffuseIntensity * DiffuseFactor;

          // Specular
          lowp vec3 Eye = normalize(frag_Position);
          lowp vec3 Reflection = reflect(u_Light.Direction, Normal);
          lowp float SpecularFactor = pow(max(0.0, -dot(Reflection, Eye)), u_Light.Shininess);
          lowp vec3 SpecularColor = u_Light.Color * u_Light.SpecularIntensity * SpecularFactor;

      gl_FragColor = (isTexture)*texture2D(texSampler, texCoord)* vec4((AmbientColor + DiffuseColor + SpecularColor), 1.0)* Opacity + (1.0 - isTexture)*uColor;
 }