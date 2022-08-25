Global XE_XF,XE_MAXtextures

Type XE_texdata
	Field idx,h,fn$
End Type
Function writemesh(mesh,e_filename$)
        ;Change to 0 for human readable but (((bigger))) output
        trunc=1

	;reset texture list
	XE_MAXtextures=0
	For td.xe_texdata =Each XE_texdata:Delete td:Next
	
	;open output file
	filenameout$=e_filename$
	XE_XF=WriteFile (filenameout$);
	
	;File version 
	WriteLine XE_XF,"xof 0302txt 0064"
	WriteLine XE_XF,""
	;Header 
	WriteLine XE_XF,"Header {"
	WriteLine XE_XF,"1;"
	WriteLine XE_XF,"0;"
	WriteLine XE_XF,"1;"
	WriteLine XE_XF,"}"
	WriteLine XE_XF,""
	
	WriteLine XE_XF,"//Exported By D-Mapper Registered to "+regname$

	;Write all materials in meshes

	XE_MAXtextures=0

	RecursiveAddMaterial(mesh,forcematerial$,e_directory)
	WriteLine XE_XF," //Truncatemarker" ; textures cannot be truncated for giles

	;Start of frame root
	WriteLine XE_XF,"Frame root {"
	WriteLine XE_XF,"   FrameTransformMatrix {"
	WriteLine XE_XF,"      1.000000,0.000000,0.000000,0.000000,"
	WriteLine XE_XF,"      0.000000,1.000000,0.000000,0.000000,"
	WriteLine XE_XF,"      0.000000,0.000000,1.000000,0.000000,"
	WriteLine XE_XF,"      0.000000,0.000000,0.000000,1.000000;;"
	WriteLine XE_XF,"   }"

	;chiname$=EntityName(mesh)
	RecursiveAddMesh(mesh)

	WriteLine XE_XF,"} //End of root"
	CloseFile XE_XF

	If trunc
		Rfile=ReadFile(filenameout$)
		Wfile=WriteFile(filenameout$+"temp.x")
		If rfile<>0 And wfile<>0
			headerwrite=1
			While Not Eof(rfile)
				lines=lines+1
				If headerwrite
					l$=ReadLine(rfile)
					WriteLine(wfile,l$)
					If Instr(l$,"//Exported By") headerwrite=0
				Else
					l$=ReadLine(rfile)
					If Instr (l$,"//") Then l$=USV(ReadLine(rfile),1,"//")
					l$=Trim(l$)
					WriteLine wfile,l$
					;dat$=dat$+l$
					;If MilliSecs()>gtime+1000 gtime=MilliSecs():messagebox(-1,0,300,"Truncating ","Collapsing whitespace, end Of Line characters and ","comments, Please Wait..","","Line "+lines+" "+ l$)

				EndIf
			Wend
			CloseFile rfile
			CloseFile wfile
		Else
			
			
		EndIf	
       EndIf
       ;CopyFile filenameout$+"temp",filenameout$

       ;anti NAN patch for X exporter
       fileR=OpenFile (filenameout)
       fileW=OpenFile (tempfilename)
       If fileR While Not Eof(fileR)
           l$=ReadLine(fileR)             
           l$=Replace (l$,"NaN","0.0")
           WriteLine(fileW,l$)
      Wend
      CloseFile fileR
      CloseFile fileW
      CopyFile fileW,fileR
      DeleteFile fileW

	
End Function


Function RecursiveAddMaterial(h,forcematerial$,e_directory$="")
DebugLog CountChildren(h)

	If EntityClass$(h)="Mesh"
		;get neccacaries
		For surfc=1 To CountSurfaces(h)
			surf=GetSurface(h,surfc)
			brush=GetSurfaceBrush(surf)
			tex=GetBrushTexture (brush)
			
			;get rid of tex path & copy textures
			If forcematerial$="" Then
				from$=TextureName(tex)
				XE_XFilen$=strip_path(from)
				CopyFile from,e_directory+XE_XFilen$
				DebugLog "Copying tex to "+e_directory+XE_XFilen$
			Else
				XE_XFilen$=strip_path(forcematerial$)
			EndIf 
	
			;mark possible 'new' texture index
			Tindex=XE_MAXtextures
	
			;see id texture is alreadt in database, if so make tindex match the existing one
			For txs.XE_texdata = Each XE_texdata
				If txs\fn=XE_XFilen$ Then noadd=1:Tindex=txs\idx
			Next
	
			;Save texture data in index with surface handle for an index key
			txs.XE_texdata = New XE_texdata
			txs\h = surf
			txs\idx = Tindex
			txs\fn = XE_XFilen$
			
			;only add texture if a new one is present
			If Not noadd
				WriteLine XE_XF,"Material dx_brush"+Str(Tindex)+" {"
				WriteLine XE_XF,"   1.000000;1.000000;1.000000;1.000000;;"
				WriteLine XE_XF,"   0.000000;"
				WriteLine XE_XF,"   1.000000;1.000000;1.000000;;"
				WriteLine XE_XF,"   0.000000;0.000000;0.000000;;"
				WriteLine XE_XF,"   TextureFilename {"
				WriteLine XE_XF,"      "+Chr(34)+XE_XFilen$+Chr(34)+";"
				WriteLine XE_XF,"   }"
				WriteLine XE_XF,"}"
				XE_MAXtextures=XE_MAXtextures+1
			EndIf 
	
			;do the cleaning
			FreeTexture Tex
			FreeBrush brush
		Next 
	EndIf 

	For cc =CountChildren(h) To 1 Step -1
		chi=GetChild (h,cc)
		RecursiveAddMaterial(chi,forcematerial$)
	Next 

End Function

Function GetStringofMatElement$(mesh,x,y)
	me$=GetMatElement(mesh,x,y)
	dp$=USV(me$,2,".")
	final$=me$
	If Len(dp$)=0
		final$=me$+"000000"
	ElseIf Len(dp$)=1
		final$=me$+"00000"
	ElseIf Len(dp$)=2
		final$=me$+"0000"
	ElseIf Len(dp$)=3
		final$=me$+"000"
	ElseIf Len(dp$)=4
		final$=me$+"00"
	ElseIf Len(dp$)=5
		final$=me$+"0"
	EndIf
	DebugLog "Matrix="+me$+" ("+dp$+") "+final$
	Return final$
End Function

Global recuse_depth
Function RecursiveAddMesh(h,basescaleX#=1,basescaleY#=1,basescaleZ#=1)
		recuse_depth=recuse_depth+1
		
		chiname$=EntityName(h)
		DebugLog "Recursing "+chiname+" Childs="+CountChildren(h)+" Depth="+recuse_depth
		If chiname="" Or Instr(chiname,"NoName") chiname="NoName"+MilliSecs()
		DebugLog chiname
		
;		mesh=CopyEntity(h)
;		EntityParent mesh,0
		mesh=h

		WriteLine XE_XF,"   Frame "+ChiName$+" {"
		WriteLine XE_XF,"      FrameTransformMatrix {"
		WriteLine XE_XF,"         "+GetStringofMatElement(mesh,0,0)+","+GetStringofMatElement(mesh,0,1)+","+GetStringofMatElement(mesh,0,2)+",0.000000,"
		WriteLine XE_XF,"         "+GetStringofMatElement(mesh,1,0)+","+GetStringofMatElement(mesh,1,1)+","+GetStringofMatElement(mesh,1,2)+",0.000000,"
		WriteLine XE_XF,"         "+GetStringofMatElement(mesh,2,0)+","+GetStringofMatElement(mesh,2,1)+","+GetStringofMatElement(mesh,2,2)+",0.000000,"
		WriteLine XE_XF,"         "+GetStringofMatElement(mesh,3,0)+","+GetStringofMatElement(mesh,3,1)+","+GetStringofMatElement(mesh,3,2)+",1.000000;;"
		WriteLine XE_XF,"   }"

		
		If EntityClass$(mesh)="Mesh"
			For surfc=1 To CountSurfaces (mesh)
				surf=GetSurface(mesh,surfc)
				verts=CountVertices(surf)
				tris=CountTriangles(surf)
				;meshinfo
				WriteLine XE_XF,"      Mesh "+ChiName$+" {"
	
	
				;Number of verts;
				WriteLine XE_XF,"         "+verts+";"
	
	
				;X;Y;Z; of verts;
				For v=0 To verts-1
					If v=verts-1 Then term$=";;" Else term$=";,"
					WriteLine XE_XF,"         "+VertexX (surf,v)+";"+VertexY (surf,v)+";"+VertexZ (surf,v)+term$
				Next
	
				
				;No of tris;
				WriteLine XE_XF,"         "+tris+";"
	
	
				;Tri ordering 3;t0,t1,t2;,
				For t=0 To Tris-1
					If t=tris-1 Then term$=";;" Else term$=";,"
					WriteLine XE_XF,"         3;"+TriangleVertex(Surf,t,0)+","+TriangleVertex(Surf,t,1)+","+TriangleVertex(Surf,t,2)+term$
				Next 
	
				;Material List
				WriteLine XE_XF,"         MeshMaterialList {"
				WriteLine XE_XF,"            1;";num of materials for mesh
				WriteLine XE_XF,"            "+tris+";";number of faces/tris
				For t=0 To tris-1
					If t=tris-1 Then term$=";;" Else term$=","
					WriteLine XE_XF,"            0"+term$ ; face indexes?
				Next 
				WriteLine XE_XF,"            {dx_brush"+LookUpTindex(surf)+"}"
				WriteLine XE_XF,"         } // end of material list"
	
				;Normals List
				WriteLine XE_XF,"         MeshNormals {"
				WriteLine XE_XF,"            "+verts+";"
				For v=0 To verts-1
					If v=verts-1 Then term$=";;" Else term$=";,"
					WriteLine XE_XF,"            "+VertexNX (surf,v)+";"+VertexNY (surf,v)+";"+VertexNZ (surf,v)+term$
				Next
				WriteLine XE_XF,"            "+tris+";"
				For t=0 To Tris-1
					If t=tris-1 Then term$=";;" Else term$=";,"
					WriteLine XE_XF,"            3;"+TriangleVertex(Surf,t,0)+","+TriangleVertex(Surf,t,1)+","+TriangleVertex(Surf,t,2)+term$
				Next 
				WriteLine XE_XF,"         } // end of normal list"
	
				;Texturecoords List
				WriteLine XE_XF,"         MeshTextureCoords {"
				WriteLine XE_XF,"            "+verts+";"
				For v=0 To verts-1
					If v=verts-1 Then term$=";;" Else term$=";,"
					WriteLine XE_XF,"            "+VertexU (surf,v)+";"+VertexV (surf,v)+term$
				Next
				WriteLine XE_XF,"         } // end of Texturecoord list"
	
				;Vertexcolor List
				WriteLine XE_XF,"         MeshVertexColors {"
				WriteLine XE_XF,"            "+verts+";"
				For v=0 To verts-1
					If v=verts-1 Then term$=";;" Else term$=";,"
					vred#=VertexRed (surf,v)/256
					vgreen#=VertexGreen (surf,v)/256
					vblue#=VertexBlue (surf,v)/256
					WriteLine XE_XF,"            "+v+";"+vred+";"+vgreen+";"+vblue+";"+VertexAlpha (surf,v)+term$
				Next
				WriteLine XE_XF,"         } // end of Vertexcolor list"
	
				;End of mesh
				WriteLine XE_XF,"      } // end of mesh block for "+ChiName$
	
			Next	 
		EndIf

		;FreeEntity mesh
		
		;do all childs.. of childs etc
		DebugLog "has "+CountChildren(h)
		For cc=CountChildren(h) To 1 Step -1
			chi=GetChild (h,cc)
			RecursiveAddMesh(chi,basescaleX#,basescaleY#,basescaleZ#)
		Next 


		;close branch frame
		WriteLine XE_XF,"   } // End of frame"+ChiName$
		DebugLog "recuse_depth Depth "+recuse_depth


	recuse_depth=recuse_depth-1

End Function

Function LookUpTindex(surf)
	;look up tindex from surface handle
	For t.XE_texdata = Each XE_texdata
		If t\h=surf Return t\idx
	Next 
End Function

Function strip_path$(f$)
	 	f$=Lower$(f$) ; Full (!) Texture Path
		lastknown=0
		For p=1 To Len (f$)
			If Instr(f$,"\",p) Then lastknown=lastknown+1
		Next
		fnl=Len(f$)-lastknown
		f$=Right(f$,fnl)
 		;DebugLog "filename stripped"+ f$

		Return f$
End Function

;'user separated values

Function USV$(in$,which%=1,sep$=",")

;''pipe seprated values

	Local n% = 1

	Local offset% = 0

	Local nextoffset% = 1

	Local ValueRet$ =""

	

	While offset<Len(in$)

		nextoffset = Instr(in$,sep$,offset+1)

		If nextoffset = 0

			nextoffset = Len(in$)+1

			which = n

		End If

		ValueRet$ = Mid$(in$,offset+1,nextoffset-offset-1)

		If which = n	

			Return ValueRet	

		End If

		offset = nextoffset

		n=n+1

	Wend



	Return n-1



End Function

;RMESH STUFF;;;;

Type Materials
	Field name$
	Field Diff
	
	Field StepSound%
End Type

Function StripFilename$(file$)
	Local mi$=""
	Local lastSlash%=0
	If Len(file)>0
		For i%=1 To Len(file)
			mi=Mid(file$,i,1)
			If mi="\" Or mi="/" Then
				lastSlash=i
			EndIf
		Next
	EndIf
	
	Return Left(file,lastSlash)
End Function

Function StripPath$(file$) 
	Local name$=""
	If Len(file$)>0 
		For i=Len(file$) To 1 Step -1 
			
			mi$=Mid$(file$,i,1) 
			If mi$="\" Or mi$="/" Then Return name$
			
			name$=mi$+name$ 
		Next 
		
	EndIf 
	
	Return name$ 
End Function

Function Piece$(s$,entry,char$=" ")
	While Instr(s,char+char)
		s=Replace(s,char+char,char)
	Wend
	For n=1 To entry-1
		p=Instr(s,char)
		s=Right(s,Len(s)-p)
	Next
	p=Instr(s,char)
	If p<1
		a$=s
	Else
		a=Left(s,p-1)
	EndIf
	Return a
End Function

Function KeyValue$(entity,key$,defaultvalue$="")
	properties$=EntityName(entity)
	properties$=Replace(properties$,Chr(13),"")
	key$=Lower(key)
	Repeat
		p=Instr(properties,Chr(10))
		If p Then test$=(Left(properties,p-1)) Else test=properties
		testkey$=Piece(test,1,"=")
		testkey=Trim(testkey)
		testkey=Replace(testkey,Chr(34),"")
		testkey=Lower(testkey)
		If testkey=key Then
			value$=Piece(test,2,"=")
			value$=Trim(value$)
			value$=Replace(value$,Chr(34),"")
			Return value
		EndIf
		If Not p Then Return defaultvalue$
		properties=Right(properties,Len(properties)-p)
	Forever 
End Function

Function GetTextureFromCache%(name$)
	For tc.Materials=Each Materials
		If tc\name = name Then Return tc\Diff
	Next
	Return 0
End Function

;Function GetBumpFromCache%(name$)
;	For tc.Materials=Each Materials
;		If tc\name = name Then Return tc\Bump
;	Next
;	Return 0
;End Function

Function GetCache.Materials(name$)
	For tc.Materials=Each Materials
		If tc\name = name Then Return tc
	Next
	Return Null
End Function

Function AddTextureToCache(texture%)
	Local tc.Materials=GetCache(StripPath(TextureName(texture)))
	If tc.Materials=Null Then
		tc.Materials=New Materials
		tc\name=StripPath(TextureName(texture))
		;Local temp$=GetINIString("Data\materials.ini",tc\name,"bump")
		;If temp<>"" Then
		;	tc\Bump=LoadTexture_Strict(temp)
		;	TextureBlend tc\Bump,FE_BUMP
		;Else
		;	tc\Bump=0
		;EndIf
		tc\Diff=0
	EndIf
	If tc\Diff=0 Then tc\Diff=texture
End Function

Function ClearTextureCache()
	For tc.Materials=Each Materials
		If tc\Diff<>0 Then FreeTexture tc\Diff
		;If tc\Bump<>0 Then FreeTexture tc\Bump
		Delete tc
	Next
End Function

Function FreeTextureCache()
	For tc.Materials=Each Materials
		If tc\Diff<>0 Then FreeTexture tc\Diff
		;If tc\Bump<>0 Then FreeTexture tc\Bump
		tc\Diff = 0; : tc\Bump = 0
	Next
End Function


Function LoadRMesh(file$)
	;generate a texture made of white
	Local blankTexture%
	blankTexture=CreateTexture(4,4,1,1)
	ClsColor 255,255,255
	SetBuffer TextureBuffer(blankTexture)
	Cls
	SetBuffer BackBuffer()
	
	Local pinkTexture%
	pinkTexture=CreateTexture(4,4,1,1)
	ClsColor 255,255,255
	SetBuffer TextureBuffer(pinkTexture)
	Cls
	SetBuffer BackBuffer()
	
	ClsColor 0,0,0
	
	;read the file
	Local f%=ReadFile(file)
	Local i%,j%,k%,x#,y#,z#,yaw#
	Local vertex%
	Local temp1i%,temp2i%,temp3i%
	Local temp1#,temp2#,temp3#
	Local temp1s$, temp2s$
	
	Local collisionMeshes% = CreatePivot()
	
	Local hasTriggerBox% = False
	
	For i=0 To 3 ;reattempt up to 3 times
		If f=0 Then
			f=ReadFile(file)
		Else
			Exit
		EndIf
	Next
	If f=0 Then RuntimeError "Error reading file "+Chr(34)+file+Chr(34)
	Local isRMesh$ = ReadString(f)
	If isRMesh$="RoomMesh"
		;Continue
	ElseIf isRMesh$="RoomMesh.HasTriggerBox"
		hasTriggerBox% = True
	Else
		RuntimeError Chr(34)+file+Chr(34)+" is Not RMESH ("+isRMesh+")"
	EndIf
	
	file=StripFilename(file)
	
	Local count%,count2%
	
	;drawn meshes
	Local Opaque%,Alpha%
	
	Opaque=CreateMesh()
	Alpha=CreateMesh()
	
	count = ReadInt(f)
	Local childMesh%
	Local surf%,tex%[2],brush%
	
	Local isAlpha%
	
	Local u#,v#
	
	For i=1 To count ;drawn mesh
		childMesh=CreateMesh()
		
		surf=CreateSurface(childMesh)
		
		brush=CreateBrush()
		
		tex[0]=0 : tex[1]=0
		
		isAlpha=0
		For j=0 To 1
			temp1i=ReadByte(f)
			If temp1i<>0 Then
				temp1s=ReadString(f)
				tex[j]=GetTextureFromCache(temp1s)
				If tex[j]=0 Then ;texture is not in cache
					Select True
						Case temp1i<3
							tex[j]=LoadTexture(file+temp1s,1)
						Default
							tex[j]=LoadTexture(file+temp1s,3)
					End Select
					
					If tex[j]<>0 Then
						If temp1i=1 Then TextureBlend tex[j],5
						;If Instr(Lower(temp1s),"_lm")<>0 Then
						;	TextureBlend tex[j],3
						;EndIf
						AddTextureToCache(tex[j])
					EndIf
					
				EndIf
				If tex[j]<>0 Then
					isAlpha=2
					If temp1i=3 Then isAlpha=1
					
					TextureCoords tex[j],1-j
				EndIf
			EndIf
		Next
		
		If isAlpha=1 Then
			If tex[1]<>0 Then
				TextureBlend tex[1],2
				BrushTexture brush,tex[1],0,0
			Else
				BrushTexture brush,blankTexture,0,0
			EndIf
		Else
			

			For j=0 To 1
				If tex[j]<>0 Then
					BrushTexture brush,tex[j],0,j+1
				Else
					BrushTexture brush,blankTexture,0,j+1
				EndIf
			Next

		EndIf
		
		surf=CreateSurface(childMesh)
		
		If isAlpha>0 Then PaintSurface surf,brush
		
		FreeBrush brush : brush = 0
		
		count2=ReadInt(f) ;vertices
		
		For j%=1 To count2
			;world coords
			x=ReadFloat(f) : y=ReadFloat(f) : z=ReadFloat(f)
			vertex=AddVertex(surf,x,y,z)
			
			;texture coords
			For k%=0 To 1
				u=ReadFloat(f) : v=ReadFloat(f)
				VertexTexCoords surf,vertex,u,v,0.0,k
			Next
			
			;colors
			temp1i=ReadByte(f)
			temp2i=ReadByte(f)
			temp3i=ReadByte(f)
			VertexColor surf,vertex,temp1i,temp2i,temp3i,1.0
		Next
		
		count2=ReadInt(f) ;polys
		For j%=1 To count2
			temp1i = ReadInt(f) : temp2i = ReadInt(f) : temp3i = ReadInt(f)
			AddTriangle(surf,temp1i,temp2i,temp3i)
		Next
		
		If isAlpha=1 Then
			AddMesh childMesh,Alpha
			EntityAlpha childMesh,0.0
		Else
			AddMesh childMesh,Opaque
			EntityParent childMesh,collisionMeshes
			EntityAlpha childMesh,0.0
			EntityType childMesh,HIT_MAP
			EntityPickMode childMesh,2
			
			;make collision double-sided
			Local flipChild% = CopyMesh(childMesh)
			FlipMesh(flipChild)
			AddMesh flipChild,childMesh
			FreeEntity flipChild			
		EndIf
		
		
	Next
	
	Local obj%
	
	temp1i=CopyMesh(Alpha)
	FlipMesh temp1i
	AddMesh temp1i,Alpha
	FreeEntity temp1i
	
	If brush <> 0 Then FreeBrush brush
	
	AddMesh Alpha,Opaque
	FreeEntity Alpha
	
	EntityFX Opaque,3
	
	EntityAlpha Opaque,1.0

	FreeTexture blankTexture
	
	CloseFile f
	
	Return Opaque
End Function

Print "RMesh Viewer"
Print "Enter RMesh path and name with extension"
Print "Example: GFX/map/4tunnels.rmesh"
Print ""
Local fname$ = Input("RMesh To load: ")

Graphics3D 1280,720,32,2

;Global bump% = LoadTexture("GFX/map/tilebump.png")
;TextureBlend bump,6
;TextureBumpEnvMat bump,0,0,1.009
;TextureBumpEnvMat bump,0,1,-1.000
;TextureBumpEnvMat bump,1,0,-1.000
;TextureBumpEnvMat bump,1,1,1.009
;TextureBumpEnvScale bump,1.0
;TextureBumpEnvOffset bump,0.0

Global mesh% = LoadRMesh(fname)

;white% = LoadTexture("GFX/map/wood.png")
;TextureCoords white,1
;EntityTexture mesh,bump,0,0
;FreeTexture bump
;EntityTexture mesh,white,0,2
ScaleEntity mesh,0.1,0.1,0.1
EntityPickMode mesh,2
ShowEntity mesh

Global sphere% = CreateSphere()
ScaleEntity sphere,5,5,5
EntityColor sphere,255,0,0

Global Camera% = CreateCamera()
TranslateEntity Camera,0,5,0
Global camPitch# = 0 : Global camYaw# = 0

HidePointer 

Global BumpEnvMat# = 0.0075

While Not KeyHit(1)
	;mouselook
	Local diffX% = MouseX()-(GraphicsWidth()/2)
	Local diffY% = MouseY()-(GraphicsHeight()/2)
	MoveMouse GraphicsWidth()/2,GraphicsHeight()/2
	camYaw = camYaw - (Float(diffX)/30.0)
	While camYaw>=360.0
		camYaw=camYaw-360.0
	Wend
	While camYaw<0.0
		camYaw=camYaw+360.0
	Wend
	camPitch = camPitch + (Float(diffY)/30.0)
	If camPitch>90.0
		camPitch = 90.0
	EndIf
	If camPitch<-90.0
		camPitch = -90.0
	EndIf
	RotateEntity Camera,camPitch,camYaw,0.0,True
	
	If KeyDown(17) Then MoveEntity Camera,0,0,1+KeyDown(42)*2
	
	If KeyDown(30) Then MoveEntity Camera,-1-KeyDown(42)*2,0,0
	If KeyDown(32) Then MoveEntity Camera,1+KeyDown(42)*2,0,0
	
	If KeyDown(31) Then MoveEntity Camera,0,0,-1-KeyDown(42)*2
	
	If KeyDown(57) Then TranslateEntity Camera,0,4+KeyDown(42)*8,0
	
	SetBuffer BackBuffer()
	
	CameraPick Camera,GraphicsWidth()/2,GraphicsHeight()/2
	PositionEntity sphere,PickedX(),PickedY(),PickedZ(),True
	Local dist# = 2.5;EntityDistance(Camera,sphere)*0.01
	ScaleEntity sphere,dist,dist,dist
	
	RenderWorld
	
	Color 0,0,0
	Text 6,6,"Picked X = "+Str(PickedX()*10.0)
	Text 6,26,"Picked Y = "+Str(PickedY()*10.0)
	Text 6,46,"Picked Z = "+Str(PickedZ()*10.0)
	Color 0,255,0
	Text 5,5,"Picked X = "+Str(PickedX()*10.0)
	Text 5,25,"Picked Y = "+Str(PickedY()*10.0)
	Text 5,45,"Picked Z = "+Str(PickedZ()*10.0)
	
	If KeyHit(44) Then
		BumpEnvMat = -BumpEnvMat
	EndIf
	
	If KeyHit(45) Then
		BumpEnvMat = BumpEnvMat/2
	EndIf
	If KeyHit(46) Then
		BumpEnvMat = BumpEnvMat*2
	EndIf
	
	If KeyHit(47) Then
		BumpEnvMat = BumpEnvMat/3
	EndIf
	If KeyHit(48) Then
		BumpEnvMat = BumpEnvMat*3
	EndIf
	
	;TextureBumpEnvMat bump,0,0,-BumpEnvMat
	;TextureBumpEnvMat bump,0,1,-BumpEnvMat
	;TextureBumpEnvMat bump,1,0,BumpEnvMat
	;TextureBumpEnvMat bump,1,1,BumpEnvMat
	;TextureBumpEnvOffset bump,0.5
	
	Flip
Wend