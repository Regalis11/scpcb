Graphics 640,480,0,2
SetBuffer BackBuffer()

Cls

Color 255,255,255
Text 0,0,"This program shows your CPU details (it was previously for the debug hud)."
Color 255,0,0
Text 0,20,"Warning: This program doesn't work for all users,"
Text 0,40,"that's why it was removed in the original game's exe."
Color 255,255,255
Text 0,80,"Press any key to continue."

Flip

WaitKey()

Global kCPUid$, kCPUfamily%, kCPUsteppingId%, kCPUbrand$, kCPUextendedId$, kCPUfeatures$

kCPUid$         = CPUid$()
kCPUfamily%     = CPUfamily%()
kCPUsteppingId% = CPUsteppingId%()
kCPUbrand$      = CPUbrand$()
kCPUextendedId$ = CPUextendedId$()
kCPUfeatures$   = CPUfeatures$()

Repeat
	Cls
	Color 255,255,255
	Text 0,0,LSet("CPU ID: ",18)+kCPUid
	Text 0,20,LSet("CPU family: ",18)+kCPUfamily
	Text 0,40,LSet("CPU stepping ID: ",18)+kCPUsteppingId
	Text 0,60,LSet("CPU brand: ",18)+kCPUbrand
	Text 0,80,LSet("CPU name: ",18)+kCPUextendedId
	Text 0,100,LSet("CPU features: ",18)+kCPUfeatures
	Text 0,140,"Press any key to close program."
	Flip
	Delay 8
Until WaitKey()
End
;~IDEal Editor Parameters:
;~C#Blitz3D