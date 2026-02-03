; Project Mirror - UI System
; HUD elements, sanity meter, act titles, objective markers

; === UI STATES ===
Global MirrorHUDVisible% = True
Global MirrorHUDAlpha# = 1.0

; sanity meter
Global SanityMeterX% = 20
Global SanityMeterY% = 100
Global SanityMeterW% = 150
Global SanityMeterH% = 15
Global SanityMeterPulse# = 0.0

; act title
Global ActTitleVisible% = False
Global ActTitleText$ = ""
Global ActTitleSubtext$ = ""
Global ActTitleTimer# = 0.0
Global ActTitleFade# = 0.0

; objective
Global ObjectiveVisible% = False
Global ObjectiveText$ = ""
Global ObjectiveSubtext$ = ""
Global ObjectiveTimer# = 0.0

; notification
Dim NotificationQueue$(8)
Dim NotificationTimers#(8)
Global NotificationCount% = 0

; interaction prompt
Global InteractionPromptVisible% = False
Global InteractionPromptText$ = ""
Global InteractionPromptKey$ = "E"

; radio message
Global RadioMessageActive% = False
Global RadioMessageSpeaker$ = ""
Global RadioMessageText$ = ""
Global RadioMessageTimer# = 0.0
Global RadioStaticAlpha# = 0.0

; compass / minimap
Global CompassEnabled% = True
Global CompassY% = 50

; GPS Line system (3D path markers)
Const GPS_MAX_MARKERS% = 20
Global GPSMarkers%[GPS_MAX_MARKERS]
Global GPSMarkersActive% = False
Global GPSUpdateTimer# = 0.0

; karma indicator
Global KarmaChangeVisible% = False
Global KarmaChangeAmount% = 0
Global KarmaChangeTimer# = 0.0

Function InitMirrorUI()
	; reset all UI states
	MirrorHUDVisible = True
	MirrorHUDAlpha = 1.0

	ActTitleVisible = False
	ObjectiveVisible = False
	NotificationCount = 0
	InteractionPromptVisible = False
	RadioMessageActive = False
	KarmaChangeVisible = False

	For i% = 0 To 7
		NotificationQueue(i) = ""
		NotificationTimers(i) = 0.0
	Next

	DebugLog "Mirror UI initialized"
End Function

Function UpdateMirrorUI()
	; sanity meter pulse
	If PlayerSanity > SANITY_ANXIETY Then
		SanityMeterPulse = SanityMeterPulse + FPSfactor * 0.1
	EndIf

	; act title fade
	If ActTitleVisible Then
		ActTitleTimer = ActTitleTimer + FPSfactor

		If ActTitleTimer < 70.0 Then
			ActTitleFade = ActTitleTimer / 70.0
		ElseIf ActTitleTimer < 280.0 Then
			ActTitleFade = 1.0
		ElseIf ActTitleTimer < 350.0 Then
			ActTitleFade = 1.0 - (ActTitleTimer - 280.0) / 70.0
		Else
			ActTitleVisible = False
			ActTitleFade = 0.0
		EndIf
	EndIf

	; objective timer
	If ObjectiveVisible Then
		ObjectiveTimer = ObjectiveTimer + FPSfactor
		If ObjectiveTimer > 350.0 Then
			ObjectiveVisible = False
		EndIf
	EndIf

	; notifications
	UpdateNotifications()

	; radio message
	If RadioMessageActive Then
		RadioMessageTimer = RadioMessageTimer + FPSfactor
		RadioStaticAlpha = Sin(RadioMessageTimer * 0.5) * 0.3 + 0.2

		If RadioMessageTimer > 350.0 Then
			RadioMessageActive = False
		EndIf
	EndIf

	; karma indicator
	If KarmaChangeVisible Then
		KarmaChangeTimer = KarmaChangeTimer + FPSfactor
		If KarmaChangeTimer > 140.0 Then
			KarmaChangeVisible = False
		EndIf
	EndIf
End Function

Function RenderMirrorUI()
	If Not MirrorHUDVisible Then Return

	; Reset font to default game font to avoid ESC menu issues
	AASetFont Font1

	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; === SANITY METER ===
	RenderSanityMeter(gw, gh)

	; === DAY & ACT INDICATOR ===
	RenderDayIndicator(gw, gh)

	; === KARMA ===
	RenderKarmaIndicator(gw, gh)

	; === ACT TITLE ===
	If ActTitleVisible Then
		RenderActTitle(gw, gh)
	EndIf

	; === OBJECTIVE ===
	If ObjectiveVisible Then
		RenderObjective(gw, gh)
	EndIf

	; === NOTIFICATIONS ===
	RenderNotifications(gw, gh)

	; === INTERACTION PROMPT ===
	If InteractionPromptVisible Then
		RenderInteractionPrompt(gw, gh)
	EndIf

	; === RADIO MESSAGE ===
	If RadioMessageActive Then
		RenderRadioMessage(gw, gh)
	EndIf

	; === NAVIGATION ARROW ONLY (no compass) ===
	If CompassEnabled Then
		; Removed compass (S W E N) - only show navigation arrow
		RenderNavigationArrow(gw, gh)
	EndIf
End Function

; ============================================================================
; SANITY METER
; ============================================================================

Function RenderSanityMeter(gw%, gh%)
	Local x% = SanityMeterX
	Local y% = SanityMeterY
	Local w% = SanityMeterW
	Local h% = SanityMeterH

	; fon
	Color 20, 20, 20
	Rect x - 2, y - 2, w + 4, h + 4, True

	; ramka
	Color 60, 60, 60
	Rect x - 2, y - 2, w + 4, h + 4, False

	; zapolneniye
	Local fillW% = Int((Float(PlayerSanity) / Float(SANITY_MAX)) * w)
	Local level% = GetSanityLevel()

	; tsvet v zavisimosti ot urovnya
	Select level
		Case 0  ; normal
			Color 50, 150, 50
		Case 1  ; trevoga
			Color 150, 150, 50
		Case 2  ; paranoya
			Color 200, 100, 50
		Case 3  ; isteriya
			; pulsiruyushchiy krasnyi
			Local pulse% = Int(Sin(SanityMeterPulse) * 50 + 200)
			Color pulse, 30, 30
	End Select

	Rect x, y, fillW, h, True

	; tekst
	Color 200, 200, 200
	Local label$ = "SANITY"
	Text x, y - 15, label

	; protsent
	Local pct$ = PlayerSanity + "%"
	Text x + w - StringWidth(pct), y - 15, pct

	; uroven'
	Local levelName$ = ""
	Select level
		Case 0: levelName = "NORMAL"
		Case 1: levelName = "ANXIETY"
		Case 2: levelName = "PARANOIA"
		Case 3: levelName = "HYSTERIA"
	End Select

	If level > 0 Then
		Select level
			Case 1: Color 150, 150, 50
			Case 2: Color 200, 100, 50
			Case 3: Color 255, 50, 50
		End Select
		Text x, y + h + 3, levelName
	EndIf
End Function

; ============================================================================
; DAY & ACT INDICATOR
; ============================================================================

Function RenderDayIndicator(gw%, gh%)
	Local x% = gw - 120
	Local y% = 20

	; den'
	Color 150, 150, 150
	Text x, y, "DAY " + CurrentDay

	; akt (tol'ko den' 3)
	If CurrentDay = 3 And CurrentAct > 0 Then
		Color 100, 100, 100
		Local actName$ = GetActName(CurrentAct)
		Text x, y + 18, actName
	EndIf
End Function

; ============================================================================
; KARMA INDICATOR
; ============================================================================

Function RenderKarmaIndicator(gw%, gh%)
	Local x% = gw - 120
	Local y% = 60

	; tekushchaya karma
	Local karmaStr$ = ""
	If CurrentKarma >= 0 Then
		karmaStr = "+" + CurrentKarma
		Color 100, 200, 100
	Else
		karmaStr = CurrentKarma
		Color 200, 100, 100
	EndIf

	Text x, y, "KARMA: " + karmaStr

	; izmeneniye karmy
	If KarmaChangeVisible Then
		Local changeY% = y + 18 - Int(KarmaChangeTimer * 0.3)
		Local alpha% = Int(255 - KarmaChangeTimer * 1.8)
		If alpha < 0 Then alpha = 0

		If KarmaChangeAmount > 0 Then
			Color 100, 200, 100
			Text x + 50, changeY, "+" + KarmaChangeAmount
		ElseIf KarmaChangeAmount < 0 Then
			Color 200, 100, 100
			Text x + 50, changeY, KarmaChangeAmount
		EndIf
	EndIf
End Function

; ============================================================================
; ACT TITLE
; ============================================================================

Function ShowActTitle(actNum%, title$, subtitle$)
	ActTitleVisible = True
	ActTitleText = title
	ActTitleSubtext = subtitle
	ActTitleTimer = 0.0
	ActTitleFade = 0.0
End Function

Function RenderActTitle(gw%, gh%)
	Local alpha% = Int(ActTitleFade * 255)
	If alpha <= 0 Then Return

	Local cx% = gw / 2
	Local cy% = gh / 3

	; zagolovok akta
	Color Int(ActTitleFade * 200), Int(ActTitleFade * 200), Int(ActTitleFade * 200)
	Local tw% = StringWidth(ActTitleText)
	Text cx - tw / 2, cy, ActTitleText

	; podpis'
	If ActTitleSubtext <> "" Then
		Color Int(ActTitleFade * 130), Int(ActTitleFade * 130), Int(ActTitleFade * 130)
		tw = StringWidth(ActTitleSubtext)
		Text cx - tw / 2, cy + 30, ActTitleSubtext
	EndIf

	; dekorativnye linii
	Color Int(ActTitleFade * 80), Int(ActTitleFade * 80), Int(ActTitleFade * 80)
	Line cx - 150, cy - 15, cx + 150, cy - 15
	Line cx - 150, cy + 55, cx + 150, cy + 55
End Function

; ============================================================================
; OBJECTIVE
; ============================================================================

Function SetObjective(text$, subtext$ = "")
	ObjectiveVisible = True
	ObjectiveText = text
	ObjectiveSubtext = subtext
	ObjectiveTimer = 0.0

	AddNotification("New objective: " + text)
End Function

Function ClearObjective()
	ObjectiveVisible = False
	ObjectiveText = ""
	ObjectiveSubtext = ""
End Function

Function RenderObjective(gw%, gh%)
	Local x% = 20
	Local y% = gh - 100

	; fon
	Color 0, 0, 0
	Rect x - 5, y - 5, 250, 45, True

	; ramka
	Color 80, 80, 80
	Rect x - 5, y - 5, 250, 45, False

	; zagolovok
	Color 180, 150, 50
	Text x, y, "OBJECTIVE:"

	; tekst
	Color 200, 200, 200
	Text x, y + 18, ObjectiveText

	; podpis'
	If ObjectiveSubtext <> "" Then
		Color 120, 120, 120
		Text x, y + 33, ObjectiveSubtext
	EndIf
End Function

; ============================================================================
; NOTIFICATIONS
; ============================================================================

Function AddNotification(text$)
	If NotificationCount < 8 Then
		NotificationQueue(NotificationCount) = text
		NotificationTimers(NotificationCount) = 280.0  ; 4 sekundy
		NotificationCount = NotificationCount + 1
	EndIf
End Function

Function UpdateNotifications()
	Local i% = 0
	While i < NotificationCount
		NotificationTimers(i) = NotificationTimers(i) - FPSfactor
		If NotificationTimers(i) <= 0.0 Then
			; udalyaem
			For j% = i To NotificationCount - 2
				NotificationQueue(j) = NotificationQueue(j + 1)
				NotificationTimers(j) = NotificationTimers(j + 1)
			Next
			NotificationCount = NotificationCount - 1
		Else
			i = i + 1
		EndIf
	Wend
End Function

Function RenderNotifications(gw%, gh%)
	Local x% = gw / 2
	Local y% = 80

	For i% = 0 To NotificationCount - 1
		Local alpha# = NotificationTimers(i) / 280.0
		If alpha > 1.0 Then alpha = 1.0

		; fade in/out
		If NotificationTimers(i) > 210.0 Then
			alpha = (280.0 - NotificationTimers(i)) / 70.0
		ElseIf NotificationTimers(i) < 70.0 Then
			alpha = NotificationTimers(i) / 70.0
		EndIf

		Local gray% = Int(alpha * 200)
		Color gray, gray, gray

		Local tw% = StringWidth(NotificationQueue(i))
		Text x - tw / 2, y + i * 20, NotificationQueue(i)
	Next
End Function

; ============================================================================
; INTERACTION PROMPT
; ============================================================================

Function ShowInteractionPrompt(text$, key$ = "E")
	InteractionPromptVisible = True
	InteractionPromptText = text
	InteractionPromptKey = key
End Function

Function HideInteractionPrompt()
	InteractionPromptVisible = False
End Function

Function RenderInteractionPrompt(gw%, gh%)
	Local x% = gw / 2
	Local y% = gh / 2 + 50

	; fon
	Local promptStr$ = "[" + InteractionPromptKey + "] " + InteractionPromptText
	Local tw% = StringWidth(promptStr)

	Color 0, 0, 0
	Rect x - tw / 2 - 10, y - 5, tw + 20, 25, True

	; tekst
	Color 200, 200, 200
	Text x - tw / 2, y, promptStr
End Function

; ============================================================================
; RADIO MESSAGE
; ============================================================================

Function ShowRadioMessage(speaker$, text$)
	RadioMessageActive = True
	RadioMessageSpeaker = speaker
	RadioMessageText = text
	RadioMessageTimer = 0.0

	; play radio static
	PlaySound LoadSound("SFX\Radio\Static.ogg")
End Function

Function RenderRadioMessage(gw%, gh%)
	Local x% = gw / 2
	Local y% = gh - 150

	Local boxW% = 400
	Local boxH% = 60

	; fon s shoom
	Color 10, 10, 10
	Rect x - boxW / 2, y, boxW, boxH, True

	; static overlay
	If RadioStaticAlpha > 0.0 Then
		Local staticCount% = Int(RadioStaticAlpha * 100)
		For i% = 0 To staticCount
			Local sx% = x - boxW / 2 + Rand(0, boxW)
			Local sy% = y + Rand(0, boxH)
			Local gray% = Rand(20, 60)
			Color gray, gray, gray
			Plot sx, sy
		Next
	EndIf

	; ramka
	Color 60, 80, 60
	Rect x - boxW / 2, y, boxW, boxH, False

	; ikonka ratsii
	Color 80, 120, 80
	Text x - boxW / 2 + 10, y + 5, "[RADIO]"

	; speaker
	Color 150, 180, 150
	Text x - boxW / 2 + 80, y + 5, RadioMessageSpeaker

	; tekst
	Color 180, 200, 180
	Text x - boxW / 2 + 10, y + 25, RadioMessageText
End Function

; ============================================================================
; COMPASS
; ============================================================================

Function RenderCompass(gw%, gh%)
	Local x% = gw / 2
	Local y% = CompassY

	Local playerYaw# = EntityYaw(Collider)

	; fon
	Color 0, 0, 0
	Rect x - 100, y - 10, 200, 20, True

	; napravleniya
	For i% = 0 To 3
		Local dirLabel$
		Select i
			Case 0: dirLabel = "N"
			Case 1: dirLabel = "E"
			Case 2: dirLabel = "S"
			Case 3: dirLabel = "W"
		End Select

		Local angle# = i * 90.0
		Local relAngle# = angle - playerYaw

		; normalizatsiya
		While relAngle > 180.0
			relAngle = relAngle - 360.0
		Wend
		While relAngle < -180.0
			relAngle = relAngle + 360.0
		Wend

		; pozitsiya na kompase
		Local compassX% = x + Int(relAngle * 1.0)

		If compassX > x - 90 And compassX < x + 90 Then
			If i = 0 Then
				Color 200, 50, 50  ; sever - krasnyi
			Else
				Color 150, 150, 150
			EndIf
			Text compassX - 4, y - 8, dirLabel
		EndIf
	Next

	; tsentral'naya metka
	Color 255, 255, 255
	Line x, y - 12, x, y + 8
End Function

; ============================================================================
; NAVIGATION ARROW - Shows direction to objective
; ============================================================================

Function RenderNavigationArrow(gw%, gh%)
	If Not NavigationActive Then Return

	; Update 3D GPS line
	UpdateGPSLine()

	; Also show small HUD indicator with distance
	Local x% = gw - 60
	Local y% = 100

	Local dist# = GetNavigationDistance()

	; Small HUD box
	Color 0, 0, 0
	Rect x - 30, y - 15, 60, 50, True
	Color 0, 200, 100
	Rect x - 30, y - 15, 60, 50, False

	; GPS text
	Color 0, 255, 100
	Text x - StringWidth("GPS") / 2, y - 12, "GPS"

	; Distance
	Color 200, 200, 200
	Local distStr$ = Int(dist) + "m"
	Text x - StringWidth(distStr) / 2, y + 5, distStr

	; Target name
	If NavigationTargetName <> "" Then
		Color 0, 200, 100
		Local name$ = NavigationTargetName
		If Len(name) > 6 Then name = Left(name, 6) + ".."
		Text x - StringWidth(name) / 2, y + 20, name
	EndIf
End Function

; Create/Update GPS line markers on the ground
Function UpdateGPSLine()
	If Not NavigationActive Then
		HideGPSMarkers()
		Return
	EndIf

	GPSUpdateTimer = GPSUpdateTimer + FPSfactor
	If GPSUpdateTimer < 5.0 Then Return  ; Update every few frames
	GPSUpdateTimer = 0.0

	Local px# = EntityX(Collider)
	Local py# = EntityY(Collider) - 0.3  ; Slightly below player (on floor)
	Local pz# = EntityZ(Collider)

	Local tx# = NavigationTargetX
	Local tz# = NavigationTargetZ

	Local dist# = Sqr((tx - px) * (tx - px) + (tz - pz) * (tz - pz))

	; Number of markers based on distance
	Local numMarkers% = Min(Int(dist / 2.0), GPS_MAX_MARKERS)
	If numMarkers < 2 Then numMarkers = 2

	; Create markers if needed
	If Not GPSMarkersActive Then
		CreateGPSMarkers()
	EndIf

	; Position markers along the path
	Local i%
	For i = 0 To GPS_MAX_MARKERS - 1
		If i < numMarkers Then
			Local t# = Float(i) / Float(numMarkers - 1)
			Local mx# = px + (tx - px) * t
			Local mz# = pz + (tz - pz) * t

			If GPSMarkers[i] <> 0 Then
				PositionEntity GPSMarkers[i], mx, py, mz
				ShowEntity GPSMarkers[i]
			EndIf
		Else
			If GPSMarkers[i] <> 0 Then
				HideEntity GPSMarkers[i]
			EndIf
		EndIf
	Next
End Function

Function CreateGPSMarkers()
	Local i%
	For i = 0 To GPS_MAX_MARKERS - 1
		If GPSMarkers[i] = 0 Then
			; Create a small green sprite as GPS marker
			GPSMarkers[i] = CreateSprite()
			ScaleSprite GPSMarkers[i], 0.1, 0.1
			EntityColor GPSMarkers[i], 0, 255, 100
			EntityAlpha GPSMarkers[i], 0.7
			EntityFX GPSMarkers[i], 1 + 8  ; Full bright + no fog
			HideEntity GPSMarkers[i]
		EndIf
	Next
	GPSMarkersActive = True
End Function

Function HideGPSMarkers()
	Local i%
	For i = 0 To GPS_MAX_MARKERS - 1
		If GPSMarkers[i] <> 0 Then
			HideEntity GPSMarkers[i]
		EndIf
	Next
End Function

Function FreeGPSMarkers()
	Local i%
	For i = 0 To GPS_MAX_MARKERS - 1
		If GPSMarkers[i] <> 0 Then
			FreeEntity GPSMarkers[i]
			GPSMarkers[i] = 0
		EndIf
	Next
	GPSMarkersActive = False
End Function

; ============================================================================
; KARMA CHANGE ANIMATION
; ============================================================================

Function ShowKarmaChange(amount%)
	KarmaChangeVisible = True
	KarmaChangeAmount = amount
	KarmaChangeTimer = 0.0
End Function

; ============================================================================
; CLEANUP
; ============================================================================

Function CleanupMirrorUI()
	ActTitleVisible = False
	ObjectiveVisible = False
	NotificationCount = 0
	RadioMessageActive = False
	InteractionPromptVisible = False
End Function
