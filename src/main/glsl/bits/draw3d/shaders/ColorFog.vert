#version 330

uniform mat4 VIEW_MAT;
uniform mat4 PROJ_VIEW_MAT;

layout( std140 ) uniform FOG {
	vec4 COLOR;
	vec4 PARAMS; // (density, startDist)
} FOG;

layout( location = 0 ) in vec4 inVert;
layout( location = 1 ) in vec4 inColor;

smooth out vec4 color;

vec4 applyFog( vec4 eyeVert, vec4 color ) {
	float fogCoord = length( eyeVert.xyz ) / eyeVert.w;
	float fogFactor = exp( -FOG.PARAMS.x * ( fogCoord - FOG.PARAMS.y ) );
	fogFactor = clamp( fogFactor, 0.0, 1.0 );
	return mix( FOG.COLOR, color, fogFactor );
}

void main() {
	gl_Position = PROJ_VIEW_MAT * inVert;
	color = applyFog( VIEW_MAT * inVert, inColor );
}

