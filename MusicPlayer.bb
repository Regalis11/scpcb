Include "Source/BlitzAL.bb"

AppTitle "SCP:CB Music Player"

Graphics 800,600,0,2
SetBuffer BackBuffer()

alInitialise()

Global CurrMusicWindow%=0
Global MusicVolume# = 1.0
Global MusicCHN = 0
Global CurrMusic = -1
Global MusicPlaying = -1
Dim Music$(256)
Global MusicAmount%=0
Global MusicAmount2%=0
Global IsMusicPlaying%=0

Local dirPath$ = "SFX\Radio\UserTracks\"
If FileType(dirPath)<>2 Then
	CreateDir(dirPath)
EndIf

Const MusicPath$ = "SFX\Music\"
Const MusicPath2$ = "SFX\Radio\UserTracks\"

Local Dir% = ReadDir(MusicPath)
Repeat
	Local file$=NextFile(Dir)
	If file$="" Then Exit
	If FileType(MusicPath+file$) = 1 Then
		Music(MusicAmount) = file$
		MusicAmount = MusicAmount + 1
	EndIf
Forever
CloseDir Dir

Dir% = ReadDir(MusicPath2)
Repeat
	file$=NextFile(Dir)
	If file$="" Then Exit
	If FileType(MusicPath2+file$) = 1 Then
		Music(MusicAmount2+MusicAmount) = file$
		MusicAmount2 = MusicAmount2 + 1
	EndIf
Forever
CloseDir Dir

Local i

Const ClrR = 50, ClrG = 50, ClrB = 50

Global MouseHit1,MouseDown1

Global OnSliderID=0

Global x#,y#,width#,height#

Global PrevBarTime# = 0.0, CurrBarTime# = 0.0
Global CurrPitch# = 1.0

Repeat
	Cls
	ClsColor 200,200,200
	
	MouseHit1 = MouseHit(1)
	MouseDown1 = MouseDown(1)
	alUpdate()
	
	If (Not MouseDown1)
		OnSliderID = 0
	EndIf
	
	x = 0
	y = 150
	width = 200
	height = 25
	If CurrMusicWindow=0
		For i = 0 To MusicAmount-1
			If Right(Music(i),4)=".ogg"
				If Button(x,y,width,height,ShortLine(Music(i),25),(i=CurrMusic))
					CurrMusic = i
					DebugLog "Playing Music: "+Music(i)
					CurrBarTime# = 0.0
					PrevBarTime# = 0.0
					AppTitle "SCP:CB Music Player - playing "+Chr(34)+Music(i)+Chr(34)
				EndIf
			Else
				Button(x,y,width,height,ShortLine(Music(i),25),True)
				Color 210,50,55
				Rect x+1,y+1,width-2,height-2,False
				Text (x+width/2),(y+height/2-1),ShortLine(Music(i),25),True,True
			EndIf
			If i=CurrMusic
				Color 50,210,55
				Rect x+1,y+1,width-2,height-2,False
				Text (x+width/2),(y+height/2-1),ShortLine(Music(i),25),True,True
			EndIf
			y=y+height
			If y > 600
				y=150
				x=x+width
			EndIf
		Next
	Else
		For i = 0 To MusicAmount2-1
			If Right(Music(i+MusicAmount),4)=".ogg" Or Right(Music(i+MusicAmount),4)=".wav"
				If Button(x,y,width,height,ShortLine(Music(i+MusicAmount),25),(i+MusicAmount=CurrMusic))
					CurrMusic = i+MusicAmount
					DebugLog "Playing Music: "+Music(i+MusicAmount)
					CurrBarTime# = 0.0
					PrevBarTime# = 0.0
					AppTitle "SCP:CB Music Player - playing "+Chr(34)+Music(i+MusicAmount)+Chr(34)
				EndIf
			Else
				Button(x,y,width,height,ShortLine(Music(i+MusicAmount),25),True)
				Color 210,50,55
				Rect x+1,y+1,width-2,height-2,False
				Text (x+width/2),(y+height/2-1),ShortLine(Music(i+MusicAmount),25),True,True
			EndIf
			If i+MusicAmount=CurrMusic
				Color 50,210,55
				Rect x+1,y+1,width-2,height-2,False
				Text (x+width/2),(y+height/2-1),ShortLine(Music(i+MusicAmount),25),True,True
			EndIf
			y=y+height
			If y > 600
				y=150
				x=x+width
			EndIf
		Next
	EndIf
	x = 0
	y = 150
	width = 255
	height = 25
	Color 40,80,50
	Rect 0,0,800,y-20,True
	Color 40,50,80
	Rect 0,y-20,800,20,True
	If CurrMusicWindow = 0
		If Button(750,y-20,50,20,"Next")
			CurrMusicWindow = 1
		EndIf
		Button(0,y-20,50,20,"Prev",True)
		Color 140,140,140
		Text 520,90,"Compatible Audio file(s): OGG",0,1
	Else
		If Button(0,y-20,50,20,"Prev")
			CurrMusicWindow = 0
		EndIf
		Button(750,y-20,50,20,"Next",True)
		Color 140,140,140
		Text 520,90,"Compatible Audio file(s): OGG,WAV",0,1
	EndIf
	Color 130,140,150
	If CurrMusicWindow=0
		Text GraphicsWidth()/2,y-11,"List of songs (in "+Chr(34)+MusicPath+Chr(34)+" folder):",1,1
	Else
		Text GraphicsWidth()/2,y-11,"List of songs (in "+Chr(34)+MusicPath2+Chr(34)+" folder):",1,1
	EndIf
	If IsMusicPlaying=1
		Color 100,100,100
		If alSourceIsPlaying(MusicCHN)
			PrevBarTime# = (alSourceGetAudioTime(MusicCHN,0)-1)/(alSourceGetLenght(MusicCHN,0)-1)
			CurrBarTime# = CurveValue(PrevBarTime#,CurrBarTime#,400.0/alSourceGetLenght(MusicCHN,0))
		EndIf
		Rect 10,10,780*CurrBarTime#,20,1
		Color 140,140,140
		Text GraphicsWidth()/2,40,"Playing Music: "+Chr(34)+ShortLine$(Music(MusicPlaying),38)+Chr(34),1,1
		Text GraphicsWidth()/2,60,"Music Time: "+GetTime(Int(alSourceGetAudioTime(MusicCHN,0)-1))+"/"+GetTime(Int(alSourceGetLenght(MusicCHN,0)-1)),1,1
		If alSourceIsPlaying(MusicCHN)
			If Button(60,40,50,25,"Pause")
				alSourcePause(MusicCHN)
			EndIf
		Else
			If Button(60,40,50,25,"Play")
				alSourceResume(MusicCHN)
			EndIf
		EndIf
		If Button(110,40,60,25,"Restart")
			alSourceStop(MusicCHN)
			alSourceSeek(MusicCHN,0,0)
			alSourcePlay_(MusicCHN,True)
			CurrBarTime# = 0.0
			PrevBarTime# = 0.0
		EndIf
		If alSourceGetAudioTime(MusicCHN,0)<1
			CurrBarTime# = 0.0
			PrevBarTime# = 0.0
		EndIf
	Else
		Color 140,140,140
		Text GraphicsWidth()/2,40,"Playing Music: None",1,1
		Text GraphicsWidth()/2,60,"Music Time: 00:00/00:00",1,1
		Button(60,40,50,25,"Pause",True)
		Button(110,40,60,25,"Restart",True)
	EndIf
	If Button(10,40,50,25,"Stop",(Not IsMusicPlaying))
		CurrMusic = -1
		IsMusicPlaying = 0
		alSourceStop(MusicCHN)
		alFreeSource(MusicCHN)
		MusicPlaying = -1
		CurrBarTime# = 0.0
		PrevBarTime# = 0.0
		AppTitle "SCP:CB Music Player"
	EndIf
	Color 5,5,5
	Rect 10,10,780,20,0
	MusicVolume = (SlideBar(30, 80, 100, MusicVolume*100.0)/100.0)
	Color 140,140,140
	Text 90,110,"Music Volume: "+Int(MusicVolume*100)+"%",1,1
	CurrPitch = (SlideBar(220, 80, 100, CurrPitch*50.0,0,200)/50.0)
	Color 140,140,140
	Text 280,110,"Music Pitch: "+Int(CurrPitch*100)+"%",1,1
	If Button(675,40,115,25,"Close Program")
		alDestroy()
		End
	EndIf
	
	UpdateMusic()
	Flip 0
Until KeyHit(1)
alDestroy()
End

Function Button%(x,y,width,height,txt$, disabled%=False)
	Local Pushed = False
	
	Color ClrR, ClrG, ClrB
	If Not disabled Then 
		If MouseX() > x And MouseX() < (x+width) Then
			If MouseY() > y And MouseY() < (y+height) Then
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
		Rect x,y,width,height
		Color 133,130,125
		Rect (x+1),(y+1),(width-1),(height-1),False	
		Color 10,10,10
		Rect x,y,width,height,False
		Color 250,250,250
		Line x,(y+height-1),(x+width-1),(y+height-1)
		Line (x+width-1),y,(x+width-1),(y+height-1)
	Else
		Rect x,y,width,height
		Color 133,130,125
		Rect x,y,(width-1),(height-1),False	
		Color 250,250,250
		Rect x,y,width,height,False
		Color 10,10,10
		Line x,(y+height-1),(x+width-1),(y+height-1)
		Line (x+width-1),y,(x+width-1),(y+height-1)		
	EndIf
	
	Color 255,255,255
	If disabled Then Color 70,70,70
	Text (x+width/2),(y+height/2-1), txt, True, True
	
	Color 0,0,0
	
	If Pushed And MouseHit1 Then Return True
End Function

Function Min#(a#,b#)
	If a < b Then Return a Else Return b
End Function

Function Max#(a#,b#)
	If a > b Then Return a Else Return b
End Function

Function f2s$(n#, count%)
	Return Left(n, Len(Int(n))+count+1)
End Function

Function CurveValue#(number#, old#, smooth#)
	If number < old Then
		Return Max(old + (number - old) * (1.0 / smooth), number)
	Else
		Return Min(old + (number - old) * (1.0 / smooth), number)
	EndIf
End Function

Function SlideBar#(x%, y%, width%, value#, min_p%=0, max_p%=100)
	If MouseDown1 And OnSliderID=0
		If MouseX() >= x And MouseX() <= x + width + 14 And MouseY() >= y And MouseY() <= y + 20 Then
			value = Min(Max((MouseX() - x) * 100 / width, 0), 100)
		EndIf
	EndIf
	
	Color 255,255,255
	Rect(x, y, width + 14, 20,False)
	
	Color 200,200,240
	Rect x+width*value/100.0+3,y+3,8,14,True
	
	Color 140,140,140 
	Text (x - 23, y + 4, min_p+"%")				
	Text (x + width + 20, y+4, max_p+"%")	
	
	Return value
End Function

Function GetTime$(numb%)
	Local secs%,mins%,secstring$,minstring$
	
	If numb<60
		If Len(Int(Max(numb,0)))<2
			secstring$ = "0"+Int(Max(numb,0))
		Else
			secstring$ = Int(Max(numb,0))
		EndIf
		Return "00:"+secstring$
	Else
		mins% = Int(numb%/60)
		secs = numb-(mins%*60)
		If Len(Int(Max(mins%,0)))<2
			minstring$ = "0"+Int(Max(mins%,0))
		Else
			minstring$ = Int(Max(mins%,0))
		EndIf
		If Len(Int(Max(secs%,0)))<2
			secstring$ = "0"+Int(Max(secs%,0))
		Else
			secstring$ = Int(Max(secs%,0))
		EndIf
		Return minstring$+":"+secstring$
	EndIf
	
End Function

Function ShortLine$(txt$,amount%)
	
	If Len(txt$)>(amount%)
		Return Left(txt$,(amount%-3))+"..."
	EndIf
	
	Return txt$
	
End Function

Function UpdateMusic()
	
	If CurrMusic > -1
		If CurrMusic <> MusicPlaying Then
			If IsMusicPlaying=1
				alSourceStop(MusicCHN)
				alFreeSource(MusicCHN)
			EndIf
			MusicPlaying = CurrMusic
			MusicCHN = 0
			IsMusicPlaying=0
		EndIf
		
		If IsMusicPlaying=0
			If MusicPlaying < MusicAmount
				MusicCHN% = alCreateSource_(MusicPath+Music(MusicPlaying),True,False)
			Else
				MusicCHN% = alCreateSource_(MusicPath2+Music(MusicPlaying),True,False)
			EndIf
			alSourcePlay2D_(MusicCHN,True)
			alSourceSetLoop(MusicCHN,True)
			IsMusicPlaying=1
		EndIf
		If alSourceIsPlaying(MusicCHN)
			alSourceSetVolume(MusicCHN, MusicVolume)
			alSourceSetPitch(MusicCHN, CurrPitch)
		EndIf
	EndIf
	
End Function





;~IDEal Editor Parameters:
;~F#E4#112#116#11A#11E#126#13A#156#160
;~C#Blitz3D