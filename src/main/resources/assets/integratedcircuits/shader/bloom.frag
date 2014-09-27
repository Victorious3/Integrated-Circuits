#version 120

uniform float time;

void main() 
{
   float brightness = sin(time / 10.0) * 0.5 + 0.5;
   gl_FragColor = vec4(brightness, 0, 0, 0.5);
}