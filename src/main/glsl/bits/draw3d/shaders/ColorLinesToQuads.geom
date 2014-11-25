#version 330
#define TOL (0.0001)

layout (lines) in;
layout (triangle_strip, max_vertices = 4) out;

uniform float LINE_WIDTH;
uniform vec4 VIEWPORT;

in VertData {
	vec4 color;
} data[];

smooth out vec4 color;



bool clipLineToVisibleDepth( inout vec4 a, inout vec4 b ) {
	// Compute intersection with near and far planes.
	float tMin  = 0.0;
	float tMax  = 1.0;
	vec4  delta = b - a;
	float den   = delta.z + delta.w;
	if( abs( den ) > TOL ) {
		float t = -( a.z + a.w ) / den;
		if( den > 0.0 ) {
			tMin = max( tMin, t );
		} else {
			tMax = min( tMax, t );
		}
	}

	den = delta.w - delta.z;
	if( abs( den ) > TOL ) {
		float t = ( a.z - a.w ) / den;
		if( den > 0.0 ) {
			tMin = max( tMin, t );
		} else {
			tMax = min( tMax, t );
		}
	}

	if( tMin >= tMax ) {
		return false;
	}

	b = a + tMax * delta;
	a = a + tMin * delta;
	return true;
}


void main() {
	vec4 clipA = gl_in[0].gl_Position;
	vec4 clipB = gl_in[1].gl_Position;
	
	if( !clipLineToVisibleDepth( clipA, clipB ) ) {
		return;
	}
	
	// Note that multiplying the NDU coordinates (-1 to 1) by viewport 
	// will give us domain of (-w, -h, w, h ), NOT ( 0, 0, w, h ).
	vec4 scaleA  = vec4( VIEWPORT.zw, 1.0, 1.0 ) / clipA.w;
	vec4 scaleB  = vec4( VIEWPORT.zw, 1.0, 1.0 ) / clipB.w;
	vec4 screenA = scaleA * clipA;
	vec4 screenB = scaleB * clipB;
	// Because our screen domain is twice is big, we're offset 
	// the line by 'LINE_WIDTH' instead of '0.5 * LINE_WIDTH'.
	vec4 dy = LINE_WIDTH * normalize( vec4( screenB.y - screenA.y, screenA.x - screenB.x, 0.0, 0.0 ) );

	color = data[0].color;
	gl_Position = clipA - dy / scaleA;
	EmitVertex();
	color = data[0].color;
	gl_Position = clipA + dy / scaleA;
	EmitVertex();
	color = data[0].color;
	gl_Position = clipB - dy / scaleB;
	EmitVertex();
	color = data[0].color;
	gl_Position = clipB + dy / scaleB;
	EmitVertex();
	EndPrimitive();
}
