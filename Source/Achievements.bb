;achievement menu & messages by InnocentSam

Const MAXACHIEVEMENTS=37
Dim Achievements%(MAXACHIEVEMENTS)

Const Achv008%=0, Achv012%=1, Achv035%=2, Achv049%=3, Achv055=4,  Achv079%=5, Achv096%=6, Achv106%=7, Achv148%=8, Achv205=9
Const Achv294%=10, Achv372%=11, Achv420%=12, Achv427=13, Achv500%=14, Achv513%=15, Achv714%=16, Achv789%=17, Achv860%=18, Achv895%=19
Const Achv914%=20, Achv939%=21, Achv966%=22, Achv970=23, Achv1025%=24, Achv1048=25, Achv1123=26

Const AchvMaynard%=27, AchvHarp%=28, AchvSNAV%=29, AchvOmni%=30, AchvConsole%=31, AchvTesla%=32, AchvPD%=33

Const Achv1162% = 34, Achv1499% = 35

Const AchvKeter% = 36

Global UsedConsole

Global AchievementsMenu%
Global AchvMSGenabled% = GetINIInt("options.ini", "options", "achievement popup enabled")
Dim AchievementStrings$(MAXACHIEVEMENTS)
Dim AchievementDescs$(MAXACHIEVEMENTS)
Dim AchvIMG%(MAXACHIEVEMENTS)
For i = 0 To MAXACHIEVEMENTS-1
	Local loc2% = GetINISectionLocation("Data\achievementstrings.ini", "s"+Str(i))
	AchievementStrings(i) = GetINIString2("Data\achievementstrings.ini", loc2, "string1")
	AchievementDescs(i) = GetINIString2("Data\achievementstrings.ini", loc2, "AchvDesc")
	
	Local image$ = GetINIString2("Data\achievementstrings.ini", loc2, "image") 
	
	AchvIMG(i) = LoadImage_Strict("GFX\menu\achievements\"+image+".jpg")
	AchvIMG(i) = ResizeImage2(AchvIMG(i),ImageWidth(AchvIMG(i))*GraphicHeight/768.0,ImageHeight(AchvIMG(i))*GraphicHeight/768.0)
Next

Global AchvLocked = LoadImage_Strict("GFX\menu\achievements\achvlocked.jpg")
AchvLocked = ResizeImage2(AchvLocked,ImageWidth(AchvLocked)*GraphicHeight/768.0,ImageHeight(AchvLocked)*GraphicHeight/768.0)

Function GiveAchievement(achvname%, showMessage%=True)
	If Achievements(achvname)<>True Then
		Achievements(achvname)=True
		If AchvMSGenabled And showMessage Then
			Local loc2% = GetINISectionLocation("Data\achievementstrings.ini", "s"+achvname)
			Local AchievementName$ = GetINIString2("Data\achievementstrings.ini", loc2, "string1")
			;Msg = "Achievement Unlocked - "+AchievementName
			;MsgTimer=70*7
			CreateAchievementMsg(achvname,AchievementName)
		EndIf
	EndIf
End Function

Function AchievementTooltip(achvno%)
    Local scale# = GraphicHeight/768.0
    
    AASetFont Font3
    Local width = AAStringWidth(AchievementStrings(achvno))
    AASetFont Font1
    If (AAStringWidth(AchievementDescs(achvno))>width) Then
        width = AAStringWidth(AchievementDescs(achvno))
    EndIf
    width = width+20*MenuScale
    
    Local height = 38*scale
    
    Color 25,25,25
    Rect(ScaledMouseX()+(20*MenuScale),ScaledMouseY()+(20*MenuScale),width,height,True)
    Color 150,150,150
    Rect(ScaledMouseX()+(20*MenuScale),ScaledMouseY()+(20*MenuScale),width,height,False)
    AASetFont Font3
    AAText(ScaledMouseX()+(20*MenuScale)+(width/2),ScaledMouseY()+(35*MenuScale), AchievementStrings(achvno), True, True)
    AASetFont Font1
    AAText(ScaledMouseX()+(20*MenuScale)+(width/2),ScaledMouseY()+(55*MenuScale), AchievementDescs(achvno), True, True)
End Function

Function DrawAchvIMG(x%, y%, achvno%)
	Local row%
	Local scale# = GraphicHeight/768.0
	Local SeparationConst2 = 76 * scale
;	If achvno >= 0 And achvno < 4 Then 
;		row = achvno
;	ElseIf achvno >= 3 And achvno <= 6 Then
;		row = achvno-3
;	ElseIf achvno >= 7 And achvno <= 10 Then
;		row = achvno-7
;	ElseIf achvno >= 11 And achvno <= 14 Then
;		row = achvno-11
;	ElseIf achvno >= 15 And achvno <= 18 Then
;		row = achvno-15
;	ElseIf achvno >= 19 And achvno <= 22 Then
;		row = achvno-19
;	ElseIf achvno >= 24 And achvno <= 26 Then
;		row = achvno-24
;	EndIf
	row = achvno Mod 4
	Color 0,0,0
	Rect((x+((row)*SeparationConst2)), y, 64*scale, 64*scale, True)
	If Achievements(achvno) = True Then
		DrawImage(AchvIMG(achvno),(x+(row*SeparationConst2)),y)
	Else
		DrawImage(AchvLocked,(x+(row*SeparationConst2)),y)
	EndIf
	Color 50,50,50
	
	Rect((x+(row*SeparationConst2)), y, 64*scale, 64*scale, False)
End Function

Global CurrAchvMSGID% = 0

Type AchievementMsg
	Field achvID%
	Field txt$
	Field msgx#
	Field msgtime#
	Field msgID%
End Type

Function CreateAchievementMsg.AchievementMsg(id%,txt$)
	Local amsg.AchievementMsg = New AchievementMsg
	
	amsg\achvID = id
	amsg\txt = txt
	amsg\msgx = 0.0
	amsg\msgtime = FPSfactor2
	amsg\msgID = CurrAchvMSGID
	CurrAchvMSGID = CurrAchvMSGID + 1
	
	Return amsg
End Function

Function UpdateAchievementMsg()
	Local amsg.AchievementMsg,amsg2.AchievementMsg
	Local scale# = GraphicHeight/768.0
	Local width% = 264*scale
	Local height% = 84*scale
	Local x%,y%
	
	For amsg = Each AchievementMsg
		If amsg\msgtime <> 0
			x=GraphicWidth+amsg\msgx
			y=(GraphicHeight-height)
			For amsg2 = Each AchievementMsg
				If amsg2 <> amsg
					If amsg2\msgID > amsg\msgID
						y=y-height
					EndIf
				EndIf
			Next
			DrawFrame(x,y,width,height)
			Color 0,0,0
			Rect(x+10*scale,y+10*scale,64*scale,64*scale,True)
			DrawImage(AchvIMG(amsg\achvID),x+10*scale,y+10*scale)
			Color 50,50,50
			Rect(x+10*scale,y+10*scale,64*scale,64*scale,False)
			Color 255,255,255
			AASetFont Font1
			RowText("Achievement Unlocked - "+amsg\txt,x+84*scale,y+10*scale,width-94*scale,y-20*scale)
			If amsg\msgtime > 0.0 And amsg\msgtime < 70*7
				amsg\msgtime = amsg\msgtime + FPSfactor2
				If amsg\msgx > -width%
					amsg\msgx = Max(amsg\msgx-4*FPSfactor2,-width%)
				EndIf
			ElseIf amsg\msgtime >= 70*7
				amsg\msgtime = -1
			ElseIf amsg\msgtime = -1
				If amsg\msgx < 0.0
					amsg\msgx = Min(amsg\msgx+4*FPSfactor2,0.0)
				Else
					amsg\msgtime = 0.0
				EndIf
			EndIf
		Else
			Delete amsg
		EndIf
	Next
	
End Function




;~IDEal Editor Parameters:
;~F#31#48
;~C#Blitz3D