#version 330
#define TOL (0.0001)

#define SEG_COUNT 10
#define MAX_VERTS 40
#define ENABLE_FOG 1
#define GLOW_TIME 0.35

layout( points ) in;
layout( triangle_strip, max_vertices=MAX_VERTS ) out;

uniform vec4 VIEWPORT;
uniform mat4 PROJ_VIEW_MAT;
uniform mat4 VIEW_MAT;
uniform float LINE_WIDTH;
uniform vec4 GLOW_COLOR;
uniform float FOG_DENSITY;
uniform float FOG_START;
uniform vec4 FOG_COLOR;

layout( std140 ) uniform FOG {
	vec4 PARAMS; // start + density
	vec4 COLOR;
} FOG;

in EdgeData {
	vec3 vertA;
	vec3 vertB;
	vec3 axis;
	vec4 params;
	vec4 color;
} data[];

smooth out vec4 color;
smooth out vec2 tex0;

struct Vert {
	vec4 pos;
	vec4 color;
};


vec4 applyFog( vec4 eyeVert, vec4 color ) {
	float fogCoord = length( eyeVert.xyz ) / eyeVert.w;
	float fogFactor = exp( -FOG.PARAMS.y * ( fogCoord - FOG.PARAMS.x ) );
	fogFactor = clamp( fogFactor, 0.0, 1.0 );
	return mix( FOG.COLOR, color, fogFactor );
}

Vert computeVert( float t, float stopTime, vec3 start, mat3 basis, vec3 shape, vec4 color ) {
	Vert ret;

	// Parabola
	float qt = 2.0 * t * ( 1.0 - t );
	// Smoothstep
	float st = t * t * ( 3.0 - 2.0 * t );
	// Compute position based on curve mixing.
	mat3 shapeMat = mat3( t, t, 0.0, st, t, 0.0, st, st, qt );
	vec4 modelPos = vec4( start + basis * shapeMat * shape, 1.0 );
	ret.pos = PROJ_VIEW_MAT * modelPos;

	#ifdef GLOW_TIME
		float glowFactor = min( ( stopTime - t ) / GLOW_TIME, 1.0 );
		ret.color = mix( GLOW_COLOR, color, glowFactor );
	#else
		ret.color = color;
	#endif

	#if ENABLE_FOG
		ret.color = applyFog( VIEW_MAT * modelPos, ret.color );
	#endif

	return ret;
}


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
		float tt = ( a.z - a.w ) / den;
		if( den > 0.0 ) {
			tMin = max( tMin, tt );
		} else {
			tMax = min( tMax, tt );
		}
	}

	if( tMin >= tMax ) {
		return false;
	}

	b = tMax * delta + a;
	a = tMin * delta + a;
	return true;
}


void main() {
	// Start and stop parameters (time), which 
	// indicate precisely what portion of the curve to render.
	#ifdef GLOW_TIME
		float timeStart = data[0].params[0] / ( 1.0 - GLOW_TIME );
		float timeStop  = data[0].params[1] / ( 1.0 - GLOW_TIME );
	#else
		float timeStart = data[0].params[0];
		float timeStop  = data[0].params[1];
	#endif
	
	float timeDelta = min( 1.0, timeStop ) - timeStart;	
	vec3 posDelta = data[0].vertB - data[0].vertA;
	
	// Compute factors for curve shape. 
	// shapeFactor.x = linear fraction of curve
	// shapeFactor.y = polynomial sigmoid fraction of curve
	// shapeFactor.z = polynomial arc fraction of curve.
	vec3 shape = vec3( data[0].params.zw, 0.0 );
	shape = vec3( (1.0 - shape.x) * ( 1.0 - shape.y ), 
				  (1.0 - shape.y) * shape.x,
				  shape.y );

	// Compute basis vectors for curve. 
	// shapeBasis[0] = component perpendicular to user-defined axis
	// shapeBasis[1] = component parallel to user-defined axis
	// shapeBasis[2] = user-defined axis scaled by length of arc.
	mat3 shapeBasis;
	shapeBasis[1] = data[0].axis * dot( posDelta, data[0].axis );
	shapeBasis[0] = posDelta - shapeBasis[1];
	shapeBasis[2] = data[0].axis * length( posDelta );
	
	float ta = timeStart;	
	Vert a = computeVert( timeStart, timeStop, data[0].vertA, shapeBasis, shape, data[0].color );

	// Break up curve into [segCount] segments.
	for( int i = 1; i <= SEG_COUNT; i++ ) {
		float tb = float( i ) / float( SEG_COUNT ) * timeDelta + timeStart;
		Vert b = computeVert( tb, timeStop, data[0].vertA, shapeBasis, shape, data[0].color );
		vec4 clipA = a.pos;
		vec4 clipB = b.pos;
		vec4 colA = a.color;
		vec4 colB = b.color;
		
		if( clipLineToVisibleDepth( clipA, clipB ) ) {
			// Note that multiplying the NDU coordinates (-1 to 1) by viewport 
			// will give us domain of (-w, -h, w, h ), NOT ( 0, 0, w, h ).
			vec4 scaleA  = vec4( VIEWPORT.zw, 1.0, 1.0 ) / clipA.w;
			vec4 scaleB  = vec4( VIEWPORT.zw, 1.0, 1.0 ) / clipB.w;
			vec4 screenA = scaleA * clipA;
			vec4 screenB = scaleB * clipB;
			// Because our screen domain is twice is big, we're offset 
			// the line by 'LINE_WIDTH' instead of '0.5 * LINE_WIDTH'.
			vec4 dy = LINE_WIDTH * normalize( vec4( screenB.y - screenA.y, screenA.x - screenB.x, 0.0, 0.0 ) );

			tex0 = vec2( ta, 0.0 );
			color = colA;
			gl_Position = clipA - dy / scaleA;
			EmitVertex();
			tex0 = vec2( ta, 1.0 );
			color = colA;
			gl_Position = clipA + dy / scaleA;
			EmitVertex();
			tex0 = vec2( tb, 0.0 );
			color = colB;
			gl_Position = clipB - dy / scaleB;
			EmitVertex();
			tex0 = vec2( tb, 1.0 );
			color = colB;
			gl_Position = clipB + dy / scaleB;
			EmitVertex();
			EndPrimitive();
		}

		a = b;
		ta = tb;
	}
}

