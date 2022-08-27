
Function sky_CreateSky(filename$,parent%=0)
	Local sky

	Restore sky_SkyboxData
	sky = CreateMesh(parent)
	For face = 1 To 6
		Read direction$
		Local fname$ = filename$ + direction$ + ".jpg"
		If FileType(fname$)=1
			Local b = LoadBrush_Strict(fname$,%110001)
			s = CreateSurface(sky,b)
			For vert = 1 To 4
				Read x,y,z,u,v
				AddVertex s,x,y,z,u,v
			Next
			AddTriangle s,0,1,2
			AddTriangle s,0,2,3
			FreeBrush b
		EndIf
	Next
	FlipMesh sky
	EntityFX sky,1+8
	EntityOrder sky,1000
	Return sky
End Function

Function UpdateSky()
	PositionEntity Sky, EntityX(Camera),EntityY(Camera),EntityZ(Camera), True
End Function

Function Update1499Sky()
	PositionEntity NTF_1499Sky, EntityX(Camera),EntityY(Camera),EntityZ(Camera), True
End Function


;-----------------------------------------------------------------------
;Data
;-----------------------------------------------------------------------

.sky_SkyboxData
Data "_back"
Data -1,+1,-1,0,0
Data +1,+1,-1,1,0
Data +1,-1,-1,1,1
Data -1,-1,-1,0,1
Data "_left"
Data +1,+1,-1,0,0
Data +1,+1,+1,1,0
Data +1,-1,+1,1,1
Data +1,-1,-1,0,1
Data "_front"
Data +1,+1,+1,0,0
Data -1,+1,+1,1,0
Data -1,-1,+1,1,1
Data +1,-1,+1,0,1
Data "_right"
Data -1,+1,+1,0,0
Data -1,+1,-1,1,0
Data -1,-1,-1,1,1
Data -1,-1,+1,0,1
Data "_up"
Data -1,+1,+1,0,0
Data +1,+1,+1,1,0
Data +1,+1,-1,1,1
Data -1,+1,-1,0,1
Data "_down"
Data -1,-1,-1,1,0
Data +1,-1,-1,1,1
Data +1,-1,+1,0,1
Data -1,-1,+1,0,0
;~IDEal Editor Parameters:
;~F#1#1B#1F
;~C#Blitz3D