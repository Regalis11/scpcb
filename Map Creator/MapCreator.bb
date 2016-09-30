
Graphics 480,240,0,2
SetBuffer BackBuffer()

AppTitle "SCP-CB Map Creator - Select Resolution"

Global ResolutionSelect% = GetINIInt("..\options.INI","map creator","resolution select")
Global ResWidth2% = GetINIInt("..\options.INI","map creator","width")
Global ResHeight2% = GetINIInt("..\options.INI","map creator","height")
Global ResWidth% = 150
Global ResHeight% = 100
Global ResolutionSelect2% = ResolutionSelect%

Global MouseDown1%, MouseHit1%, MouseDown2%, MouseSpeedX#, MouseSpeedY#, MouseSpeedZ#
Global SelectedTextBox% = 0
Global PrevSelectedTextBox% = 0

Global ResFactor# = 1.0

Global G_desktop_screen_width
Global G_desktop_screen_height
GetDesktopSize()

Global Modes = CountGfxModes()

Dim IsGoodMode(Modes+1)
Global ValidModes = 0
Dim HasDepth(3)

If ResolutionSelect%
	For i = 1 To Modes
		If Float(GfxModeWidth(i))/Float(GfxModeHeight(i)) < 1.34 And Float(GfxModeWidth(i))/Float(GfxModeHeight(i)) > 1.32
			IsGoodMode(i)=True
			ValidModes = i
		EndIf
		If GfxModeDepth(i)=16
			HasDepth(0)=True
		EndIf
		If GfxModeDepth(i)=24
			HasDepth(1)=True
		EndIf
		If GfxModeDepth(i)=32
			HasDepth(2)=True
		EndIf
		
	Next
	
	DebugLog ValidModes
	
	If HasDepth(0)=True And HasDepth(1)=True And HasDepth(2)=True
		ValidModes = ValidModes/3
	ElseIf HasDepth(0)=True And HasDepth(1)=True
		ValidModes = ValidModes/2
	ElseIf HasDepth(0)=True And HasDepth(2)=True
		ValidModes = ValidModes/2
	ElseIf HasDepth(1)=True And HasDepth(2)=True
		ValidModes = ValidModes/2
	EndIf
	
	DebugLog ValidModes
EndIf

ChangeDir ".."

Global ButtonSFX% = LoadSound_Strict("SFX\Interact\Button.ogg")

ChangeDir "Map Creator"

If Not ResolutionSelect Then Goto Skip

Repeat
	Cls
	Local x2 = 0
	Local y2 = 0
	Local width2 = 80
	Local height2 = 50
	MouseDown1 = MouseDown(1)
	MouseHit1 = MouseHit(1)
	
	For i = 1 To ValidModes
		;x2 = (width2/ValidModes) Mod 6
		If IsGoodMode(i)
			If Button(x2,y2,width2,height2,GfxModeWidth(i)+"X"+GfxModeHeight(i))
				ResWidth2 = GfxModeWidth(i)
				ResHeight2 = GfxModeHeight(i)
			EndIf
			x2=x2+width2
			If x2 > 460
				x2=0
				y2=y2+height2
			EndIf
		EndIf
		Color 255,255,255
		If GfxModeWidth(i)=ResWidth2 And GfxModeHeight(i)=ResHeight2
			Text 240,240-height2-20,"Selected Resolution: "+GfxModeWidth(i)+"X"+GfxModeHeight(i),1,1
		EndIf
	Next
	
	If Button(0,240-height2,width2,height2,"QUIT") Then End
	
	If Button(480-width2,240-height2,width2,height2,"START") Then
		PutINIValue("..\options.INI","map creator","width",ResWidth2%)
		PutINIValue("..\options.INI","map creator","height",ResHeight2%)
		PutINIValue("..\options.INI","map creator","resolution select",ResolutionSelect2%)
		ResolutionSelect% = False
	EndIf
	
	Color 255,255,255
	Text 150,240-height2,"Resolution"
	Text 150,255-height2,"Selection:"
	
	If ResolutionSelect2
		Color 0,255,0
		Text 150,270-height2,"Enabled"
		If Button(240,240-height2,width2,height2,"Disable")
			ResolutionSelect2 = False
		EndIf
	Else
		Color 255,0,0
		Text 150,270-height2,"Disabled"
		If Button(240,240-height2,width2,height2,"Enable")
			ResolutionSelect2 = True
		EndIf
	EndIf
	
	Flip
Until ResolutionSelect% = False

.Skip

ResWidth = ResWidth2
ResHeight = ResHeight2
ResFactor# = ResHeight/768.0

Graphics ResWidth,ResHeight,0,2
SetBuffer BackBuffer()

AppTitle "SCP-CB Map Creator"

Include "StrictLoads.bb"

Const ROOM1% = 1, ROOM2% = 2, ROOM2C% = 3, ROOM3% = 4, ROOM4% = 5

Const ZONEAMOUNT = 3


Global RoomTempID%
Type RoomTemplates
	Field Shape%, Name$
	Field Description$
	Field Large%
	Field id
	
	Field events$[5]
End Type 	

Function CreateRoomTemplate.RoomTemplates()
	Local rt.RoomTemplates = New RoomTemplates
	
	rt\id = RoomTempID
	RoomTempID=RoomTempID+1
	
	Return rt
End Function

Function LoadRoomTemplates(file$)
	Local TemporaryString$
	Local rt.RoomTemplates = Null
	Local StrTemp$ = ""
	
	Local f = OpenFile(file)
	
	While Not Eof(f)
		TemporaryString = Trim(ReadLine(f))
		If Left(TemporaryString,1) = "[" Then
			TemporaryString = Mid(TemporaryString, 2, Len(TemporaryString) - 2)
			
			If TemporaryString <> "room ambience" Then
				rt = CreateRoomTemplate()
				rt\Name = TemporaryString
				
				StrTemp = Lower(GetINIString(file, TemporaryString, "shape"))
				Select StrTemp
					Case "room1", "1"
						rt\Shape = ROOM1
					Case "room2", "2"
						rt\Shape = ROOM2
					Case "room2c", "2c", "2c"
						rt\Shape = ROOM2C
					Case "room3", "3"
						rt\Shape = ROOM3
					Case "room4", "4"
						rt\Shape = ROOM4
					Default
				End Select
				
				rt\Description = GetINIString(file, TemporaryString, "descr")
				rt\Large = GetINIInt(file, TemporaryString, "large")
				
			EndIf
			
		EndIf
	Wend
	
	CloseFile f
	
End Function

Function InitEvents()
	
	For rt.roomtemplates = Each RoomTemplates
		Select rt\name
			Case "173"
				rt\events[0]="173"
			Case "008"
				rt\events[0]="008"
			Case "914"
				rt\events[0]="914"
			Case "coffin"
				rt\events[0]="coffin"
				rt\events[1]="coffin106"
			Case "gatea"
				rt\events[0]="gatea"	
			Case "gateaentrance"
				rt\events[0]="gateaentrance"	
			Case "exit1"
				rt\events[0]="exit1"	
			Case "endroom"
				rt\events[0]="endroom106"	
			Case "start"
				rt\events[0]="alarm"	
			Case "pocketdimension"
				rt\events[0]="pocketdimension"
			Case "checkpoint1","checkpoint2"
				rt\events[0]="checkpoint"
			Case "lockroom"
				rt\events[0]="lockroom173"
			Case "lockroom2"
				rt\events[0]="lockroom096"	
			Case "testroom"
				rt\events[0]="testroom"
			Case "tunnel2"
				rt\events[0]="tunnel2"
				rt\events[1]="tunnel2smoke"
				rt\events[2]="096spawn"
			Case "roompj"
				rt\events[0]="pj"
			Case "room012"
				rt\events[0]="room012"
			Case "room035"
				rt\events[0]="room035"
			Case "room049"
				rt\events[0]="room049"
			Case "room079"
				rt\events[0]="room079"
			Case "room106"
				rt\events[0]="room106"
			Case "room205"
				rt\events[0]="room205"
			Case "room966"
				rt\events[0]="room966"
			Case "room1123"
				rt\events[0]="room1123"			
			Case "room2"
				rt\events[0]="room2trick"
				rt\events[1]="1048a"
			Case "room2_2"
				rt\events[0]="room2fan"	
				rt\events[1]="1048a"
			Case "room2_3"
				rt\events[0]="room2trick"
				rt\events[1]="1048a"
			Case "room2cafeteria"
				rt\events[0]="room2cafeteria"						
			Case "room2ccont"
				rt\events[0]= "room2ccont"
			Case "room2closets"
				rt\events[0]="room2closets"
			Case "room2doors"
				rt\events[0]="room2doors173"
			Case "room2elevator"
				rt\events[0]="room2elevator"
			Case "room2nuke"
				rt\events[0]="room2nuke"
			Case "room2offices2"
				rt\events[0]="room2offices2"
			Case "room2offices3"
				rt\events[0]="room2offices3"
			Case "room2pipes"
				rt\events[0]="room2pipes106"
				rt\events[1]="096spawn"
			Case "room2pit"
				rt\events[0]="room2pit"	
				rt\events[1]="room2pit106"
				rt\events[2]="096spawn"
			Case "room2poffices2"
				rt\events[0]="room2poffices2"					
			Case "room2servers"
				rt\events[0]="room2servers"
			Case "room2storage"
				rt\events[0]="room2storage"
			Case "room2tesla"
				rt\events[0]="room2tesla"	
			Case "room2testroom2"
				rt\events[0]="testroom173"	
			Case "room2test1074"
				rt\events[0]="room2test1074"
			Case "room2toilets"
				rt\events[0]="toiletguard"	
				rt\events[1]="buttghost"	
			Case "room2tunnel"
				rt\events[0]="room2tunnel"	
			Case "room3", "room3tunnel"
				rt\events[0]="room3door"				
			Case "room3servers","room3servers2"
				rt\events[0]="room3servers"
			Case "room3storage"
				rt\events[0]="room3storage"
			Case "room3pit"
				rt\events[0]="room3pitduck"
				rt\events[1]="room3pit1048"
				rt\events[2]="096spawn"
			Case "room3tunnel"
				rt\events[0]="room3tunnel"
				rt\events[1]="096spawn"
			Case "room3","room3_2" 
				rt\events[0]="106victim"
				rt\events[1]="106sinkhole"
			Case "room4"
				rt\events[0]="room4"
				rt\events[1]="106sinkhole"
			Case "room860"
				rt\events[0]="room860"
			Case "tunnel"
				rt\events[0]="tunnel106"
				rt\events[1]="096spawn"
			Case "room2tesla_lcz"
				rt\events[0]="room2tesla"
			Case "room2tesla_hcz"
				rt\events[0]="room2tesla"
			Case "room4tunnels"
				rt\events[0]="room4tunnels"
				rt\events[1]="096spawn"
			Case "room2gw"
				rt\events[0]="room_gw"
			Case "room2gw_b"
				rt\events[0]="room2gw_b"
			Case "room1162"
				rt\events[0]="room1162"
			Case "room2scps2"
				rt\events[0]="room2scps2"
			Case "room3gw"
				rt\events[0]="room_gw"
			Case "room2sl"
				rt\events[0]="room2sl"
			Case "room2_4"
				rt\events[0]="room2pit"
			Case "room4pit"
				rt\events[0]="096spawn"
			Case "room3z2"
				rt\events[0]="096spawn"
		End Select
	Next
	
End Function

ChangeDir ".."

Global Font1 = LoadFont_Strict("GFX\cour.ttf", 16*ResFactor)

ChangeDir "Map Creator"
SetFont Font1

Dim MapIcons(5,4)
MapIcons(ROOM1, 0)=LoadImage_Strict("room1.png")
MapIcons(ROOM2, 0)=LoadImage_Strict("room2.png")
MapIcons(ROOM2C, 0)=LoadImage_Strict("room2C.png")
MapIcons(ROOM3, 0)=LoadImage_Strict("room3.png")
MapIcons(ROOM4, 0)=LoadImage_Strict("room4.png")

For i = ROOM1 To ROOM4
	MaskImage MapIcons(i,0), 255,255,255
	ScaleImage MapIcons(i,0),ResFactor,ResFactor
	HandleImage MapIcons(i,0),Floor(ImageWidth(MapIcons(i,0))/2.0),Floor(ImageHeight(MapIcons(i,0))/2.0)
	For n = 1 To 3
		MapIcons(i,n)=CopyImage(MapIcons(i,0))
		MaskImage MapIcons(i,n), 255,255,255
		RotateImage(MapIcons(i,n),90*n)
		;If n = 2
		HandleImage MapIcons(i,n),Floor(ImageWidth(MapIcons(i,n))/2.0),Floor(ImageHeight(MapIcons(i,n))/2.0)
	Next
Next

Dim Arrows(4)
Arrows(0) = LoadImage_Strict("arrows.png")
ScaleImage Arrows(0),ResFactor,ResFactor
HandleImage Arrows(0),ImageWidth(Arrows(0))/2,ImageHeight(Arrows(0))/2
For i = 1 To 3
	Arrows(i)=CopyImage(Arrows(0))
	;ScaleImage Arrows(i),ResFactor,ResFactor
	HandleImage Arrows(i), ImageWidth(Arrows(i))/2,ImageHeight(Arrows(i))/2
	RotateImage Arrows(i), i*90
Next

Const ClrR = 50, ClrG = 50, ClrB = 50

Dim SavedMaps$(30)
Global CurrMap$, SavePath$, Saved%

LoadSavedMaps()

Global FileLocation$ = "..\Data\rooms.ini"

LoadRoomTemplates(FileLocation)
Global SelectedRoomTemplate.RoomTemplates
Global SelectedX%, SelectedY%, RotateRoom%

InitEvents()

Global MapWidth% = GetINIInt("..\options.ini", "options", "map size"), MapHeight% = GetINIInt("..\options.ini", "options", "map size")
Dim Map.RoomTemplates(MapWidth, MapHeight)
Dim MapAngle%(MapWidth, MapHeight)
Dim MapEvent$(MapWidth, MapHeight)
Dim MapEventProb#(MapWidth, MapHeight)
;Dim MapAngle%(MapWidth, MapHeight)

Global RoomTemplateAmount = 0

For rt.RoomTemplates = Each RoomTemplates
	If rt\Name = "start" Then
		Map(MapWidth/2-1,MapHeight)=rt
		MapEvent(MapWidth/2-1,MapHeight)="alarm"
		MapAngle(MapWidth/2-1,MapHeight)=180
	EndIf
	RoomTemplateAmount = RoomTemplateAmount + 1
Next

Global TickIMG = LoadImage_Strict("tickimg.png")
ScaleImage TickIMG,ResFactor,ResFactor

strtemp$ = "aaaa"

Global OnSideBar% = False

Repeat
	
	Local x,y,width,height
	Local ScrollMenuY#, ScrollMenuHeight#
	
	MouseDown1 = MouseDown(1)
	MouseHit1 = MouseHit(1)
	MouseDown2 = MouseDown(2)
	MouseSpeedX# = MouseXSpeed()
	MouseSpeedY# = MouseYSpeed()
	MouseSpeedZ# = MouseZSpeed()
	
	
	Cls
	ClsColor ClrR, ClrG, ClrB
	
	x = 20
	y = 50
	width = 180
	height = 768-70
	TextBox (x,y,width,height,"")
	
	Local RA# = Max(RoomTemplateAmount/67.0,1)
	
	y = y-((ScrollMenuY#*RA)*height)
	For rt.RoomTemplates = Each RoomTemplates
		;DebugLog rt\name
		If y > 40 And y < 30+height Then 
			If SelectedRoomTemplate = rt Then 
				Color 170, 170, 170
				Rect (x+2)*ResFactor,(y+10)*ResFactor,(width-4)*ResFactor,FontHeight()+1*ResFactor
				Color 0,0,0
			EndIf			
			
			If MouseY()>(y+9)*ResFactor And MouseY()<(y+9+18)*ResFactor Then
				If MouseX()>(x+1)*ResFactor And MouseX()<(x+(width-1))*ResFactor Then
					If (Not LockRoom(rt))
						Rect (x+1)*ResFactor,(y+9)*ResFactor,(width-3)*ResFactor,FontHeight()+2*ResFactor,False
						If MouseHit1 Then
							PlaySound ButtonSFX
							If SelectedRoomTemplate = rt Then
								SelectedRoomTemplate = Null
							Else
								SelectedRoomTemplate = rt
								SelectedX=0
								SelectedY=0
							EndIf
						EndIf
					EndIf
				EndIf
			EndIf
			
			If LockRoom(rt)
				Color 255,0,0
				Rect (x+1)*ResFactor,(y+9)*ResFactor,(width-3)*ResFactor,FontHeight()+2*ResFactor,True
			EndIf
			Color 0,0,0
			Text (x+10)*ResFactor, (y+10)*ResFactor, rt\Name
		EndIf
		y=y+18
	Next
	
	x = x+width+1
	y = 50
	width = 20
	
	ScrollMenuY# = DrawScrollBar(x,y,width,height,x, y + ((height - ScrollMenuHeight) * ScrollMenuY),20,ScrollMenuHeight,ScrollMenuY,1)
	
	ScrollMenuHeight = (height * ((height / 15) / Max(RoomTemplateAmount, height / 15))) ;' palkin korkeus
	
	x = 20+240
	y = 50
	width = 500
	height = 500
	TextBox (x,y,width,height,"")
	
	If Saved Then 
		Text (x+width/2)*ResFactor, (y+15)*ResFactor, CurrMap, True
	ElseIf CurrMap<>""
		Text (x+width/2)*ResFactor, (y+15)*ResFactor, CurrMap+"*", True
	EndIf
	
	
	If CurrMap <> "" Then 
		If Button (x+width/2-75, y+height-30, 150, 20, "Delete map", False) Then
			DebugLog CurrentDir()+"Maps\"+SavePath+".cbmap"
			If FileType(CurrentDir()+"Maps\"+SavePath+".cbmap")=1 Then 
				DeleteFile(CurrentDir()+"Maps\"+SavePath+".cbmap")
				SavePath = ""
				CurrMap = ""
				EraseMap()
				LoadSavedMaps()
			EndIf
		EndIf
	EndIf
	
	x = x+(width/2)-(MapWidth*10)-10
	y = y+(height/2)-(MapHeight*10)-10
	
	Local mx%,my%,temp%,nx%,ny%
	
	For mx = 0 To MapWidth
		For my = 0 To MapHeight
			If GetZone(my)=0
				Color 255,255,255
			ElseIf GetZone(my)=1
				Color 255,200,200
			Else
				Color 255,255,200
			EndIf
			Rect((x + mx * 20+1)*ResFactor, (y + my * 20+1)*ResFactor, 17*ResFactor, 17*ResFactor)
			
			If SelectedX> 0 Then 
				If mx=SelectedX And my=SelectedY Then
					Color 200,200,200
					Rect((x + mx * 20+1)*ResFactor, (y + my * 20+1)*ResFactor, 17*ResFactor, 17*ResFactor)
				EndIf
			EndIf			
			
			If Map(mx, my) = Null Then
				If mx=0 Or mx=MapWidth Or my=0 Or my = MapHeight Then
					Color 220, 220, 220
				Else
					Color 170, 170, 170
				EndIf
				
				Rect((x + mx * 20)*ResFactor, (y + my * 20)*ResFactor, 19*ResFactor, 19*ResFactor,False)
			Else
				Color 0,0,0
				DrawImage(MapIcons(Map(mx,my)\Shape, Floor(MapAngle(mx,my)/90.0)), (x + mx * 20 + 9)*ResFactor, (y + my * 20 + 9)*ResFactor)
				;Color 100,100,100
				;If Map(mx,my)\Large Then Rect ((x + mx * 20 - 9)*ResFactor, (y + my * 20 - 9)*ResFactor, 38*ResFactor, 38*ResFactor,False)
				;;Rect(x + mx * 20, y + my * 20, 19, 19,False)
			End If
			
			If MouseX()>(x + mx * 20)*ResFactor And mx>0 And mx<MapWidth Then
				If MouseX()<(x + mx * 20+19)*ResFactor Then
					If MouseY()>(y + my * 20)*ResFactor And my > 0 And my < MapHeight Then
						If MouseY()<(y + my * 20+19)*ResFactor Then
							Color 0,0,0
							Rect((x + mx * 20+1)*ResFactor, (y + my * 20+1)*ResFactor, 17*ResFactor, 17*ResFactor,False)
							
							If MouseHit1 Then
								If SelectedRoomTemplate <> Null Then
									temp = True
									;don't allow placing two large rooms next to each it other
									If SelectedRoomTemplate\Large Then 
										For nx = Max(mx-1,1) To Min(mx+1,MapWidth)
											For ny = Max(my-1,1) To Min(my+1,MapHeight)
												If Map(nx,ny)<>Null Then
													If Map(nx,ny)\Large Then temp = False : Exit
												EndIf
											Next
										Next
									EndIf
									
									If temp Then 
										Map(mx,my)=SelectedRoomTemplate
										MapEventProb(mx,my)=1.0
										Saved = False										
									EndIf
									
									
								Else
									SelectedRoomTemplate = Null
									If Map(mx,my)<>Null Then 
										SelectedX = mx
										SelectedY = my
										RotateRoom = True
									Else
										SelectedX=0
										SelectedY=0
									EndIf
								EndIf
							ElseIf MouseDown2
								Map(mx,my)=Null
								Saved = False
								If SelectedX = mx And SelectedY=my Then 
									SelectedX=0
									SelectedY=0
								EndIf
							EndIf
						EndIf
					EndIf
				EndIf
			EndIf
		Next
	Next
	
	For mx = 0 To MapWidth
		For my = 0 To MapHeight
			If Map(mx,my)<>Null
				Color 100,100,100
				If Map(mx,my)\Large Then Rect ((x + mx * 20 - 9)*ResFactor, (y + my * 20 - 9)*ResFactor, 38*ResFactor, 38*ResFactor,False)
			EndIf
		Next
	Next
	
	If MouseDown1 And Map(SelectedX, SelectedY)<>Null Then
		If RotateRoom Then 
			DrawImage Arrows(Floor(MapAngle(SelectedX, SelectedY)/90)), (x + SelectedX*20 + 10)*ResFactor, (y + SelectedY*20 + 10)*ResFactor
			
			If Distance((x + SelectedX*20 + 10)*ResFactor, (y + SelectedY*20 + 10)*ResFactor, MouseX(), MouseY())>15*ResFactor Then
				Saved = False
				MapAngle(SelectedX, SelectedY) = WrapAngle(Floor((GetAngle((x + SelectedX*20 + 10)*ResFactor, (y + SelectedY*20 + 10)*ResFactor, MouseX(), MouseY())+45.0)/90.0)*90.0-90)
			EndIf
		EndIf
	Else
		RotateRoom = False
	EndIf
	
	
	x = 20+240
	y = 570
	width = 500
	height = 768-590
	TextBox (x,y,width,height,"")
	
	If SelectedRoomTemplate <> Null Then
		Text (x+20)*ResFactor, (y+20)*ResFactor, "Selected room template: "+SelectedRoomTemplate\Name
		Text (x+20)*ResFactor, (y+50)*ResFactor, SelectedRoomTemplate\Description
		
		If Button(x+20, y+80, 150,25, "Deselect") Then SelectedRoomTemplate = Null
		
	ElseIf SelectedX <> 0
		Text (x+20)*ResFactor, (y+20)*ResFactor, "Selected room: " +Map(SelectedX,SelectedY)\Name
		Text (x+20)*ResFactor, (y+50)*ResFactor, Map(SelectedX,SelectedY)\Description
		
		Text (x+20)*ResFactor, (y+80)*ResFactor, "Angle: "
		If Button(x+80, y+80-4, 20,20, "-") Then MapAngle(SelectedX,SelectedY)=WrapAngle(MapAngle(SelectedX,SelectedY)-90)
		Text (x+130)*ResFactor, (y+80)*ResFactor, MapAngle(SelectedX,SelectedY), True
		If Button(x+160, y+80-4, 20,20, "+") Then MapAngle(SelectedX,SelectedY)=WrapAngle(MapAngle(SelectedX,SelectedY)+90)
		
		Text (x+20)*ResFactor, (y+100)*ResFactor, "Events: "
		;y=y+110+20
		y=y+100+20
		For i = 0 To 4
			If Map(SelectedX,SelectedY)\events[i]<>"" Then
				Text (x+50)*ResFactor, y*ResFactor, Map(SelectedX,SelectedY)\events[i]
				If Tick(x+20, y, (MapEvent(SelectedX,SelectedY)=Map(SelectedX,SelectedY)\events[i])) Then
					MapEvent(SelectedX,SelectedY)=Map(SelectedX,SelectedY)\events[i]
					If Button(x+240, y-4, 20,20, "-") Then 
						MapEventProb(SelectedX,SelectedY) = Max(MapEventProb(SelectedX,SelectedY)-0.1, 0.0)
						Saved = False
					EndIf
					Text (x+335)*ResFactor, y*ResFactor, "Probability: "+ MapEventProb(SelectedX,SelectedY), True
					If Button(x+410, y-4, 20,20, "+") Then 
						MapEventProb(SelectedX,SelectedY) = Min(MapEventProb(SelectedX,SelectedY)+0.1, 1.0)		
						Saved = False
					EndIf
				Else
					If MapEvent(SelectedX,SelectedY)=Map(SelectedX,SelectedY)\events[i] Then MapEvent(SelectedX,SelectedY)=""
				EndIf
				
				y=y+20
			EndIf
		Next
	EndIf
	
	x = x + width + 40
	y = 50
	width = 1024 - x - 20
	height = 500
	TextBox (x,y,width,height,"")
	
	
	Text (x+20)*ResFactor,(y+20)*ResFactor,"Saved maps: "
	y=y+40
	For i = 0 To 20
		If SavedMaps(i)<>"" Then
			If CurrMap = SavedMaps(i) Then 
				Color 170, 170, 170
				Rect (x+2)*ResFactor,y*ResFactor,(width-4)*ResFactor,20*ResFactor
				Color 0,0,0
				If Saved Then 
					Text (x+20)*ResFactor, (y+2)*ResFactor, SavedMaps(i)
				Else
					Text (x+20)*ResFactor, (y+2)*ResFactor, SavedMaps(i)+"*"	
				EndIf
			Else
				Text (x+20)*ResFactor, (y+2)*ResFactor, SavedMaps(i)
			EndIf
			
			
			If MouseX()>x*ResFactor And MouseX()<(x+width)*ResFactor Then
				If MouseY()> y*ResFactor And MouseY()<(y+19)*ResFactor Then
					Rect (x+1)*ResFactor,y*ResFactor,(width-3)*ResFactor,20*ResFactor,False
					If MouseHit1 Then 
						PlaySound ButtonSFX
						SavePath = SavedMaps(i)
						CurrMap = SavePath
						LoadMap("Maps\"+SavedMaps(i))
					EndIf
				EndIf
			EndIf			
			y=y+20
		EndIf
	Next
	
	
	y = 50+550 - 20
	width = 1024 - x - 20
	height = 25
	
	If Button(x,y,width,height, "Save", (CurrMap="")) Then
		SaveMap("Maps\"+CurrMap)
	EndIf	
	
	y = y + 40
	
	If Button(x,y,width,height, "Save as "+SavePath+"", (SavePath="")) Then
		If CurrMap = "" Then CurrMap = SavePath
		SaveMap("Maps\"+SavePath)
		LoadSavedMaps()
	EndIf
	
	SavePath$ = Left(InputBox(x,y+25,width,height,SavePath),15)
	
	y = y + 80
	
	If Button(x,y,width,height, "New map") Then
		CurrMap = ""
		SavePath = ""
		SelectedX = 0
		SelectedY = 0
		For i = 0 To MapWidth-1
			For j = 0 To MapHeight-1
				Map(i, j) = Null
				MapAngle(i, j) = 0
				MapEvent$(i, j) = ""
				MapEventProb#(i, j) = 0
			Next
		Next
	EndIf
	
	If Button(x+width/2, 15, width/2, height, "QUIT") Then End
	
	
	Flip
	
	Delay 8
Forever

Function Button%(x,y,width,height,txt$, disabled%=False)
	Local Pushed = False
	
	Color ClrR, ClrG, ClrB
	If Not disabled Then 
		If MouseX() > x*ResFactor And MouseX() < (x+width)*ResFactor Then
			If MouseY() > y*ResFactor And MouseY() < (y+height)*ResFactor Then
				If MouseDown1 Then
					Pushed = True
					Color ClrR*0.6, ClrG*0.6, ClrB*0.6
				Else
					Color Min(ClrR*1.2,255),Min(ClrR*1.2,255),Min(ClrR*1.2,255)
				EndIf
			EndIf
		EndIf
	EndIf
	
	If Pushed Then 
		Rect x*ResFactor,y*ResFactor,width*ResFactor,height*ResFactor
		Color 133,130,125
		Rect (x+1)*ResFactor,(y+1)*ResFactor,(width-1)*ResFactor,(height-1)*ResFactor,False	
		Color 10,10,10
		Rect x*ResFactor,y*ResFactor,width*ResFactor,height*ResFactor,False
		Color 250,250,250
		Line x*ResFactor,(y+height-1)*ResFactor,(x+width-1)*ResFactor,(y+height-1)*ResFactor
		Line (x+width-1)*ResFactor,y*ResFactor,(x+width-1)*ResFactor,(y+height-1)*ResFactor
	Else
		Rect x*ResFactor,y*ResFactor,width*ResFactor,height*ResFactor
		Color 133,130,125
		Rect x*ResFactor,y*ResFactor,(width-1)*ResFactor,(height-1)*ResFactor,False	
		Color 250,250,250
		Rect x*ResFactor,y*ResFactor,width*ResFactor,height*ResFactor,False
		Color 10,10,10
		Line x*ResFactor,(y+height-1)*ResFactor,(x+width-1)*ResFactor,(y+height-1)*ResFactor
		Line (x+width-1)*ResFactor,y*ResFactor,(x+width-1)*ResFactor,(y+height-1)*ResFactor		
	EndIf
	
	Color 255,255,255
	If disabled Then Color 70,70,70
	Text (x+width/2)*ResFactor,(y+height/2-1)*ResFactor, txt, True, True
	
	Color 0,0,0
	
	If Pushed And MouseHit1 Then PlaySound ButtonSFX : Return True
End Function

Function Tick(x,y,selected%)
	TextBox(x,y,13,13,"")
	
	If selected Then
		DrawImage TickIMG, x*ResFactor, y*ResFactor
	EndIf
	
	If MouseX() > x*ResFactor And MouseX() < (x+13)*ResFactor Then
		If MouseY() > y*ResFactor And MouseY() < (y+13)*ResFactor Then
			If MouseHit1 Then PlaySound ButtonSFX : Return (Not selected)
		EndIf
	EndIf	
	
	Return selected
	
End Function

Function InputBox$(x,y,width,height,Txt$,ID=0)
	TextBox(x,y,width,height,Txt$)
	
	Local MouseOnBox = False
	
	If MouseX() > x*ResFactor And MouseX() < (x+width)*ResFactor Then
		If MouseY() > y*ResFactor And MouseY() < (y+height)*ResFactor Then
			MouseOnBox = True
			If MouseHit1 Then SelectedTextBox = ID : FlushKeys
		EndIf
	EndIf	
	
	If MouseOnBox = False And MouseHit1 And SelectedTextBox = ID Then SelectedTextBox = 0
	
	If SelectedTextBox = ID Then
		Txt = rInput(Txt)
		Color 0,0,0
		If (MilliSecs() Mod 800) < 400 Then  Rect ((x+width/2 + StringWidth(Txt)/2 + 2))*ResFactor, (y+height/2-5)*ResFactor, 2*ResFactor, 12*ResFactor
	EndIf
	
	Return Txt
End Function

Function TextBox(x,y,width,height,Txt$)
	Color 255,255,255
	Rect x*ResFactor,y*ResFactor,width*ResFactor,height*ResFactor
	
	Color 128,128,128
	Rect x*ResFactor,y*ResFactor,width*ResFactor,height*ResFactor,False
	Color 64,64,64
	Rect (x+1)*ResFactor,(y+1)*ResFactor,(width-2)*ResFactor,(height-2)*ResFactor,False	
	Color 255,255,255
	Line (x+width-1)*ResFactor,y*ResFactor,(x+width-1)*ResFactor,(y+height-1)*ResFactor
	Line x*ResFactor,(y+height-1)*ResFactor,(x+width-1)*ResFactor,(y+height-1)*ResFactor
	Color 212, 208, 199
	Line (x+width-2)*ResFactor,(y+1)*ResFactor,(x+width-2)*ResFactor,(y+height-2)*ResFactor
	Line (x+1)*ResFactor,(y+height-2)*ResFactor,(x+width-2)*ResFactor,(y+height-2)*ResFactor
	
	Color 0,0,0
	Text (x+width/2)*ResFactor,(y+height/2)*ResFactor, Txt, True, True
End Function

Function rInput$(aString$)
	value = GetKey()
	length = Len(aString$)
	If value = 8 Then value = 0 :If length > 0 Then aString$ = Left$(aString,Length-1)
	If value = 13 Then Goto ende
	If value = 0 Then Goto ende
	If value>0 And value<7 Or value>26 And value<32 Or value=9 Then Goto ende
	aString$=aString$ + Chr$(value)
	.ende
	Return aString$
End Function

Function SlideBar#(x, y, leveys, arvo)
	
	If MouseDown(1) Then
		If MouseX() >= (x-5)*ResFactor And MouseX() <= (x+leveys+15)*ResFactor And MouseY() >= (y-5)*ResFactor And MouseY() <= (y+5)*ResFactor Then
			arvo = Min(Max((MouseX()-x-5)*100/leveys, 0), 100)
		EndIf
	EndIf
	
	TextBox(x, y-5, leveys+10, 10, "")
	
	Button(x + leveys*arvo/100, y-5, 10, 11, "")
	
	Return arvo
	
End Function


Function DrawScrollBar#(x, y, width, height, barx, bary, barwidth, barheight, bar#, dir = 0)
	;0 = vaakasuuntainen, 1 = pystysuuntainen
	
	Color(0, 0, 0)
	Rect(x*ResFactor, y*ResFactor, width*ResFactor, height*ResFactor)
	Button(barx, bary, barwidth, barheight, "")
	
	If dir = 0 Then ;vaakasuunnassa
		If height > 10 Then
			Color 250,250,250
			Rect((barx + barwidth / 2)*ResFactor, (bary + 5)*ResFactor, 2*ResFactor, (barheight - 10)*ResFactor)
			Rect((barx + barwidth / 2 - 3)*ResFactor, (bary + 5)*ResFactor, 2*ResFactor, (barheight - 10)*ResFactor)
			Rect((barx + barwidth / 2 + 3)*ResFactor, (bary + 5)*ResFactor, 2*ResFactor, (barheight - 10)*ResFactor)
		EndIf
	Else ;pystysuunnassa
		If width > 10 Then
			Color 250,250,250
			Rect((barx + 4)*ResFactor, (bary + barheight / 2)*ResFactor, (barwidth - 10)*ResFactor, 2*ResFactor)
			Rect((barx + 4)*ResFactor, (bary + barheight / 2 - 3)*ResFactor, (barwidth - 10)*ResFactor, 2*ResFactor)
			Rect((barx + 4)*ResFactor, (bary + barheight / 2 + 3)*ResFactor, (barwidth - 10)*ResFactor, 2*ResFactor)
		EndIf
	EndIf
	
	If MouseDown1 Then
		If MouseX()>barx*ResFactor And MouseX()<(barx+barwidth)*ResFactor Then
			If MouseY()>bary*ResFactor And MouseY()<(bary+barheight)*ResFactor Then
				OnSideBar% = True
			EndIf
		EndIf
		
		If OnSideBar
			If dir = 0 Then
				Return Min(Max(bar + MouseSpeedX / Float(width - barwidth), 0), 1)
			Else
				Return Min(Max(bar + MouseSpeedY / Float(height - barheight), 0), 1)
			End If
		EndIf
	Else
		OnSideBar = False
	End If
	
	;Speed = 10 (Prev)
	If MouseSpeedZ <> 0
		If dir = 0 Then
			Return Min(Max(bar + -(MouseSpeedZ*15) / Float(width - barwidth), 0), 1)
		Else
			Return Min(Max(bar + -(MouseSpeedZ*15) / Float(height - barheight), 0), 1)
		End If
	EndIf
	
	Return bar

End Function



Function SaveMap(file$)
	f% = WriteFile(file+".cbmap")
	
	For x = 0 To MapWidth
		For y = 0 To MapHeight
			If Map(x,y)<>Null Then
				WriteByte f, x
				WriteByte f, y
				WriteString f, Lower(Map(x,y)\Name)
				WriteByte f, Floor(MapAngle(x,y)/90.0)
				WriteString f, MapEvent(x,y)
				WriteFloat f, MapEventProb(x,y)
			EndIf
		Next
	Next
	
	Saved = True
	
	CloseFile f
End Function

Function LoadMap(file$)
	EraseMap()
	
	f% = ReadFile(file+".cbmap")
	DebugLog file+".cbmap"
	
	While Not Eof(f)
		DebugLog "dsfkjmgndfklmgkl"
		x = ReadByte(f)
		y = ReadByte(f)
		name$ = ReadString(f)
		DebugLog x+", "+y+": "+name
		For rt.roomtemplates=Each RoomTemplates
			If Lower(rt\name) = name Then
				DebugLog rt\name
				Map(x,y)=rt
				Exit
			EndIf
		Next
		MapAngle(x,y)=ReadByte(f)*90
		MapEvent(x,y) = ReadString(f)
		MapEventProb(x,y) = ReadFloat(f)
	Wend
	
	Saved = True
	
	CloseFile f
End Function

Function LoadSavedMaps()
	
	For i = 0 To 20
		SavedMaps(i)=""
	Next
	
	myDir=ReadDir(CurrentDir()+"\Maps") 
	i = 0
	Repeat 
		file$=NextFile$(myDir)
		DebugLog file
		If file$="" Then Exit 
		If FileType("Maps\"+file$) = 1 Then 
			If file <> "." And file <> ".." Then 
				SavedMaps(i) = Left(file,Max(Len(file)-6,1))
				i=i+1
			EndIf
		End If 
	Forever 
	CloseDir myDir 
	
End Function


Function EraseMap()
	SelectedX = 0
	SelectedY = 0
	SelectedRoomTemplate = Null
	
	For x = 1 To MapWidth-1
		For y = 1 To MapHeight-1
			Map(x,y)=Null
			MapAngle(x,y)=0
			MapEvent(x,y)=""
			MapEventProb(x,y)=0.0
		Next
	Next
End Function

;INI-funktiot:
Function GetINIString$(file$, section$, parameter$)
	Local TemporaryString$ = ""
	Local f = ReadFile(file)
	
	While Not Eof(f)
		If ReadLine(f) = "["+section+"]" Then
			Repeat 
				TemporaryString = ReadLine(f)
				If Trim( Left(TemporaryString, Max(Instr(TemporaryString,"=")-1,0)) ) = parameter Then
					CloseFile f
					Return Trim( Right(TemporaryString,Len(TemporaryString)-Instr(TemporaryString,"=")) )
				EndIf
			Until Left(TemporaryString,1) = "[" Or Eof(f)
			CloseFile f
			Return ""
		EndIf
	Wend
	
	CloseFile f
End Function

Function GetINIInt%(file$, section$, parameter$)
	Local strtemp$ = Lower(GetINIString(file$, section$, parameter$))
	
	Select strtemp
		Case "true"
			Return 1
		Case "false"
			Return 0
		Default
			Return Int(strtemp)
	End Select
	Return 
End Function

Function GetINIFloat#(file$, section$, parameter$)
	Return GetINIString(file$, section$, parameter$)
End Function


; matemaattiset funktiot:
Function Min#(a#,b#)
	If a < b Then Return a Else Return b
End Function

Function Max#(a#,b#)
	If a > b Then Return a Else Return b
End Function

Function WrapAngle#(angle#)
	While angle < 0
		angle = angle + 360
	Wend 
	While angle >= 360
		angle = angle - 360
	Wend
	Return angle
End Function

Function GetAngle#(x1#, y1#, x2#, y2#)
	Return ATan2( y2 - y1, x2 - x1 )
End Function

Function Distance#(x1#, y1#, x2#, y2#)
	Return(Sqr(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1))))
End Function


Function PutINIValue%(INI_sAppName$, INI_sSection$, INI_sKey$, INI_sValue$)
	
; Returns: True (Success) or False (Failed)
	
	INI_sSection = "[" + Trim$(INI_sSection) + "]"
	INI_sUpperSection$ = Upper$(INI_sSection)
	INI_sKey = Trim$(INI_sKey)
	INI_sValue = Trim$(INI_sValue)
	INI_sFilename$ = CurrentDir$() + "\"  + INI_sAppName
	
; Retrieve the INI data (if it exists)
	
	INI_sContents$= INI_FileToString(INI_sFilename)
	
; (Re)Create the INI file updating/adding the SECTION, KEY and VALUE
	
	INI_bWrittenKey% = False
	INI_bSectionFound% = False
	INI_sCurrentSection$ = ""
	
	INI_lFileHandle = WriteFile(INI_sFilename)
	If INI_lFileHandle = 0 Then Return False ; Create file failed!
	
	INI_lOldPos% = 1
	INI_lPos% = Instr(INI_sContents, Chr$(0))
	
	While (INI_lPos <> 0)
		
		INI_sTemp$ =Trim$(Mid$(INI_sContents, INI_lOldPos, (INI_lPos - INI_lOldPos)))
		
		If (INI_sTemp <> "") Then
			
			If Left$(INI_sTemp, 1) = "[" And Right$(INI_sTemp, 1) = "]" Then
				
				; Process SECTION
				
				If (INI_sCurrentSection = INI_sUpperSection) And (INI_bWrittenKey = False) Then
					INI_bWrittenKey = INI_CreateKey(INI_lFileHandle, INI_sKey, INI_sValue)
				End If
				INI_sCurrentSection = Upper$(INI_CreateSection(INI_lFileHandle, INI_sTemp))
				If (INI_sCurrentSection = INI_sUpperSection) Then INI_bSectionFound = True
				
			Else
				
				; KEY=VALUE
				
				lEqualsPos% = Instr(INI_sTemp, "=")
				If (lEqualsPos <> 0) Then
					If (INI_sCurrentSection = INI_sUpperSection) And (Upper$(Trim$(Left$(INI_sTemp, (lEqualsPos - 1)))) = Upper$(INI_sKey)) Then
						If (INI_sValue <> "") Then INI_CreateKey INI_lFileHandle, INI_sKey, INI_sValue
						INI_bWrittenKey = True
					Else
						WriteLine INI_lFileHandle, INI_sTemp
					End If
				End If
				
			End If
			
		End If
		
		; Move through the INI file...
		
		INI_lOldPos = INI_lPos + 1
		INI_lPos% = Instr(INI_sContents, Chr$(0), INI_lOldPos)
		
	Wend
	
	; KEY wasn't found in the INI file - Append a new SECTION if required and create our KEY=VALUE line
	
	If (INI_bWrittenKey = False) Then
		If (INI_bSectionFound = False) Then INI_CreateSection INI_lFileHandle, INI_sSection
		INI_CreateKey INI_lFileHandle, INI_sKey, INI_sValue
	End If
	
	CloseFile INI_lFileHandle
	
	Return True ; Success
	
End Function

Function INI_FileToString$(INI_sFilename$)
	
	INI_sString$ = ""
	INI_lFileHandle% = ReadFile(INI_sFilename)
	If INI_lFileHandle <> 0 Then
		While Not(Eof(INI_lFileHandle))
			INI_sString = INI_sString + ReadLine$(INI_lFileHandle) + Chr$(0)
		Wend
		CloseFile INI_lFileHandle
	End If
	Return INI_sString
	
End Function

Function INI_CreateSection$(INI_lFileHandle%, INI_sNewSection$)
	
	If FilePos(INI_lFileHandle) <> 0 Then WriteLine INI_lFileHandle, "" ; Blank line between sections
	WriteLine INI_lFileHandle, INI_sNewSection
	Return INI_sNewSection
	
End Function

Function INI_CreateKey%(INI_lFileHandle%, INI_sKey$, INI_sValue$)
	
	WriteLine INI_lFileHandle, INI_sKey + "=" + INI_sValue
	Return True
	
End Function



Function LockRoom(rt.RoomTemplates)
	
	If rt\Name = "173" Then Return True
	If rt\Name = "start" Then Return True
	If rt\Name = "gatea" Then Return True
	If rt\Name = "pocketdimension" Then Return True
	If rt\Name = "dimension1499" Then Return True
	
End Function

Function GetDesktopSize()
	; Gets the width and height of the desktop on the main monitor and returns them in
	; the globals G_desktop_screen_width and G_desktop_screen_height.
	
	Local rectangle = CreateBank( 16 )
	api_GetClientRect( api_GetDesktopWindow(), rectangle )
	G_desktop_screen_width = PeekInt( rectangle, 8 ) - PeekInt( rectangle, 0 )
	G_desktop_screen_height = PeekInt( rectangle, 12 ) - PeekInt( rectangle, 4 )
	FreeBank rectangle
End Function

Function GetZone(y%)
	Return Min(Floor((Float(MapWidth-y)/MapWidth*ZONEAMOUNT)),ZONEAMOUNT-1)
End Function



;~IDEal Editor Parameters:
;~F#93#9C#A5#D0#352#369#37C#388#3D1#3E6#403#41B#42B#440#44E#454#458#45C#466#46A
;~F#46F#4BF#4CD#4D5#4DE#4E8
;~C#Blitz3D