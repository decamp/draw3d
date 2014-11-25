#version 330

layout( location = 0 ) in vec3 vertA;
layout( location = 1 ) in vec3 vertB;
layout( location = 2 ) in vec3 axis;
layout( location = 3 ) in vec4 params;
layout( location = 4 ) in vec4 color;

out EdgeData {
	vec3 vertA;
	vec3 vertB;
	vec3 axis;
	vec4 params;
	vec4 color;
} data;

void main() {
	data.vertA  = vertA;
	data.vertB  = vertB;
	data.axis   = axis;
	data.params = params;
	data.color  = color;
}
