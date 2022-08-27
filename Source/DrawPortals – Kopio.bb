Type DrawPortal
	;Field x#,y#,z#
	Field w#,h#
	;Field pitch#,yaw#,roll#
	Field cam%,portal%
	Field camx#,camy#,camz#
	Field camZoom#
	Field campitch#,camyaw#,camroll#
	Field tex%,brush%,surface%
	Field texw%,texh%
	Field id%
End Type

Function CreateDrawPortal.DrawPortal(x#,y#,z#,pitch#,yaw#,roll#,w#,h#,camx#=0.0,camy#=0.0,camz#=0.0,campitch#=0.0,camyaw#=0.0,camroll#=0.0,camZoom#=1.0,texw%=1024,texh%=1024)
	Local v0%,v1%,v2%,v3%
	Local ndp.DrawPortal = New DrawPortal
	;ndp\x = x
	;ndp\y = y
	;ndp\z = z
	ndp\w = w
	ndp\h = h
	;ndp\pitch = pitch
	;ndp\yaw   = yaw
	;ndp\roll  = roll
	ndp\tex = CreateTexture(texw%,texh%,1+256+FE_RENDER+FE_ZRENDER) ;make a texture we can render to
	ndp\texw = texw
	ndp\texh = texh
	ndp\cam = CreateCamera() ;create a camera to enable rendering
	CameraRange ndp\cam,0.5,20.0
	PositionEntity ndp\cam,camx,camy,camz,True
	RotateEntity ndp\cam,campitch,camyaw,camroll,True
	CameraZoom ndp\cam,camZoom
	
	;ndp\camx = camx
	;ndp\camy = camy
	;ndp\camz = camz
	
	ndp\campitch = campitch
	ndp\camyaw   = camyaw
	ndp\camroll  = camroll
	
	ndp\camZoom  = camZoom
	
	ndp\portal = CreateMesh()
	ndp\brush  = CreateBrush()
	
	BrushTexture ndp\brush,ndp\tex
	
	ndp\surface = CreateSurface(ndp\portal,ndp\brush)
	v0=AddVertex(ndp\surface,w/2.0,h/2.0,0.0,0.0,0.0)
	v1=AddVertex(ndp\surface,w/2.0,-h/2.0,0.0,0.0,1.0)
	v2=AddVertex(ndp\surface,-w/2.0,h/2.0,0.0,1.0,0.0)
	v3=AddVertex(ndp\surface,-w/2.0,-h/2.0,0.0,1.0,1.0)
	AddTriangle(ndp\surface,v0,v2,v1) ;create the
	AddTriangle(ndp\surface,v3,v1,v2) ;first face
	
	v0=AddVertex(ndp\surface,w/2.0,h/2.0,0.0,1.0,0.0) ;new vertices to prevent image mirroring
	v1=AddVertex(ndp\surface,w/2.0,-h/2.0,0.0,1.0,1.0)
	v2=AddVertex(ndp\surface,-w/2.0,h/2.0,0.0,0.0,0.0)
	v3=AddVertex(ndp\surface,-w/2.0,-h/2.0,0.0,0.0,1.0)
	AddTriangle(ndp\surface,v0,v1,v2) ;create the
	AddTriangle(ndp\surface,v3,v2,v1) ;second face
	
	EntityFX ndp\portal,1
	PositionEntity ndp\portal,x,y,z,True
	RotateEntity ndp\portal,pitch,yaw,roll,True
	
	HideEntity ndp\cam ;prevent the camera from causing problems with the BackBuffer
	
	ndp\id = 0
	
	Local temp%
	temp = 0
	For c.DrawPortal = Each DrawPortal
	;	temp=temp+1
		temp = Max(c\id,temp)
	Next
	ndp\id = temp+1
	
	Return ndp
End Function

Function DestroyDrawPortal(ndp.DrawPortal)
	If ndp\brush<>0 Then FreeBrush ndp\brush
	ndp\brush = 0
	If ndp\tex<>0 Then FreeTexture ndp\tex
	ndp\tex = 0 : ndp\texw = 0 : ndp\texh = 0
	If ndp\cam<>0 Then FreeEntity ndp\cam
	ndp\cam = 0
	If ndp\portal<>0 Then FreeEntity ndp\portal
	ndp\portal = 0
	Delete ndp
End Function

Function UpdateDrawPortal(ndp.DrawPortal);,passive%=True)
	;ClsColor 255,255,255
	
	;If (Not passive) Then
	;	PositionEntity ndp\portal,ndp\x,ndp\y,ndp\z,True
	;Else
	;	ndp\x = EntityX(ndp\portal,True)
	;	ndp\y = EntityY(ndp\portal,True)
	;	ndp\z = EntityZ(ndp\portal,True)
	;EndIf
	
	;PositionEntity ndp\cam,ndp\camx,ndp\camy,ndp\camz,True
	pvt = CreatePivot()
	PositionEntity pvt, EntityX(Camera),EntityY(Camera),EntityZ(Camera)
	PointEntity pvt, ndp\portal
	
	ang# = WrapAngle(EntityYaw(pvt)-EntityYaw(ndp\portal,True))
	
	If ang > 90 And ang < 270 Then
		RotateEntity ndp\cam,ndp\campitch+EntityPitch(pvt)*0.5,ndp\camyaw+EntityYaw(pvt)-180.0,ndp\camroll,True
	Else
		RotateEntity ndp\cam,ndp\campitch+EntityPitch(pvt)*0.5,ndp\camyaw+EntityYaw(pvt),ndp\camroll,True
	EndIf
	
	CameraZoom ndp\cam, (EntityDistance(Camera, ndp\portal)/5.0)+0.6
	
	FreeEntity pvt
	
	ShowEntity ndp\cam ;enable the camera
	
	SetBuffer(TextureBuffer(ndp\tex))
	CameraViewport ndp\cam,0,0,ndp\texw,ndp\texh
	Cls
	RenderWorld ;requires FastExt to render to texture
	BrushTexture(ndp\brush,ndp\tex)
	PaintSurface(ndp\surface,ndp\brush)
	
	HideEntity ndp\cam ;disable the camera
	
	SetBuffer(BackBuffer())
	;ClsColor 0,0,0
End Function
;~IDEal Editor Parameters:
;~F#D
;~C#Blitz3D