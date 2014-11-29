#version 330
#define LIGHT_NUM 4

uniform mat3 NORM_MAT;
uniform mat4 VIEW_MAT;
uniform mat4 PROJ_VIEW_MAT;

layout( location = 0 ) in vec4 inVert;
layout( location = 1 ) in vec3 inNorm;

smooth out vec3 vertEye;
smooth out vec3 vertNorm;

void main() {
	vec4 ev     = VIEW_MAT * inVert;
	vertEye     = ev.xyz / ev.w;
	vertNorm    = normalize( NORM_MAT * inNorm );
	gl_Position = PROJ_VIEW_MAT * inVert;
}


