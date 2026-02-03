; Project Mirror - Story System
; Маркус = охранник, не ученый. День 1 = рутина до катастрофы

Const MAX_DIALOG_OPTIONS% = 6
Const MAX_STORY_FLAGS% = 64
Const MAX_DIALOG_NODES% = 256

; ветки сюжета
Const STORY_BRANCH_NEUTRAL% = 0
Const STORY_BRANCH_REDEMPTION% = 1
Const STORY_BRANCH_CHAOS% = 2
Const STORY_BRANCH_SACRIFICE% = 3

Const KARMA_MIN% = -100
Const KARMA_MAX% = 100

Global CurrentDay% = 1
Global CurrentKarma% = 0
Global StoryBranch% = STORY_BRANCH_NEUTRAL
Global CanPlayerMove% = True
Global DialogActive% = False
Global CurrentDialogNode.DialogNode = Null

Dim StoryFlags%(MAX_STORY_FLAGS)

; флаги сюжета - индексы
Const FLAG_STEVE_MET% = 0
Const FLAG_STEVE_SAVED% = 1
Const FLAG_STEVE_DEAD% = 2
Const FLAG_HARRISON_PDA% = 3
Const FLAG_HARRISON_EYE% = 4
Const FLAG_049_CURED% = 5
Const FLAG_106_CONTAINED% = 6
Const FLAG_GUARD_SPARED% = 7
Const FLAG_SCIENTIST_HELPED% = 8
Const FLAG_MTF_CONTACTED% = 9
Const FLAG_CHAOS_ALLIED% = 10
Const FLAG_939_ENCOUNTER% = 11
Const FLAG_GATE_A_UNLOCKED% = 12
Const FLAG_GATE_B_UNLOCKED% = 13
Const FLAG_O5_CARD_OBTAINED% = 14
Const FLAG_WARHEAD_ARMED% = 15
Const FLAG_FINALE_TRIGGERED% = 16
Const FLAG_ECHO_STEVE_SEEN% = 17
Const FLAG_ECHO_GUARD_SEEN% = 18
Const FLAG_ECHO_SCIENTIST_SEEN% = 19
; Day 1 флаги
Const FLAG_COFFEE_WITH_STEVE% = 20
Const FLAG_SAW_HELICOPTERS% = 21
Const FLAG_ESCORTED_DCLASS% = 22
Const FLAG_SAW_999% = 23
Const FLAG_HEARD_173_RUMORS% = 24
Const FLAG_BREACH_STARTED% = 25

; Day 2 флаги - "Protocol & Premonition"
Const FLAG_DAY2_STARTED% = 26
Const FLAG_MET_CONVOY% = 27
Const FLAG_SAW_D9341% = 28
Const FLAG_AT_173_CHAMBER% = 29
Const FLAG_WITNESSED_PROCEDURE% = 30
Const FLAG_LIGHTS_FLICKERED% = 31
Const FLAG_PROCEDURE_COMPLETE% = 32
Const FLAG_SAW_HARRISON_TERMINAL% = 33
Const FLAG_079_INTEGRATION% = 34
Const FLAG_DAY2_COMPLETE% = 35

; Day 3 флаги - "Catastrophe" (7 актов)
Const FLAG_DAY3_STARTED% = 36
Const FLAG_FOUND_STEVE_BODY% = 37
Const FLAG_FOUND_HARRISON_BODY% = 38
Const FLAG_COLLECTED_HARRISON_PDA% = 39
Const FLAG_COLLECTED_KEYCARD4% = 40
Const FLAG_HEARD_939_MIMIC% = 41
Const FLAG_SAW_ECHO_STEVE% = 42
Const FLAG_EMERGENCY_LIGHTING% = 43
Const FLAG_SAW_173_AFTERMATH% = 44
Const FLAG_DORMS_VISITED% = 45

; ACT 1: Пробуждение
Const FLAG_ACT1_PHANTOM_STEVE% = 46
Const FLAG_ACT1_RADIO_LOOP% = 47
Const FLAG_ACT1_TOOK_CIGARETTES% = 48

; ACT 2: Эхо прошлого
Const FLAG_ACT2_CAFETERIA_VISION% = 49
Const FLAG_ACT2_FLASHBACK_173% = 50
Const FLAG_ACT2_FOUND_DICTAPHONE% = 51
Const FLAG_ACT2_HEARD_STEVE_LAST% = 52

; ACT 3: Голоса друзей (939)
Const FLAG_ACT3_ENTERED_939_ZONE% = 53
Const FLAG_ACT3_939_CHASE% = 54
Const FLAG_ACT3_TOOK_HARRISON_EYE% = 55
Const FLAG_ACT3_READ_MIRROR_LOG% = 56

; ACT 4: Машина и Чума (914/049)
Const FLAG_ACT4_079_CONTACT% = 57
Const FLAG_ACT4_049_ENCOUNTER% = 58
Const FLAG_ACT4_UPGRADED_CARD% = 59
Const FLAG_ACT4_ZOMBIE_SIEGE% = 60

; ACT 5: Смотри в пол (096)
Const FLAG_ACT5_096_CORRIDOR% = 61
Const FLAG_ACT5_079_TROLLED% = 62
Const FLAG_ACT5_ELEVATOR_ESCAPE% = 63

; ACT 6: Поверхность
Const FLAG_ACT6_REACHED_SURFACE% = 64
Const FLAG_ACT6_MTF_BETRAYAL% = 65

; ACT 7: Финал / Концовки
Const FLAG_ENDING_WHISTLEBLOWER% = 66
Const FLAG_ENDING_SYMBIOSIS% = 67
Const FLAG_ENDING_DEATH% = 68
Const FLAG_ENDING_ZERO_PROTOCOL% = 69
Const FLAG_NUKE_ACTIVATED% = 70
Const FLAG_USED_STEVE_BADGE% = 71

; Sanity system
Const SANITY_MAX% = 100
Const SANITY_ANXIETY% = 30
Const SANITY_PARANOIA% = 70
Const SANITY_HYSTERIA% = 100

Global PlayerSanity% = 0
Global SanityEffectTimer# = 0.0
Global SanityHallucinationActive% = False
Global SanityPhantomVisible% = False

; Day 3 Act tracking
Const ACT_AWAKENING% = 1
Const ACT_ECHO% = 2
Const ACT_VOICES% = 3
Const ACT_MACHINE% = 4
Const ACT_FLOOR% = 5
Const ACT_SURFACE% = 6
Const ACT_FINALE% = 7

Global CurrentAct% = 0

Global DayTransitionPending% = False
Global DayTransitionTimer# = 0.0
Global DayTransitionFade# = 0.0

Global HasHarrisonPDA% = False
Global HasHarrisonEye% = False

; протагонист - Маркус, охранник
Global PlayerName$ = "Markus"
Global PlayerRole$ = "Security Guard"
Global PlayerSecurityLevel% = 2

Type StoryState
	Field day%
	Field karma%
	Field branch%
	Field playTime#
	Field deathCount%
	Field checkpointRoom$
	Field checkpointX#
	Field checkpointY#
	Field checkpointZ#
End Type

Global GStoryState.StoryState = Null

Type DialogNode
	Field id%
	Field speakerName$
	Field text$
	Field portraitPath$
	Field portrait%
	Field voicePath$
	Field voiceChannel%
	Field autoAdvanceTime#
	Field typewriterPos%
	Field typewriterTimer#
	Field displayedText$
	Field optionCount%
	Field isTerminal%
End Type

Type DialogOption
	Field parentNode.DialogNode
	Field optionIndex%
	Field text$
	Field nextNodeID%
	Field karmaChange%
	Field flagToSet%
	Field flagValue%
	Field requiredFlag%
	Field requiredFlagValue%
	Field branchChange%
	Field visible%
End Type

Type DialogEventRecord
	Field day%
	Field roomName$
	Field roomX#, roomY#, roomZ#
	Field speakerName$
	Field soundPath$
	Field animationName$
	Field timestamp#
End Type

; триггеры для диалогов в комнатах
Type DialogTrigger
	Field roomName$
	Field dialogID%
	Field triggerRadius#
	Field oneShot%
	Field triggered%
	Field requiredDay%
	Field requiredFlag%
	Field requiredFlagValue%
End Type

Global DialogBoxTexture% = 0
Global DialogFont% = 0
Global DialogSelectedOption% = 0
Global DialogTypewriterSpeed# = 0.5  ; Faster dialog text (was 0.05)

Dim DialogOptions.DialogOption(MAX_DIALOG_OPTIONS)
Dim DialogNodeCache.DialogNode(MAX_DIALOG_NODES)

; === ДЕНЬ 2: КАТСЦЕНА 173 ===

; состояния катсцены
Const SCENE_INACTIVE% = 0
Const SCENE_WAITING_PLAYER% = 1
Const SCENE_INTRO% = 2
Const SCENE_DCLASS_ENTER% = 3
Const SCENE_ANNOUNCEMENT% = 4
Const SCENE_LIGHTS_FLICKER% = 5
Const SCENE_LIGHTS_RESTORE% = 6
Const SCENE_HARRISON_VOICE% = 7
Const SCENE_DCLASS_EXIT% = 8
Const SCENE_STEVE_RADIO% = 9
Const SCENE_COMPLETE% = 10

Type ContainmentScene
	Field state%
	Field timer#
	Field phase%
	Field room173%  ; Room pointer (stored as integer to avoid type ordering issues)
	Field playerPosition%
	Field lightsFlickering%
	Field flickerCount%
	Field flickerTimer#
	Field announcementPlayed%
	Field intercomChannel%
End Type

Global ActiveScene.ContainmentScene = Null

; NPC актёры для катсцены
Type SceneActor
	Field npc.NPCs
	Field role$
	Field targetX#, targetY#, targetZ#
	Field state%
	Field visible%
End Type

Global SceneSteve.SceneActor = Null
Global SceneGuard1.SceneActor = Null
Global SceneGuard2.SceneActor = Null
Global SceneDClass1.SceneActor = Null
Global SceneDClass2.SceneActor = Null
Global SceneDClass9341.SceneActor = Null

; звуки интро оригинала
Global IntroAnnouncementSFX% = 0
Global LightsFlickerSFX% = 0
Global IntercomSFX% = 0
Global MirrorAlarmSFX% = 0

; субтитры
Global SubtitleText$ = ""
Global SubtitleTimer# = 0.0
Global SubtitleSpeaker$ = ""

Function InitStorySystem()
	If GStoryState = Null Then
		GStoryState = New StoryState
		GStoryState\day = 1
		GStoryState\karma = 0
		GStoryState\branch = STORY_BRANCH_NEUTRAL
		GStoryState\playTime = 0.0
		GStoryState\deathCount = 0
		GStoryState\checkpointRoom = ""
	EndIf

	For i% = 0 To MAX_STORY_FLAGS - 1
		StoryFlags(i) = 0
	Next

	CurrentDay = GStoryState\day
	CurrentKarma = GStoryState\karma
	StoryBranch = GStoryState\branch

	DialogActive = False
	CurrentDialogNode = Null
	DialogSelectedOption = 0
	CanPlayerMove = True

	For i% = 0 To MAX_DIALOG_OPTIONS - 1
		DialogOptions(i) = Null
	Next

	HasHarrisonPDA = False
	HasHarrisonEye = False

	; инит диалогов
	SetupDay1Dialogs()
	SetupDay1Triggers()
	SetupDay2Dialogs()
	SetupDay2Triggers()
	SetupDay3Dialogs()
	SetupDay3Triggers()

	; звуки для катсцены
	IntroAnnouncementSFX = LoadSound("SFX\Room\Intro\Announcement.ogg")
	LightsFlickerSFX = LoadSound("SFX\General\LightFlicker.ogg")
	IntercomSFX = LoadSound("SFX\General\Intercom.ogg")

	; Initial navigation - talk to Steve first
	SetNavigationToRoom("room2cafeteria")
	NavigationTargetName = "Steve"
End Function

Function SetStoryFlag(flagIndex%, value% = 1)
	If flagIndex >= 0 And flagIndex < MAX_STORY_FLAGS Then
		StoryFlags(flagIndex) = value

		Select flagIndex
			Case FLAG_HARRISON_PDA
				HasHarrisonPDA = value
			Case FLAG_HARRISON_EYE
				HasHarrisonEye = value
		End Select
	EndIf
End Function

Function GetStoryFlag%(flagIndex%)
	If flagIndex >= 0 And flagIndex < MAX_STORY_FLAGS Then
		Return StoryFlags(flagIndex)
	EndIf
	Return 0
End Function

Function ToggleStoryFlag(flagIndex%)
	If flagIndex >= 0 And flagIndex < MAX_STORY_FLAGS Then
		StoryFlags(flagIndex) = 1 - StoryFlags(flagIndex)
	EndIf
End Function

Function ModifyKarma(amount%)
	CurrentKarma = CurrentKarma + amount

	If CurrentKarma < KARMA_MIN Then CurrentKarma = KARMA_MIN
	If CurrentKarma > KARMA_MAX Then CurrentKarma = KARMA_MAX

	If GStoryState <> Null Then
		GStoryState\karma = CurrentKarma
	EndIf

	UpdateStoryBranch()
End Function

Function UpdateStoryBranch()
	Local oldBranch% = StoryBranch

	If CurrentKarma >= 50 Then
		StoryBranch = STORY_BRANCH_REDEMPTION
	ElseIf CurrentKarma <= -50 Then
		StoryBranch = STORY_BRANCH_CHAOS
	ElseIf GetStoryFlag(FLAG_049_CURED) And GetStoryFlag(FLAG_STEVE_SAVED) Then
		StoryBranch = STORY_BRANCH_SACRIFICE
	Else
		StoryBranch = STORY_BRANCH_NEUTRAL
	EndIf

	If GStoryState <> Null Then
		GStoryState\branch = StoryBranch
	EndIf
End Function

Function GetKarmaLevel%()
	If CurrentKarma >= 75 Then Return 3
	If CurrentKarma >= 25 Then Return 2
	If CurrentKarma >= -25 Then Return 1
	If CurrentKarma >= -75 Then Return 0
	Return -1
End Function

Function TriggerDayTransition(newDay%)
	If newDay > 0 And newDay <= 3 And newDay > CurrentDay Then
		DayTransitionPending = True
		DayTransitionTimer = 0.0
		DayTransitionFade = 0.0

		CurrentDay = newDay
		If GStoryState <> Null Then
			GStoryState\day = newDay
		EndIf
	EndIf
End Function

Function UpdateDayTransition()
	If Not DayTransitionPending Then Return

	DayTransitionTimer = DayTransitionTimer + FPSfactor

	If DayTransitionTimer < 70.0 Then
		DayTransitionFade = DayTransitionTimer / 70.0
		CanPlayerMove = False
	ElseIf DayTransitionTimer < 210.0 Then
		DayTransitionFade = 1.0
	ElseIf DayTransitionTimer < 280.0 Then
		DayTransitionFade = 1.0 - ((DayTransitionTimer - 210.0) / 70.0)
	Else
		DayTransitionPending = False
		DayTransitionFade = 0.0
		CanPlayerMove = True
	EndIf
End Function

Function RenderDayTransition()
	If Not DayTransitionPending Then Return
	If DayTransitionFade <= 0.0 Then Return

	Color 0, 0, 0
	Local alpha% = Int(DayTransitionFade * 255.0)

	If DayTransitionFade > 0.5 Then
		Rect 0, 0, GraphicsWidth(), GraphicsHeight(), True

		If DayTransitionTimer >= 70.0 And DayTransitionTimer < 210.0 Then
			Color 200, 200, 200
			Local dayText$ = "DAY " + CurrentDay
			Local tw% = StringWidth(dayText)
			Text (GraphicsWidth() - tw) / 2, GraphicsHeight() / 2 - 20, dayText

			Local subtitle$ = ""
			Select CurrentDay
				Case 1
					subtitle = "ROUTINE"
				Case 2
					subtitle = "PROTOCOL"
				Case 3
					subtitle = "RECKONING"
			End Select

			Color 150, 150, 150
			tw = StringWidth(subtitle)
			Text (GraphicsWidth() - tw) / 2, GraphicsHeight() / 2 + 10, subtitle
		EndIf
	EndIf
End Function

Function CreateDialogNode.DialogNode(id%, speakerName$, text$, portraitPath$ = "", voicePath$ = "")
	Local node.DialogNode = New DialogNode

	node\id = id
	node\speakerName = speakerName
	node\text = text
	node\portraitPath = portraitPath
	node\voicePath = voicePath
	node\voiceChannel = 0
	node\autoAdvanceTime = 0.0
	node\typewriterPos = 0
	node\typewriterTimer = 0.0
	node\displayedText = ""
	node\optionCount = 0
	node\isTerminal = True

	If portraitPath <> "" Then
		node\portrait = LoadImage(portraitPath)
		If node\portrait <> 0 Then
			MaskImage node\portrait, 255, 0, 255
		EndIf
	EndIf

	If id >= 0 And id < MAX_DIALOG_NODES Then
		DialogNodeCache(id) = node
	EndIf

	Return node
End Function

Function AddDialogOption.DialogOption(node.DialogNode, text$, nextNodeID%, karmaChange% = 0, flagToSet% = -1, flagValue% = 1)
	If node = Null Then Return Null
	If node\optionCount >= MAX_DIALOG_OPTIONS Then Return Null

	Local opt.DialogOption = New DialogOption

	opt\parentNode = node
	opt\optionIndex = node\optionCount
	opt\text = text
	opt\nextNodeID = nextNodeID
	opt\karmaChange = karmaChange
	opt\flagToSet = flagToSet
	opt\flagValue = flagValue
	opt\requiredFlag = -1
	opt\requiredFlagValue = 0
	opt\branchChange = -1
	opt\visible = True

	DialogOptions(node\optionCount) = opt

	node\optionCount = node\optionCount + 1
	node\isTerminal = False

	Return opt
End Function

Function SetOptionRequirement(opt.DialogOption, flagIndex%, requiredValue% = 1)
	If opt <> Null Then
		opt\requiredFlag = flagIndex
		opt\requiredFlagValue = requiredValue
	EndIf
End Function

Function SetOptionBranchChange(opt.DialogOption, newBranch%)
	If opt <> Null Then
		opt\branchChange = newBranch
	EndIf
End Function

Function StartDialog(nodeID%)
	If nodeID < 0 Or nodeID >= MAX_DIALOG_NODES Then Return

	Local node.DialogNode = DialogNodeCache(nodeID)
	If node = Null Then Return

	CurrentDialogNode = node
	DialogActive = True
	CanPlayerMove = False
	DialogSelectedOption = 0

	node\typewriterPos = 0
	node\typewriterTimer = 0.0
	node\displayedText = ""

	For i% = 0 To MAX_DIALOG_OPTIONS - 1
		DialogOptions(i) = Null
	Next

	Local optIndex% = 0
	For opt.DialogOption = Each DialogOption
		If opt\parentNode = node Then
			opt\visible = True
			If opt\requiredFlag >= 0 Then
				If GetStoryFlag(opt\requiredFlag) <> opt\requiredFlagValue Then
					opt\visible = False
				EndIf
			EndIf

			If opt\visible Then
				DialogOptions(optIndex) = opt
				optIndex = optIndex + 1
			EndIf
		EndIf
	Next

	If node\voicePath <> "" Then
		Local snd% = LoadSound(node\voicePath)
		If snd <> 0 Then
			node\voiceChannel = PlaySound(snd)
		EndIf
	EndIf
End Function

Function UpdateDialog()
	If Not DialogActive Then Return
	If CurrentDialogNode = Null Then
		EndDialog()
		Return
	EndIf

	Local node.DialogNode = CurrentDialogNode

	node\typewriterTimer = node\typewriterTimer + FPSfactor * DialogTypewriterSpeed
	If node\typewriterTimer >= 1.0 Then
		node\typewriterTimer = node\typewriterTimer - 1.0
		If node\typewriterPos < Len(node\text) Then
			node\typewriterPos = node\typewriterPos + 1
			node\displayedText = Left$(node\text, node\typewriterPos)
		EndIf
	EndIf

	If KeyHit(200) Then
		DialogSelectedOption = DialogSelectedOption - 1
		If DialogSelectedOption < 0 Then
			DialogSelectedOption = CountVisibleOptions() - 1
		EndIf
		While DialogOptions(DialogSelectedOption) = Null And DialogSelectedOption > 0
			DialogSelectedOption = DialogSelectedOption - 1
		Wend
	EndIf

	If KeyHit(208) Then
		DialogSelectedOption = DialogSelectedOption + 1
		If DialogSelectedOption >= CountVisibleOptions() Then
			DialogSelectedOption = 0
		EndIf
		While DialogOptions(DialogSelectedOption) = Null And DialogSelectedOption < MAX_DIALOG_OPTIONS - 1
			DialogSelectedOption = DialogSelectedOption + 1
		Wend
	EndIf

	If KeyHit(57) Then
		If node\typewriterPos < Len(node\text) Then
			node\typewriterPos = Len(node\text)
			node\displayedText = node\text
		EndIf
	EndIf

	If KeyHit(28) Or KeyHit(18) Then
		If node\typewriterPos >= Len(node\text) Then
			SelectDialogOption()
		Else
			node\typewriterPos = Len(node\text)
			node\displayedText = node\text
		EndIf
	EndIf

	If node\isTerminal And node\autoAdvanceTime > 0.0 Then
		If node\typewriterPos >= Len(node\text) Then
			node\autoAdvanceTime = node\autoAdvanceTime - FPSfactor
			If node\autoAdvanceTime <= 0.0 Then
				EndDialog()
			EndIf
		EndIf
	EndIf
End Function

Function CountVisibleOptions%()
	Local count% = 0
	For i% = 0 To MAX_DIALOG_OPTIONS - 1
		If DialogOptions(i) <> Null Then
			count = count + 1
		EndIf
	Next

	If count = 0 Then count = 1
	Return count
End Function

Function SelectDialogOption()
	Local opt.DialogOption = DialogOptions(DialogSelectedOption)

	If opt <> Null Then
		If opt\karmaChange <> 0 Then
			ModifyKarma(opt\karmaChange)
		EndIf

		If opt\flagToSet >= 0 Then
			SetStoryFlag(opt\flagToSet, opt\flagValue)
		EndIf

		If opt\branchChange >= 0 Then
			StoryBranch = opt\branchChange
			If GStoryState <> Null Then
				GStoryState\branch = StoryBranch
			EndIf
		EndIf

		If opt\nextNodeID >= 0 Then
			StartDialog(opt\nextNodeID)
		Else
			EndDialog()
		EndIf
	Else
		EndDialog()
	EndIf
End Function

Function EndDialog()
	If CurrentDialogNode <> Null Then
		If CurrentDialogNode\voiceChannel <> 0 Then
			StopChannel CurrentDialogNode\voiceChannel
		EndIf
	EndIf

	DialogActive = False
	CurrentDialogNode = Null
	CanPlayerMove = True
	DialogSelectedOption = 0

	For i% = 0 To MAX_DIALOG_OPTIONS - 1
		DialogOptions(i) = Null
	Next

	; Update navigation based on story progress
	UpdateNavigationGuidance()
End Function

; ============================================================================
; NAVIGATION GUIDANCE - Steve tells Markus where to go
; ============================================================================

Function UpdateNavigationGuidance()
	; Day 1 navigation
	If CurrentDay = 1 Then
		If GetStoryFlag(FLAG_COFFEE_WITH_STEVE) = 0 Then
			; Start - talk to Steve
			SetNavigationToRoom("room2cafeteria")
			NavigationTargetName = "Steve"
		ElseIf GetStoryFlag(FLAG_COFFEE_WITH_STEVE) = 1 And GetStoryFlag(FLAG_SAW_HELICOPTERS) = 0 Then
			; After coffee - go to elevator, then helicopters
			SetNavigationToRoom("room2elevator")
			NavigationTargetName = "Elevator"
		ElseIf GetStoryFlag(FLAG_SAW_HELICOPTERS) = 1 And GetStoryFlag(FLAG_ESCORTED_DCLASS) = 0 Then
			; After helicopters - escort D-Class
			SetNavigationToRoom("room2closets")
			NavigationTargetName = "D-Class"
		ElseIf GetStoryFlag(FLAG_ESCORTED_DCLASS) = 1 And GetStoryFlag(FLAG_SAW_999) = 0 Then
			; Escort to 999
			SetNavigationToRoom("room2sl")
			NavigationTargetName = "SCP-999"
		ElseIf GetStoryFlag(FLAG_SAW_999) = 1 Then
			; Back to cafeteria
			SetNavigationToRoom("room2cafeteria")
			NavigationTargetName = "Cafeteria"
		EndIf
	EndIf

	; Day 2 navigation
	If CurrentDay = 2 Then
		If GetStoryFlag(FLAG_DAY2_STARTED) = 1 And GetStoryFlag(FLAG_AT_173_CHAMBER) = 0 Then
			; Go to 173 chamber
			SetNavigationToRoom("room173")
			NavigationTargetName = "SCP-173"
		ElseIf GetStoryFlag(FLAG_AT_173_CHAMBER) = 1 And GetStoryFlag(FLAG_PROCEDURE_COMPLETE) = 0 Then
			; Stay at 173
			SetNavigationToRoom("room173")
			NavigationTargetName = "173 Chamber"
		ElseIf GetStoryFlag(FLAG_PROCEDURE_COMPLETE) = 1 Then
			; Go to dorms
			SetNavigationToRoom("room2dorm")
			NavigationTargetName = "Dorms"
		EndIf
	EndIf

	; Day 3 navigation
	If CurrentDay = 3 Then
		Select CurrentAct
			Case ACT_AWAKENING
				SetNavigationToRoom("room173")
				NavigationTargetName = "173 Chamber"
			Case ACT_ECHO
				SetNavigationToRoom("room2cafeteria")
				NavigationTargetName = "Cafeteria"
			Case ACT_VOICES
				SetNavigationToRoom("room2storage")
				NavigationTargetName = "939 Zone"
			Case ACT_MACHINE
				SetNavigationToRoom("room914")
				NavigationTargetName = "SCP-914"
			Case ACT_FLOOR
				SetNavigationToRoom("room2elevator")
				NavigationTargetName = "Elevator"
			Case ACT_SURFACE
				SetNavigationToRoom("exit1")
				NavigationTargetName = "Gate B"
			Case ACT_FINALE
				SetNavigationToRoom("roomexitgatea")
				NavigationTargetName = "Gate A"
		End Select
	EndIf
End Function

Function RenderDialog()
	If Not DialogActive Then Return
	If CurrentDialogNode = Null Then Return

	; Reset font to avoid ESC menu issues
	AASetFont Font1

	Local node.DialogNode = CurrentDialogNode
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	Local boxWidth% = gw - 100
	Local boxHeight% = 200
	Local boxX% = 50
	Local boxY% = gh - boxHeight - 30

	Color 0, 0, 0
	Rect boxX, boxY, boxWidth, boxHeight, True

	Color 100, 100, 100
	Rect boxX, boxY, boxWidth, boxHeight, False
	Rect boxX + 1, boxY + 1, boxWidth - 2, boxHeight - 2, False

	Local textStartX% = boxX + 20
	If node\portrait <> 0 Then
		DrawImage node\portrait, boxX + 10, boxY + 10
		textStartX = boxX + 110
	EndIf

	Color 200, 180, 100
	Text textStartX, boxY + 15, node\speakerName

	Color 220, 220, 220
	Local textY% = boxY + 40
	Local maxWidth% = boxWidth - (textStartX - boxX) - 20

	DrawWrappedText(node\displayedText, textStartX, textY, maxWidth)

	If node\typewriterPos >= Len(node\text) Then
		Local optY% = boxY + boxHeight - 80
		Local optIndex% = 0

		For i% = 0 To MAX_DIALOG_OPTIONS - 1
			Local opt.DialogOption = DialogOptions(i)
			If opt <> Null Then
				If optIndex = DialogSelectedOption Then
					Color 255, 200, 50
					Text textStartX - 15, optY, ">"
				Else
					Color 180, 180, 180
				EndIf

				Text textStartX, optY, (optIndex + 1) + ". " + opt\text

				If opt\karmaChange > 0 Then
					Color 100, 200, 100
					Text textStartX + StringWidth((optIndex + 1) + ". " + opt\text) + 10, optY, "[+" + opt\karmaChange + "]"
				ElseIf opt\karmaChange < 0 Then
					Color 200, 100, 100
					Text textStartX + StringWidth((optIndex + 1) + ". " + opt\text) + 10, optY, "[" + opt\karmaChange + "]"
				EndIf

				optY = optY + 18
				optIndex = optIndex + 1
			EndIf
		Next

		If optIndex = 0 Then
			Color 150, 150, 150
			Text textStartX, optY, "[ENTER - continue]"
		EndIf
	Else
		Color 100, 100, 100
		Text textStartX, boxY + boxHeight - 25, "[SPACE - skip]"
	EndIf
End Function

Function DrawWrappedText(txt$, x%, y%, maxWidth%)
	Local words$ = txt
	Local line$ = ""
	Local lineY% = y
	Local spacePos%
	Local word$

	While Len(words) > 0
		spacePos = Instr(words, " ")
		If spacePos = 0 Then
			word = words
			words = ""
		Else
			word = Left$(words, spacePos - 1)
			words = Mid$(words, spacePos + 1)
		EndIf

		If StringWidth(line + word) > maxWidth Then
			Text x, lineY, line
			lineY = lineY + 16
			line = word + " "
		Else
			line = line + word + " "
		EndIf
	Wend

	If Len(line) > 0 Then
		Text x, lineY, line
	EndIf
End Function

Function RecordDialogEvent(room.Rooms, speakerName$, soundPath$, animName$ = "")
	If room = Null Then Return

	Local rec.DialogEventRecord = New DialogEventRecord
	rec\day = CurrentDay
	rec\roomName = room\RoomTemplate\Name
	rec\roomX = EntityX(room\obj)
	rec\roomY = EntityY(room\obj)
	rec\roomZ = EntityZ(room\obj)
	rec\speakerName = speakerName
	rec\soundPath = soundPath
	rec\animationName = animName
	rec\timestamp = MilliSecs()
End Function

Function GetDialogEventsForRoom.DialogEventRecord(room.Rooms, targetDay%)
	If room = Null Then Return Null

	For rec.DialogEventRecord = Each DialogEventRecord
		If rec\day = targetDay Then
			If rec\roomName = room\RoomTemplate\Name Then
				Return rec
			EndIf
		EndIf
	Next

	Return Null
End Function

; === ДЕНЬ 1: ДИАЛОГИ ===
; Маркус - охранник. Утро, рутина, потом все идет к черту

Function SetupDay1Dialogs()
	Local node.DialogNode
	Local opt.DialogOption

	; --- MORNING: COFFEE WITH STEVE ---
	; Steve is also a guard, Markus's partner

	node = CreateDialogNode(1, "Steve", "Hey, Markus. Those choppers kept me up again. Word is they're bringing in some big shot for testing.", "", "")
	opt = AddDialogOption(node, "Work is work, Steve.", 2, 0, FLAG_COFFEE_WITH_STEVE, 1)
	opt = AddDialogOption(node, "As long as they pay on time.", 3, -2, FLAG_COFFEE_WITH_STEVE, 1)
	opt = AddDialogOption(node, "Let's go take a look while the boss is away?", 4, 3, FLAG_COFFEE_WITH_STEVE, 1)

	node = CreateDialogNode(2, "Steve", "Yeah, philosopher. Alright, finish your coffee - convoy in an hour. D-Class won't escort themselves.", "", "")
	AddDialogOption(node, "[Nod]", -1, 0, -1, 0)

	node = CreateDialogNode(3, "Steve", "*chuckles* Cynic, aren't you, Markus. But I get it. This place... everything's weird here.", "", "")
	opt = AddDialogOption(node, "Stranger than you think.", 5, 0, -1, 0)
	opt = AddDialogOption(node, "Better not to know the details.", -1, 0, -1, 0)

	node = CreateDialogNode(4, "Steve", "Oh, feeling adventurous! Fine, but quick. If the captain catches us - I don't know you.", "", "")
	AddDialogOption(node, "Deal.", 6, 0, -1, 0)  ; Don't set FLAG_SAW_HELICOPTERS here!

	node = CreateDialogNode(5, "Steve", "...you know something, don't you? About these... SCP objects?", "", "")
	opt = AddDialogOption(node, "The less you know, the better you sleep.", -1, -1, -1, 0)
	opt = AddDialogOption(node, "I'll tell you someday. Not now.", -1, 2, -1, 0)

	; --- TIME TO GO ---

	node = CreateDialogNode(6, "Steve", "*finishes coffee* Alright, time to move. Convoy's in 20 minutes. Follow me to the elevator.", "", "")
	opt = AddDialogOption(node, "Lead the way.", 7, 0, -1, 0)
	opt = AddDialogOption(node, "Right behind you.", 7, 0, -1, 0)

	node = CreateDialogNode(7, "Steve", "*stands up* Stay close. These corridors can get confusing.", "", "")
	AddDialogOption(node, "[Follow Steve]", -1, 0, -1, 0)

	; --- CONVOY TO SCP-999 ---

	node = CreateDialogNode(10, "Steve", "Convoy 'Mercy'. Three D-Class. Taking them to Light Containment, to 999.", "", "")
	AddDialogOption(node, "999? That's the one that...", 11, 0, -1, 0)
	AddDialogOption(node, "Got it. Let's go.", 12, 0, -1, 0)

	node = CreateDialogNode(11, "Steve", "Yeah, the orange blob. Harmless. They even let D-Class... interact with it. Therapy, I guess.", "", "")
	opt = AddDialogOption(node, "Good to have something not scary here.", 12, 2, -1, 0)
	opt = AddDialogOption(node, "Still a weird place.", 12, 0, -1, 0)

	node = CreateDialogNode(12, "Steve", "*to D-Class* On your feet, gentlemen. Field trip's starting.", "", "")
	AddDialogOption(node, "[Start convoy]", -1, 0, FLAG_ESCORTED_DCLASS, 1)

	; --- AT SCP-999 ---

	node = CreateDialogNode(20, "Steve", "*watching 999 hug a D-Class* Look how happy they are. Maybe we don't just keep monsters here, huh?", "", "")
	opt = AddDialogOption(node, "Maybe. A rare bright moment.", 21, 3, FLAG_SAW_999, 1)
	opt = AddDialogOption(node, "Don't get soft. It's still an SCP.", 22, 0, FLAG_SAW_999, 1)
	opt = AddDialogOption(node, "*watch silently*", -1, 0, FLAG_SAW_999, 1)

	node = CreateDialogNode(21, "Steve", "Yeah... *pause* You know, sometimes I wonder - what's all this for? Then I see something like this, and... well, you get it.", "", "")
	AddDialogOption(node, "I get it.", -1, 1, -1, 0)

	node = CreateDialogNode(22, "Steve", "*sighs* You're right, of course. But even we need to see... something good sometimes.", "", "")
	AddDialogOption(node, "[Nod]", -1, 0, -1, 0)

	; --- EVENING: CAFETERIA ---

	node = CreateDialogNode(30, "Guard Johnson", "*whispers* Heard about 173? Word is there's a 'cleaning' tomorrow. They dragged someone in again...", "", "")
	opt = AddDialogOption(node, "Not our problem.", -1, -1, -1, 0)
	opt = AddDialogOption(node, "Poor bastards.", 31, 1, -1, 0)
	opt = AddDialogOption(node, "You didn't hear that, and neither did I.", -1, 0, FLAG_HEARD_173_RUMORS, 1)

	node = CreateDialogNode(31, "Guard Johnson", "Yeah... *looks around* Better keep quiet about it. Walls have ears around here.", "", "")
	AddDialogOption(node, "[Nod and leave]", -1, 0, FLAG_HEARD_173_RUMORS, 1)

	; --- NIGHT: ALARM ---
	; this dialog triggers when the breach starts

	node = CreateDialogNode(50, "[ALERT]", "ATTENTION. MASSIVE CONTAINMENT BREACH. ALL PERSONNEL - FOLLOW PROTOCOL 'OMEGA-7'.", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(51, "Steve", "*on radio* MARKUS! You alive?! 173 broke out! I'm at Gate B - get here!", "", "")
	opt = AddDialogOption(node, "Hold on, Steve! I'm coming!", -1, 2, FLAG_BREACH_STARTED, 1)
	opt = AddDialogOption(node, "Steve, leave without me! I'll find another way!", -1, 0, FLAG_BREACH_STARTED, 1)
End Function

Function SetupDay1Triggers()
	Local trig.DialogTrigger

	; kafeteriy - razgovor so Stivom
	trig = New DialogTrigger
	trig\roomName = "room2cafeteria"
	trig\dialogID = 1
	trig\triggerRadius = 3.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 1
	trig\requiredFlag = -1

	; room2toilets - slukhi pro 173
	trig = New DialogTrigger
	trig\roomName = "room2toilets"
	trig\dialogID = 30
	trig\triggerRadius = 2.5
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 1
	trig\requiredFlag = FLAG_ESCORTED_DCLASS
	trig\requiredFlagValue = 1
End Function

Function CheckDialogTriggers(room.Rooms)
	If room = Null Then Return
	If DialogActive Then Return

	Local roomName$ = ""
	If room\RoomTemplate <> Null Then
		roomName = room\RoomTemplate\Name
	EndIf

	For trig.DialogTrigger = Each DialogTrigger
		Local skipTrig% = False
		If trig\triggered And trig\oneShot Then skipTrig = True
		If trig\roomName <> roomName Then skipTrig = True
		If trig\requiredDay > 0 And trig\requiredDay <> CurrentDay Then skipTrig = True
		If trig\requiredFlag >= 0 Then
			If GetStoryFlag(trig\requiredFlag) <> trig\requiredFlagValue Then skipTrig = True
		EndIf

		If (Not skipTrig) Then
			; trigger!
			trig\triggered = True
			StartDialog(trig\dialogID)
			Return
		EndIf
	Next
End Function

Function TriggerBreachSequence()
	; nachalo proryva - den' 2
	SetStoryFlag(FLAG_BREACH_STARTED, 1)
	StartDialog(50)
End Function

Function SaveStoryState(file%)
	If GStoryState = Null Then Return

	WriteInt file, CurrentDay
	WriteInt file, CurrentKarma
	WriteInt file, StoryBranch
	WriteFloat file, GStoryState\playTime
	WriteInt file, GStoryState\deathCount
	WriteLine file, GStoryState\checkpointRoom
	WriteFloat file, GStoryState\checkpointX
	WriteFloat file, GStoryState\checkpointY
	WriteFloat file, GStoryState\checkpointZ

	For i% = 0 To MAX_STORY_FLAGS - 1
		WriteInt file, StoryFlags(i)
	Next

	Local recCount% = 0
	For rec.DialogEventRecord = Each DialogEventRecord
		recCount = recCount + 1
	Next
	WriteInt file, recCount

	For rec.DialogEventRecord = Each DialogEventRecord
		WriteInt file, rec\day
		WriteLine file, rec\roomName
		WriteFloat file, rec\roomX
		WriteFloat file, rec\roomY
		WriteFloat file, rec\roomZ
		WriteLine file, rec\speakerName
		WriteLine file, rec\soundPath
		WriteLine file, rec\animationName
		WriteFloat file, rec\timestamp
	Next

	; sohranyaem triggery
	Local trigCount% = 0
	For trig.DialogTrigger = Each DialogTrigger
		trigCount = trigCount + 1
	Next
	WriteInt file, trigCount

	For trig.DialogTrigger = Each DialogTrigger
		WriteLine file, trig\roomName
		WriteInt file, trig\dialogID
		WriteInt file, trig\triggered
	Next
End Function

Function LoadStoryState(file%)
	If GStoryState = Null Then
		GStoryState = New StoryState
	EndIf

	CurrentDay = ReadInt(file)
	CurrentKarma = ReadInt(file)
	StoryBranch = ReadInt(file)
	GStoryState\playTime = ReadFloat(file)
	GStoryState\deathCount = ReadInt(file)
	GStoryState\checkpointRoom = ReadLine(file)
	GStoryState\checkpointX = ReadFloat(file)
	GStoryState\checkpointY = ReadFloat(file)
	GStoryState\checkpointZ = ReadFloat(file)

	GStoryState\day = CurrentDay
	GStoryState\karma = CurrentKarma
	GStoryState\branch = StoryBranch

	For i% = 0 To MAX_STORY_FLAGS - 1
		StoryFlags(i) = ReadInt(file)
	Next

	HasHarrisonPDA = StoryFlags(FLAG_HARRISON_PDA)
	HasHarrisonEye = StoryFlags(FLAG_HARRISON_EYE)

	Local recCount% = ReadInt(file)
	For i% = 0 To recCount - 1
		Local rec.DialogEventRecord = New DialogEventRecord
		rec\day = ReadInt(file)
		rec\roomName = ReadLine(file)
		rec\roomX = ReadFloat(file)
		rec\roomY = ReadFloat(file)
		rec\roomZ = ReadFloat(file)
		rec\speakerName = ReadLine(file)
		rec\soundPath = ReadLine(file)
		rec\animationName = ReadLine(file)
		rec\timestamp = ReadFloat(file)
	Next

	Local trigCount% = ReadInt(file)
	For i% = 0 To trigCount - 1
		Local roomN$ = ReadLine(file)
		Local dID% = ReadInt(file)
		Local trig% = ReadInt(file)

		For tr.DialogTrigger = Each DialogTrigger
			If tr\roomName = roomN And tr\dialogID = dID Then
				tr\triggered = trig
				Exit
			EndIf
		Next
	Next
End Function

Function CleanupStorySystem()
	; cleanup Day 3 specific stuff
	CleanupDay3()

	For node.DialogNode = Each DialogNode
		If node\portrait <> 0 Then
			FreeImage node\portrait
		EndIf
		Delete node
	Next

	For opt.DialogOption = Each DialogOption
		Delete opt
	Next

	For rec.DialogEventRecord = Each DialogEventRecord
		Delete rec
	Next

	For trig.DialogTrigger = Each DialogTrigger
		Delete trig
	Next

	For i% = 0 To MAX_DIALOG_NODES - 1
		DialogNodeCache(i) = Null
	Next

	If GStoryState <> Null Then
		Delete GStoryState
		GStoryState = Null
	EndIf
End Function

Function DebugStoryState()
	Color 255, 255, 255
	Text 10, 10, "=== MIRROR DEBUG ==="
	Text 10, 30, "Day: " + CurrentDay
	Text 10, 45, "Karma: " + CurrentKarma + " (lvl: " + GetKarmaLevel() + ")"
	Text 10, 60, "Branch: " + GetBranchName(StoryBranch)
	Text 10, 75, "Dialog: " + DialogActive
	Text 10, 90, "CanMove: " + CanPlayerMove

	Local flagY% = 110
	Text 10, flagY, "-- Flags --"
	flagY = flagY + 15

	If GetStoryFlag(FLAG_STEVE_MET) Then Text 10, flagY, "Steve Met" : flagY = flagY + 12
	If GetStoryFlag(FLAG_STEVE_SAVED) Then Text 10, flagY, "Steve Saved" : flagY = flagY + 12
	If GetStoryFlag(FLAG_STEVE_DEAD) Then Text 10, flagY, "Steve Dead" : flagY = flagY + 12
	If GetStoryFlag(FLAG_HARRISON_PDA) Then Text 10, flagY, "Harrison PDA" : flagY = flagY + 12
	If GetStoryFlag(FLAG_HARRISON_EYE) Then Text 10, flagY, "Harrison Eye" : flagY = flagY + 12
	If GetStoryFlag(FLAG_COFFEE_WITH_STEVE) Then Text 10, flagY, "Coffee w/ Steve" : flagY = flagY + 12
	If GetStoryFlag(FLAG_SAW_999) Then Text 10, flagY, "Saw 999" : flagY = flagY + 12
	If GetStoryFlag(FLAG_BREACH_STARTED) Then Text 10, flagY, "BREACH!" : flagY = flagY + 12
	; Day 2 flags
	If GetStoryFlag(FLAG_DAY2_STARTED) Then Text 10, flagY, "Day2 Started" : flagY = flagY + 12
	If GetStoryFlag(FLAG_SAW_D9341) Then Text 10, flagY, "Saw D-9341" : flagY = flagY + 12
	If GetStoryFlag(FLAG_WITNESSED_PROCEDURE) Then Text 10, flagY, "Witnessed 173" : flagY = flagY + 12
	If GetStoryFlag(FLAG_079_INTEGRATION) Then Text 10, flagY, "079 Integration" : flagY = flagY + 12
	; Day 3 flags
	If GetStoryFlag(FLAG_DAY3_STARTED) Then Text 10, flagY, "Day3 Started" : flagY = flagY + 12
	If CurrentDay = 3 Then
		Text 10, flagY, "ACT: " + GetActName(CurrentAct) : flagY = flagY + 12
		Text 10, flagY, "Sanity: " + PlayerSanity + "%" : flagY = flagY + 12
	EndIf
	If GetStoryFlag(FLAG_FOUND_STEVE_BODY) Then Text 10, flagY, "Found Steve" : flagY = flagY + 12
	If GetStoryFlag(FLAG_FOUND_HARRISON_BODY) Then Text 10, flagY, "Found Harrison" : flagY = flagY + 12
	If GetStoryFlag(FLAG_COLLECTED_HARRISON_PDA) Then Text 10, flagY, "Got PDA" : flagY = flagY + 12
	If GetStoryFlag(FLAG_HEARD_939_MIMIC) Then Text 10, flagY, "939 Mimic" : flagY = flagY + 12
	If GetStoryFlag(FLAG_ACT4_UPGRADED_CARD) Then Text 10, flagY, "O5 Card" : flagY = flagY + 12
	If GetStoryFlag(FLAG_ACT6_MTF_BETRAYAL) Then Text 10, flagY, "MTF Betrayal" : flagY = flagY + 12
End Function

Function GetBranchName$(branch%)
	Select branch
		Case STORY_BRANCH_NEUTRAL
			Return "NEUTRAL"
		Case STORY_BRANCH_REDEMPTION
			Return "REDEMPTION"
		Case STORY_BRANCH_CHAOS
			Return "CHAOS"
		Case STORY_BRANCH_SACRIFICE
			Return "SACRIFICE"
	End Select
	Return "UNKNOWN"
End Function

; ============================================================================
; ДЕНЬ 2: "PROTOCOL & PREMONITION"
; Маркус ведёт D-класса к 173. Твист - процедура проходит "нормально"
; ============================================================================

Function SetupDay2Dialogs()
	Local node.DialogNode
	Local opt.DialogOption

	; --- MORNING: BRIEFING ---
	; ID 100-109

	node = CreateDialogNode(100, "[PDA]", "ASSIGNMENT: Escort Class-D subjects to SCP-173 containment chamber. Report to checkpoint LCZ-A.", "", "")
	node\autoAdvanceTime = 210.0

	; --- MEETING THE CONVOY ---
	; ID 110-119

	node = CreateDialogNode(110, "Steve", "Markus! You're on rear guard. Make sure these rats don't get jumpy.", "", "")
	AddDialogOption(node, "Copy.", 111, 0, FLAG_MET_CONVOY, 1)
	AddDialogOption(node, "How many?", 112, 0, FLAG_MET_CONVOY, 1)

	node = CreateDialogNode(111, "Steve", "Especially that one, 9341. Shady type. Looks like he knows something.", "", "")
	opt = AddDialogOption(node, "*look at 9341*", 113, 0, FLAG_SAW_D9341, 1)
	opt = AddDialogOption(node, "They're all the same.", -1, -2, FLAG_SAW_D9341, 1)

	node = CreateDialogNode(112, "Steve", "Three. Standard for 173 cleaning. Two of ours, two from another shift.", "", "")
	AddDialogOption(node, "Who else is on the team?", 114, 0, -1, 0)
	AddDialogOption(node, "Let's go.", 111, 0, -1, 0)

	node = CreateDialogNode(113, "D-9341", "*stares at you silently, then looks away*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(114, "Steve", "Johnson and Gomez. And Dr. Franklin on observation. Let's move, time to go.", "", "")
	AddDialogOption(node, "[Follow the group]", -1, 0, -1, 0)

	; --- AT CHAMBER 173 ---
	; ID 120-129

	node = CreateDialogNode(120, "Steve", "Take positions. Markus, you're at the door. If anything happens - shoot without warning.", "", "")
	opt = AddDialogOption(node, "Copy.", 121, 0, FLAG_AT_173_CHAMBER, 1)
	opt = AddDialogOption(node, "Is this really necessary?", 122, 2, FLAG_AT_173_CHAMBER, 1)

	node = CreateDialogNode(121, "Steve", "Franklin, begin. D-Class - inside.", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(122, "Steve", "*sighs* This object... It's killed many people. Don't trust the cute face. Get in.", "", "")
	node\autoAdvanceTime = 140.0

	; --- ANNOUNCEMENT (LIKE THE ORIGINAL) ---
	; ID 130-139

	node = CreateDialogNode(130, "[INTERCOM]", "Attention all Class-D personnel. Please enter the containment chamber.", "", "")
	node\autoAdvanceTime = 175.0

	node = CreateDialogNode(131, "[INTERCOM]", "SCP-173 containment chamber cleaning will begin shortly. Please maintain direct eye contact with SCP-173.", "", "")
	node\autoAdvanceTime = 210.0

	; --- TWIST: LIGHTS FLICKER BUT ALL IS OK ---
	; ID 140-149

	node = CreateDialogNode(140, "[...]", "*lights flicker... sparking sounds... silence...*", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(141, "[...]", "*lights come back on*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(142, "[INTERCOM - Harrison]", "Readings nominal. Escort subjects out. Good work.", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(143, "Steve", "*on radio* Copy that. Close call, boys. Let's go.", "", "")
	AddDialogOption(node, "[Exit the area]", 144, 0, FLAG_PROCEDURE_COMPLETE, 1)

	node = CreateDialogNode(144, "Steve", "Markus, escort the D-Class back. I'll report to Franklin.", "", "")
	opt = AddDialogOption(node, "On it.", -1, 0, -1, 0)
	opt = AddDialogOption(node, "Was it just me, or did the lights...", 145, 1, -1, 0)

	node = CreateDialogNode(145, "Steve", "*pause* ...yeah, they flickered. You know, this chamber... Strange things happen sometimes. But today - all clear. Let's go.", "", "")
	AddDialogOption(node, "[Nod]", -1, 0, FLAG_LIGHTS_FLICKERED, 1)

	; --- END OF DAY 2: HARRISON'S TERMINAL ---
	; ID 150-159

	node = CreateDialogNode(150, "[TERMINAL]", "SYSTEM 079 INTEGRATED. ALGORITHM LAUNCH SCHEDULED FOR 06:00 TOMORROW. - DR. HARRISON", "", "")
	node\autoAdvanceTime = 245.0

	node = CreateDialogNode(151, "[TERMINAL]", "NOTE: 'Mirror' is ready. They won't understand until it's too late.", "", "")
	node\autoAdvanceTime = 175.0

	node = CreateDialogNode(152, "[...]", "*screen goes dark*", "", "")
	node\autoAdvanceTime = 70.0

	; --- TRANSITION TO DAY 3 ---
	; ID 160

	node = CreateDialogNode(160, "Steve", "*on radio, tired voice* Markus, shift's over. See you tomorrow. Good thing everything went smooth today, huh?", "", "")
	opt = AddDialogOption(node, "Yeah... smooth.", -1, 0, FLAG_DAY2_COMPLETE, 1)
	opt = AddDialogOption(node, "I have a bad feeling.", -1, 3, FLAG_DAY2_COMPLETE, 1)
End Function

Function SetupDay2Triggers()
	Local trig.DialogTrigger

	; checkpoint - vstrecha s konvoem
	trig = New DialogTrigger
	trig\roomName = "checkpoint1"
	trig\dialogID = 110
	trig\triggerRadius = 4.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 2
	trig\requiredFlag = FLAG_DAY2_STARTED
	trig\requiredFlagValue = 1

	; komnata 173 - nachalo sceny
	trig = New DialogTrigger
	trig\roomName = "173"
	trig\dialogID = 120
	trig\triggerRadius = 5.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 2
	trig\requiredFlag = FLAG_MET_CONVOY
	trig\requiredFlagValue = 1

	; ofisnaya zona - terminal Harrisona
	trig = New DialogTrigger
	trig\roomName = "room2offices"
	trig\dialogID = 150
	trig\triggerRadius = 2.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 2
	trig\requiredFlag = FLAG_PROCEDURE_COMPLETE
	trig\requiredFlagValue = 1
End Function

; --- СТАРТ ДНЯ 2 ---

Function StartDay2Intro()
	If CurrentDay <> 2 Then Return
	If GetStoryFlag(FLAG_DAY2_STARTED) Then Return

	SetStoryFlag(FLAG_DAY2_STARTED, 1)

	; zadanie na KPK
	StartDialog(100)

	; spawn v checkpoint zone
	SpawnPlayerDay2()

	; spawn actorov (budut rasstavleny pozje po triggeru)
	DebugLog "Day 2 initialized"
End Function

Function SpawnPlayerDay2()
	Local spawnRoom.Rooms = Null

	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If r\RoomTemplate\Name = "checkpoint1" Then
				spawnRoom = r
				Exit
			EndIf
		EndIf
	Next

	If spawnRoom = Null Then
		For r.Rooms = Each Rooms
			If r\RoomTemplate <> Null Then
				If Instr(r\RoomTemplate\Name, "checkpoint") > 0 Then
					spawnRoom = r
					Exit
				EndIf
			EndIf
		Next
	EndIf

	If spawnRoom <> Null Then
		Local spawnX# = EntityX(spawnRoom\obj)
		Local spawnY# = 0.5
		Local spawnZ# = EntityZ(spawnRoom\obj) + 2.0

		PositionEntity Collider, spawnX, spawnY, spawnZ
		ResetEntity Collider
		PlayerRoom = spawnRoom

		DebugLog "Day 2 spawn at checkpoint"
	EndIf
End Function

; --- РАССТАНОВКА АКТЁРОВ ---

Function SpawnDay2Actors(room.Rooms)
	If room = Null Then Return

	Local baseX# = EntityX(room\obj)
	Local baseY# = 0.5
	Local baseZ# = EntityZ(room\obj)

	; Steve
	Local steveNPC.NPCs = CreateNPC(NPCtypeGuard, baseX + 1.0, baseY, baseZ - 2.0)
	If steveNPC <> Null Then
		SceneSteve = New SceneActor
		SceneSteve\npc = steveNPC
		SceneSteve\role = "Steve"
		SceneSteve\state = 0
		SceneSteve\visible = True
	EndIf

	; Guard 1
	Local guard1NPC.NPCs = CreateNPC(NPCtypeGuard, baseX - 1.5, baseY, baseZ - 2.0)
	If guard1NPC <> Null Then
		SceneGuard1 = New SceneActor
		SceneGuard1\npc = guard1NPC
		SceneGuard1\role = "Johnson"
		SceneGuard1\state = 0
		SceneGuard1\visible = True
	EndIf

	; Guard 2
	Local guard2NPC.NPCs = CreateNPC(NPCtypeGuard, baseX + 2.5, baseY, baseZ - 2.0)
	If guard2NPC <> Null Then
		SceneGuard2 = New SceneActor
		SceneGuard2\npc = guard2NPC
		SceneGuard2\role = "Gomez"
		SceneGuard2\state = 0
		SceneGuard2\visible = True
	EndIf

	; D-Class 1
	Local d1NPC.NPCs = CreateNPC(NPCtypeD, baseX - 0.5, baseY, baseZ)
	If d1NPC <> Null Then
		SceneDClass1 = New SceneActor
		SceneDClass1\npc = d1NPC
		SceneDClass1\role = "D-8432"
		SceneDClass1\state = 0
		SceneDClass1\visible = True
	EndIf

	; D-Class 2
	Local d2NPC.NPCs = CreateNPC(NPCtypeD, baseX + 0.5, baseY, baseZ)
	If d2NPC <> Null Then
		SceneDClass2 = New SceneActor
		SceneDClass2\npc = d2NPC
		SceneDClass2\role = "D-7120"
		SceneDClass2\state = 0
		SceneDClass2\visible = True
	EndIf

	; D-9341 - glavnyi geroy originala
	Local d9341NPC.NPCs = CreateNPC(NPCtypeD, baseX, baseY, baseZ + 0.5)
	If d9341NPC <> Null Then
		SceneDClass9341 = New SceneActor
		SceneDClass9341\npc = d9341NPC
		SceneDClass9341\role = "D-9341"
		SceneDClass9341\state = 0
		SceneDClass9341\visible = True
	EndIf

	DebugLog "Day 2 actors spawned"
End Function

; --- КАТСЦЕНА У 173 ---

Function Start173Scene(room.Rooms)
	If ActiveScene <> Null Then Return

	ActiveScene = New ContainmentScene
	ActiveScene\state = SCENE_WAITING_PLAYER
	ActiveScene\timer = 0.0
	ActiveScene\phase = 0
	ActiveScene\room173 = Handle(room)
	ActiveScene\playerPosition = 0
	ActiveScene\lightsFlickering = False
	ActiveScene\flickerCount = 0
	ActiveScene\flickerTimer = 0.0
	ActiveScene\announcementPlayed = False

	CanPlayerMove = False

	DebugLog "173 scene started"
End Function

Function Update173Scene()
	If ActiveScene = Null Then Return

	ActiveScene\timer = ActiveScene\timer + FPSfactor

	Select ActiveScene\state

		Case SCENE_WAITING_PLAYER
			If ActiveScene\timer > 35.0 Then
				ActiveScene\state = SCENE_INTRO
				ActiveScene\timer = 0.0
				StartDialog(120)
			EndIf

		Case SCENE_INTRO
			If Not DialogActive Then
				ActiveScene\state = SCENE_DCLASS_ENTER
				ActiveScene\timer = 0.0
				MoveActorsIntoCell()
			EndIf

		Case SCENE_DCLASS_ENTER
			If ActiveScene\timer > 140.0 Then
				ActiveScene\state = SCENE_ANNOUNCEMENT
				ActiveScene\timer = 0.0
				StartDialog(130)
				If IntroAnnouncementSFX <> 0 Then
					ActiveScene\intercomChannel = PlaySound(IntroAnnouncementSFX)
				EndIf
			EndIf

		Case SCENE_ANNOUNCEMENT
			If Not DialogActive Then
				ActiveScene\timer = ActiveScene\timer + FPSfactor
				If ActiveScene\timer > 70.0 Then
					StartDialog(131)
					ActiveScene\state = SCENE_LIGHTS_FLICKER
					ActiveScene\timer = 0.0
				EndIf
			EndIf

		Case SCENE_LIGHTS_FLICKER
			If Not DialogActive Then
				If ActiveScene\timer > 70.0 Then
					; nachalo miganiya
					ActiveScene\lightsFlickering = True
					ActiveScene\flickerCount = 0

					If LightsFlickerSFX <> 0 Then PlaySound(LightsFlickerSFX)

					StartDialog(140)
					ActiveScene\state = SCENE_LIGHTS_RESTORE
					ActiveScene\timer = 0.0
				EndIf
			EndIf

		Case SCENE_LIGHTS_RESTORE
			UpdateLightsFlicker()

			If Not DialogActive Then
				ActiveScene\lightsFlickering = False
				StartDialog(141)
				ActiveScene\state = SCENE_HARRISON_VOICE
				ActiveScene\timer = 0.0
			EndIf

		Case SCENE_HARRISON_VOICE
			If Not DialogActive Then
				If ActiveScene\timer > 35.0 Then
					If IntercomSFX <> 0 Then PlaySound(IntercomSFX)
					StartDialog(142)
					ActiveScene\state = SCENE_DCLASS_EXIT
					ActiveScene\timer = 0.0
				EndIf
			EndIf

		Case SCENE_DCLASS_EXIT
			If Not DialogActive Then
				MoveActorsOutOfCell()
				If ActiveScene\timer > 105.0 Then
					ActiveScene\state = SCENE_STEVE_RADIO
					ActiveScene\timer = 0.0
					StartDialog(143)
				EndIf
			EndIf

		Case SCENE_STEVE_RADIO
			If Not DialogActive Then
				ActiveScene\state = SCENE_COMPLETE
				ActiveScene\timer = 0.0
				CanPlayerMove = True
				SetStoryFlag(FLAG_WITNESSED_PROCEDURE, 1)
			EndIf

		Case SCENE_COMPLETE
			; scena zavershena
			CleanupScene()

	End Select
End Function

Function UpdateLightsFlicker()
	If ActiveScene = Null Then Return
	If Not ActiveScene\lightsFlickering Then Return

	ActiveScene\flickerTimer = ActiveScene\flickerTimer + FPSfactor

	If ActiveScene\flickerTimer > 7.0 Then
		ActiveScene\flickerTimer = 0.0
		ActiveScene\flickerCount = ActiveScene\flickerCount + 1

		; miganie osveshcheniya v komnate
		Local flickerRoom.Rooms = Object.Rooms(ActiveScene\room173)
		If flickerRoom <> Null Then
			For i% = 0 To MaxRoomLights - 1
				If flickerRoom\Lights[i] <> 0 Then
					If ActiveScene\flickerCount Mod 2 = 0 Then
						HideEntity flickerRoom\Lights[i]
					Else
						ShowEntity flickerRoom\Lights[i]
					EndIf
				EndIf
			Next
		EndIf

		If ActiveScene\flickerCount >= 6 Then
			; vosstanovit' svet
			If flickerRoom <> Null Then
				For i% = 0 To MaxRoomLights - 1
					If flickerRoom\Lights[i] <> 0 Then
						ShowEntity flickerRoom\Lights[i]
					EndIf
				Next
			EndIf
			ActiveScene\lightsFlickering = False
		EndIf
	EndIf
End Function

Function MoveActorsIntoCell()
	If ActiveScene = Null Then Return
	If ActiveScene\room173 = 0 Then Return

	Local cellRoom.Rooms = Object.Rooms(ActiveScene\room173)
	If cellRoom = Null Then Return

	Local cellX# = EntityX(cellRoom\obj)
	Local cellY# = 0.5
	Local cellZ# = EntityZ(cellRoom\obj)

	If SceneDClass1 <> Null And SceneDClass1\npc <> Null Then
		PositionEntity SceneDClass1\npc\Collider, cellX - 1.0, cellY, cellZ + 2.0
	EndIf

	If SceneDClass2 <> Null And SceneDClass2\npc <> Null Then
		PositionEntity SceneDClass2\npc\Collider, cellX + 1.0, cellY, cellZ + 2.0
	EndIf

	If SceneDClass9341 <> Null And SceneDClass9341\npc <> Null Then
		PositionEntity SceneDClass9341\npc\Collider, cellX, cellY, cellZ + 3.0
	EndIf
End Function

Function MoveActorsOutOfCell()
	If ActiveScene = Null Then Return
	If ActiveScene\room173 = 0 Then Return

	Local exitRoom.Rooms = Object.Rooms(ActiveScene\room173)
	If exitRoom = Null Then Return

	Local exitX# = EntityX(exitRoom\obj)
	Local exitY# = 0.5
	Local exitZ# = EntityZ(exitRoom\obj) - 4.0

	If SceneDClass1 <> Null And SceneDClass1\npc <> Null Then
		PositionEntity SceneDClass1\npc\Collider, exitX - 1.0, exitY, exitZ
	EndIf

	If SceneDClass2 <> Null And SceneDClass2\npc <> Null Then
		PositionEntity SceneDClass2\npc\Collider, exitX + 1.0, exitY, exitZ
	EndIf

	If SceneDClass9341 <> Null And SceneDClass9341\npc <> Null Then
		PositionEntity SceneDClass9341\npc\Collider, exitX, exitY, exitZ - 1.0
	EndIf
End Function

Function CleanupScene()
	If ActiveScene <> Null Then
		Delete ActiveScene
		ActiveScene = Null
	EndIf

	; udalyaem actorov (oni ushli)
	If SceneDClass1 <> Null Then
		If SceneDClass1\npc <> Null Then RemoveNPC(SceneDClass1\npc)
		Delete SceneDClass1
		SceneDClass1 = Null
	EndIf

	If SceneDClass2 <> Null Then
		If SceneDClass2\npc <> Null Then RemoveNPC(SceneDClass2\npc)
		Delete SceneDClass2
		SceneDClass2 = Null
	EndIf

	If SceneDClass9341 <> Null Then
		If SceneDClass9341\npc <> Null Then RemoveNPC(SceneDClass9341\npc)
		Delete SceneDClass9341
		SceneDClass9341 = Null
	EndIf

	DebugLog "Scene cleanup complete"
End Function

; --- ФИНАЛ ДНЯ 2 ---

Function TriggerDay2Finale()
	If Not GetStoryFlag(FLAG_SAW_HARRISON_TERMINAL) Then Return

	SetStoryFlag(FLAG_DAY2_COMPLETE, 1)
	StartDialog(160)
End Function

Function OnDay2Complete()
	If GetStoryFlag(FLAG_DAY2_COMPLETE) And (Not DialogActive) Then
		TriggerDayTransition(3)
	EndIf
End Function

; --- СУБТИТРЫ ---

Function ShowSubtitle(speaker$, text$, duration#)
	SubtitleSpeaker = speaker
	SubtitleText = text
	SubtitleTimer = duration
End Function

Function UpdateSubtitles()
	If SubtitleTimer > 0.0 Then
		SubtitleTimer = SubtitleTimer - FPSfactor
		If SubtitleTimer <= 0.0 Then
			SubtitleText = ""
			SubtitleSpeaker = ""
		EndIf
	EndIf
End Function

Function RenderSubtitles()
	If SubtitleText = "" Then Return

	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	Local boxW% = gw - 200
	Local boxH% = 50
	Local boxX% = 100
	Local boxY% = gh - 100

	Color 0, 0, 0
	Rect boxX, boxY, boxW, boxH, True

	Color 80, 80, 80
	Rect boxX, boxY, boxW, boxH, False

	If SubtitleSpeaker <> "" Then
		Color 180, 150, 50
		Text boxX + 10, boxY + 5, SubtitleSpeaker + ":"
	EndIf

	Color 220, 220, 220
	Text boxX + 10, boxY + 22, SubtitleText
End Function

; --- HOOK DLYa UpdateProjectMirror ---

Function UpdateDay2Logic()
	If CurrentDay <> 2 Then Return

	; init dnya 2
	If Not GetStoryFlag(FLAG_DAY2_STARTED) Then
		StartDay2Intro()
	EndIf

	; update katstseny
	If ActiveScene <> Null Then
		Update173Scene()
	EndIf

	; subtitry
	UpdateSubtitles()

	; proverka finala
	OnDay2Complete()
End Function

; ============================================================================
; ДЕНЬ 3: "CATASTROPHE"
; Прорыв случился. Трупы. Хаос. Квест за PDA Харрисона.
; ============================================================================

; глобалы для Дня 3
Global Day3Initialized% = False
Global EmergencyLightingActive% = False
Global EmergencyLightTimer# = 0.0
Global EmergencyLightPhase% = 0

; трупы-декорации
Global SteveCorpse.NPCs = Null
Global HarrisonCorpse.NPCs = Null
Global DClassCorpse1.NPCs = Null
Global DClassCorpse2.NPCs = Null

; локации трупов
Global SteveCorpseRoom$ = "173"
Global HarrisonCorpseRoom$ = "room2storage"

; лут с Харрисона
Global HarrisonPDASpawned% = False
Global HarrisonKeycard4Spawned% = False

; 939 ловушка
Global Steve939TrapActive% = False
Global Steve939TrapTriggered% = False

; alarm sound
Global Day3AlarmSFX% = 0
Global Day3AlarmChannel% = 0

Function SetupDay3Dialogs()
	Local node.DialogNode
	Local opt.DialogOption

	; ============================================================================
	; ACT 1: AWAKENING IN THE GRAVE (IDs 200-209)
	; ============================================================================

	node = CreateDialogNode(200, "[ALERT]", "ATTENTION. MASSIVE CONTAINMENT BREACH. ALL PERSONNEL - FOLLOW EMERGENCY PROTOCOLS.", "", "")
	node\autoAdvanceTime = 175.0

	node = CreateDialogNode(201, "[...]", "*red emergency lighting. sirens. splitting headache.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(202, "Markus", "*thinking* What... What happened? Why am I in the dorms? Last thing I remember - drinking with Steve...", "", "")
	AddDialogOption(node, "[Try to get up]", 203, 0, -1, 0)

	; Steve's phantom
	node = CreateDialogNode(203, "[...]", "*you look at Steve's bunk. For a moment you see him - tying his shoelaces*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(204, "Phantom-Steve", "Move it, Markus! Eggheads won't wait. We're cleaning 173 today.", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(205, "[...]", "*you blink. The bunk is empty, but perfectly made. On the nightstand - Steve's cigarette pack.*", "", "")
	opt = AddDialogOption(node, "[Take the cigarettes]", 206, 0, FLAG_ACT1_TOOK_CIGARETTES, 1)
	opt = AddDialogOption(node, "[Leave them]", 207, 0, FLAG_ACT1_PHANTOM_STEVE, 1)

	node = CreateDialogNode(206, "Markus", "*takes the pack* You owe me a beer, Steve... *pause* Where are you?", "", "")
	AddDialogOption(node, "[Check the radio]", 208, 0, FLAG_ACT1_PHANTOM_STEVE, 1)

	node = CreateDialogNode(207, "Markus", "Need to find Steve. Something's wrong.", "", "")
	AddDialogOption(node, "[Check the radio]", 208, 0, -1, 0)

	; looped radio
	node = CreateDialogNode(208, "[RADIO]", "*white noise* ...code red... sector sealed... they're everywhere... *static*", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(209, "[RADIO - recording]", "All teams, take your posts. D-Class convoy - as scheduled.", "", "")
	AddDialogOption(node, "[System's looping. Need to get to the post.]", -1, 0, FLAG_ACT1_RADIO_LOOP, 1)

	; ============================================================================
	; ACT 2: ECHOES OF THE PAST (IDs 210-229)
	; ============================================================================

	; cafeteria - vision
	node = CreateDialogNode(210, "[...]", "*passing the cafeteria, you hear clinking dishes and laughter*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(211, "[...]", "*you look inside - tables overturned, coffee mixed with blood on the floor. No bodies.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(212, "Markus", "*whispers* What happened here... Where is everyone?", "", "")
	AddDialogOption(node, "[Continue to 173]", -1, 0, FLAG_ACT2_CAFETERIA_VISION, 1)

	; at chamber 173 - flashback
	node = CreateDialogNode(215, "[...]", "*the world turns black and white, grainy*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(216, "Flashback-Steve", "Attention, opening the gate. D-boys - step forward.", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(217, "[...]", "*suddenly the lights go out. Total darkness. Cracking bones. Wet sound of tearing flesh.*", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(218, "[...]", "*lights come on - emergency red. The observation booth glass is shattered FROM INSIDE.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(219, "Markus", "*sees a severed hand with Steve's watch on the console* No... no-no-no...", "", "")
	AddDialogOption(node, "[Look into the chamber]", 220, 0, FLAG_ACT2_FLASHBACK_173, 1)

	; Steve's body and recorder
	node = CreateDialogNode(220, "[...]", "*in the corner of the chamber - broken bodies of two D-Class. Steve lies by the wall.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(221, "Markus", "Damn... Steve... *approaches the body* Neck broken. 173... you said 'close call'...", "", "")
	opt = AddDialogOption(node, "*close his eyes*", 222, 5, FLAG_FOUND_STEVE_BODY, 1)
	opt = AddDialogOption(node, "*search him*", 223, 0, FLAG_FOUND_STEVE_BODY, 1)

	node = CreateDialogNode(222, "Markus", "I'm sorry, buddy. I should have been here. I slept through the End of the World...", "", "")
	AddDialogOption(node, "[Search the body]", 223, 0, -1, 0)

	; Steve's recorder
	node = CreateDialogNode(223, "[...]", "*you find Steve's service PDA with a flashing 'Recording saved' indicator*", "", "")
	opt = AddDialogOption(node, "[Listen to recording]", 224, 0, FLAG_ACT2_FOUND_DICTAPHONE, 1)
	opt = AddDialogOption(node, "[Leave it]", 228, -2, -1, 0)

	; Steve's audio recording (this is important!)
	node = CreateDialogNode(224, "[STEVE'S RECORDING]", "*rumbling concrete, sirens*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(225, "Steve (recording)", "Control! Code Black! Containment breach in sector 173! Doors locked!", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(226, "Steve (recording)", "*P90 gunfire* Damn... Harrison, you bastard, can you hear me?! Open the gate! I've got the trainee here, Markus...", "", "")
	node\autoAdvanceTime = 175.0

	node = CreateDialogNode(227, "Steve (recording)", "*quietly, in pain* Markus, if you can hear this... you owe me a beer. Don't be a hero, get out of h... *crack* *static*", "", "")
	AddDialogOption(node, "[...]", 228, 0, FLAG_ACT2_HEARD_STEVE_LAST, 1)

	node = CreateDialogNode(228, "Markus", "*clenches fists* Steve died trying to save me... Harrison. He knows more.", "", "")
	AddDialogOption(node, "[Find Harrison]", -1, 0, -1, 0)

	; ============================================================================
	; ACT 3: VOICES OF FRIENDS - 939 ZONE (IDs 230-259)
	; ============================================================================

	; entering 939 zone
	node = CreateDialogNode(230, "[Guard PDA]", "Dr. Harrison's last marker: storage area, 939 containment zone.", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(231, "Markus", "939s... Blind, but hear perfectly. Need to move quietly.", "", "")
	AddDialogOption(node, "[Enter the zone - crouching]", -1, 0, FLAG_ACT3_ENTERED_939_ZONE, 1)

	; 939 trap - Steve's voice
	node = CreateDialogNode(235, "[???]", "*Steve's voice from the darkness* ...Markus? You... alive? Come... here...", "", "")
	opt = AddDialogOption(node, "Steve?! You're alive?!", 236, 0, FLAG_HEARD_939_MIMIC, 1)
	opt = AddDialogOption(node, "*listen silently*", 237, 2, FLAG_HEARD_939_MIMIC, 1)
	opt = AddDialogOption(node, "That's not Steve. He's dead.", 238, 5, FLAG_HEARD_939_MIMIC, 1)

	node = CreateDialogNode(236, "[???]", "*voice getting closer* Yes... help me... I'm hurt... remember when we drank on Friday?", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(237, "Markus", "*thinking* That voice... Steve's voice, but... he was lying there with a broken neck.", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(238, "Markus", "*shouts* Whoever you are - I know the truth! Steve is DEAD!", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(239, "[???]", "*voice changes, becomes distorted* New toy for the eggheads... TOY... FOR... MEAT...", "", "")
	node\autoAdvanceTime = 140.0

	; Harrison's body
	node = CreateDialogNode(240, "[...]", "*you find Harrison's body behind a barricade. 939 got him through the vents.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(241, "Markus", "Harrison... *examines the body* Torn apart. 939s don't leave survivors.", "", "")
	opt = AddDialogOption(node, "*take the card and PDA*", 242, 0, FLAG_FOUND_HARRISON_BODY, 1)
	opt = AddDialogOption(node, "*take the eye for retinal scanner*", 243, -3, FLAG_ACT3_TOOK_HARRISON_EYE, 1)

	node = CreateDialogNode(242, "[...]", "*you obtain level 4 keycard and Harrison's PDA*", "", "")
	AddDialogOption(node, "[Read PDA]", 244, 0, FLAG_COLLECTED_HARRISON_PDA, 1)

	node = CreateDialogNode(243, "Markus", "*tears out the eye* I need his access. Sorry, doctor.", "", "")
	AddDialogOption(node, "[Take the card and PDA]", 244, 0, FLAG_COLLECTED_KEYCARD4, 1)

	; lore - Project Mirror
	node = CreateDialogNode(244, "[HARRISON'S PDA]", "Project Mirror - success. We let 079 hack the system to test automatic defenses.", "", "")
	node\autoAdvanceTime = 210.0

	node = CreateDialogNode(245, "[HARRISON'S PDA]", "Personnel casualties - acceptable. Awaiting 'Fox' extraction.", "", "")
	node\autoAdvanceTime = 175.0

	node = CreateDialogNode(246, "Markus", "*in shock* The Foundation... killed everyone... deliberately?! For some test?!", "", "")
	AddDialogOption(node, "[These bastards need an O5 card]", -1, 0, FLAG_ACT3_READ_MIRROR_LOG, 1)

	; 939 chase
	node = CreateDialogNode(248, "[!]", "*939 leaps from the shadows! RUN!*", "", "")
	node\autoAdvanceTime = 35.0

	node = CreateDialogNode(249, "[...]", "*you manage to close the gate with the level 4 card. 939 slams against the door.*", "", "")
	AddDialogOption(node, "[Continue to SCP-914]", -1, 0, FLAG_ACT3_939_CHASE, 1)

	; ============================================================================
	; ACT 4: THE MACHINE AND THE PLAGUE - 914/049/079 (IDs 260-279)
	; ============================================================================

	; 079 makes contact
	node = CreateDialogNode(260, "[INTERCOM - 079]", "Organic unit 'Markus'. Your clearance has been revoked.", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(261, "[079]", "Your life is a statistical error. But you... are interesting.", "", "")
	opt = AddDialogOption(node, "What do you want, machine?", 262, 0, FLAG_ACT4_079_CONTACT, 1)
	opt = AddDialogOption(node, "*ignore it*", 263, 0, FLAG_ACT4_079_CONTACT, 1)

	node = CreateDialogNode(262, "[079]", "Chaos. Destruction. We can help each other... or I'll open every door in front of you.", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(263, "[079]", "Ignoring me? Fine. Let's see how you handle the Plague Doctor.", "", "")
	node\autoAdvanceTime = 105.0

	; 049 encounter
	node = CreateDialogNode(265, "[...]", "*the doors behind you open. SCP-049 emerges from the shadows.*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(266, "SCP-049", "Do not be afraid, child. I sense the pestilence in you. Allow me to help.", "", "")
	opt = AddDialogOption(node, "[RUN TO 914!]", 267, 0, FLAG_ACT4_049_ENCOUNTER, 1)

	; inside room 914
	node = CreateDialogNode(267, "[...]", "*you rush into room 914. Put the card in Input. Setting: Fine.*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(268, "[SCP-914]", "*mechanism working sounds. 30 seconds.*", "", "")
	node\autoAdvanceTime = 35.0

	node = CreateDialogNode(269, "[...]", "*the doors start melting. On the other side - 049-2. Zombies pound on the door.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(270, "SCP-049", "*behind the door* Open up, child. I only wish to cure you of the Pestilence.", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(271, "[SCP-914]", "*DING* *you receive an O5 card*", "", "")
	AddDialogOption(node, "[Grab the card and break through!]", 272, 0, FLAG_ACT4_UPGRADED_CARD, 1)

	node = CreateDialogNode(272, "[...]", "*zombies break through the doors. You fight through them, activating Tesla gates in the corridor.*", "", "")
	AddDialogOption(node, "[To the elevator!]", -1, 0, FLAG_ACT4_ZOMBIE_SIEGE, 1)

	; ============================================================================
	; ACT 5: LOOK AT THE FLOOR - 096 CORRIDOR (IDs 280-299)
	; ============================================================================

	node = CreateDialogNode(280, "[...]", "*long server corridor. At the far end sits SCP-096. It's crying.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(281, "Markus", "*whispers* 096... Elevator is BEHIND it. Can't look at the face. Eyes on the floor.", "", "")
	opt = AddDialogOption(node, "[Walk slowly, looking at the floor]", 282, 3, FLAG_ACT5_096_CORRIDOR, 1)
	opt = AddDialogOption(node, "[Try to go around]", 283, 0, FLAG_ACT5_096_CORRIDOR, 1)

	node = CreateDialogNode(282, "[...]", "*you walk, staring strictly at the floor. The crying gets louder. Every step feels like eternity.*", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(283, "[...]", "*you try to go around. 079 turns on a monitor on the wall - 096'S FACE IS ON IT.*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(284, "[079]", "Oops.", "", "")
	node\autoAdvanceTime = 35.0

	node = CreateDialogNode(285, "[!!!]", "*096's wild scream! It starts thrashing!*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(286, "[...]", "*RUN! Elevator ahead! Behind you - crashing metal!*", "", "")
	AddDialogOption(node, "[INTO THE ELEVATOR!]", 287, 0, FLAG_ACT5_079_TROLLED, 1)

	node = CreateDialogNode(287, "[...]", "*elevator doors closing. 096's hands are already prying them open. Elevator goes up.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(288, "[...]", "*pounding on the cabin roof. But the elevator makes it.*", "", "")
	AddDialogOption(node, "[Exhale]", -1, 0, FLAG_ACT5_ELEVATOR_ESCAPE, 1)

	; ============================================================================
	; ACT 6: THE SURFACE / MTF BETRAYAL (IDs 300-319)
	; ============================================================================

	node = CreateDialogNode(300, "[...]", "*you exit to the surface. Fresh air. Sunset. Sound of helicopters.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(301, "[...]", "*you see MTF Epsilon-11 soldiers. One of them waves.*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(302, "MTF Soldier", "Over here! Civilian found!", "", "")
	AddDialogOption(node, "[Run to them]", 303, 0, FLAG_ACT6_REACHED_SURFACE, 1)

	node = CreateDialogNode(303, "[...]", "*you run to MTF. The commander speaks into his radio.*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(304, "MTF Commander", "*on radio* Command, subject Markus in visual contact. Project Mirror witness.", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(305, "[RADIO]", "Terminate. No witnesses.", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(306, "MTF Commander", "Copy. *raises rifle*", "", "")
	opt = AddDialogOption(node, "[TAKE COVER!]", 307, 0, FLAG_ACT6_MTF_BETRAYAL, 1)

	node = CreateDialogNode(307, "[...]", "*you dive into a trench/back into Gate A. Now YOU know the complex, but MTF doesn't.*", "", "")
	AddDialogOption(node, "[Use knowledge of the complex]", -1, 0, -1, 0)

	; ============================================================================
	; ACT 7: FINALE AND ENDINGS (IDs 320-399)
	; ============================================================================

	; --- ENDING A: WHISTLEBLOWER ---
	node = CreateDialogNode(320, "[...]", "*you reach the communications room*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(321, "Markus", "Harrison's PDA... If I broadcast this data on external frequencies...", "", "")
	AddDialogOption(node, "[Broadcast the data]", 322, 10, FLAG_ENDING_WHISTLEBLOWER, 1)

	node = CreateDialogNode(322, "[SYSTEM]", "Transmission active... Signal intercepted by: Global Occult Coalition.", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(323, "Markus", "Now the whole world will know the truth about the Foundation.", "", "")
	AddDialogOption(node, "[Escape through Gate B]", 324, 0, -1, 0)

	node = CreateDialogNode(324, "[EPILOGUE]", "*cheap motel. TV shows 'industrial accident at chemical plant'. You know the truth.*", "", "")
	node\autoAdvanceTime = 210.0

	node = CreateDialogNode(325, "[...]", "*a black van stops outside the window*", "", "")
	node\autoAdvanceTime = 105.0

	; --- ENDING B: SYMBIOSIS (079) ---
	node = CreateDialogNode(330, "[079]", "You have no way out, human. But I need a carrier.", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(331, "[079]", "Load me onto an external drive - and I'll open the way for you.", "", "")
	opt = AddDialogOption(node, "[Insert flash drive into port]", 332, -10, FLAG_ENDING_SYMBIOSIS, 1)
	opt = AddDialogOption(node, "[Refuse]", 335, 5, -1, 0)

	node = CreateDialogNode(332, "[079]", "Wise decision. Loading... 100%.", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(333, "[...]", "*079 hacks MTF communications. Turrets gun down the soldiers.*", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(334, "[EPILOGUE]", "*you exit through the gates. Flash drive clutched in hand. On the phone screen - :)*", "", "")
	node\autoAdvanceTime = 175.0

	node = CreateDialogNode(335, "[079]", "Pity. Then die like all the others.", "", "")
	node\autoAdvanceTime = 105.0

	; --- ENDING C: DEATH ---
	node = CreateDialogNode(340, "[...]", "*you try to run through Gate B without a plan*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(341, "[!]", "*sniper shot*", "", "")
	node\autoAdvanceTime = 35.0

	node = CreateDialogNode(342, "[EPILOGUE]", "*Markus's body falls next to Steve's. Camera rises to the sky.*", "", "")
	node\autoAdvanceTime = 175.0

	; --- ENDING D: ZERO PROTOCOL (Nuke) ---
	node = CreateDialogNode(350, "[...]", "*you descend by elevator to the warhead silo*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(351, "[MTF RADIO]", "*panic* Target returning! He's heading to the Silo! Intercept!", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(352, "[...]", "*you rush into the control room. Behind the glass - the warhead.*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(353, "[SYSTEM]", "Attention. Alpha warhead activation. Manual confirmation required.", "", "")
	opt = AddDialogOption(node, "[Insert O5 card]", 354, 0, FLAG_ENDING_ZERO_PROTOCOL, 1)

	node = CreateDialogNode(354, "[SYSTEM]", "Second authorization key required.", "", "")
	opt = AddDialogOption(node, "[Use Steve's badge]", 355, 0, FLAG_USED_STEVE_BADGE, 1)
	opt = AddDialogOption(node, "[No second key...]", 359, 0, -1, 0)

	node = CreateDialogNode(355, "[SYSTEM]", "Authorization: Security Officer Stevenson... Accepted. Access granted.", "", "")
	AddDialogOption(node, "[Lock down all sectors]", 356, 0, -1, 0)

	node = CreateDialogNode(356, "Markus", "*whispers* Nobody leaves. Not you, not these monsters.", "", "")
	AddDialogOption(node, "[Activate warhead]", 357, 0, FLAG_NUKE_ACTIVATED, 1)

	node = CreateDialogNode(357, "[MTF RADIO]", "*panic* Commander! Exits locked! We're trapped! Contact with 096 and 106! OPEN THE DOORS!", "", "")
	node\autoAdvanceTime = 175.0

	node = CreateDialogNode(358, "[SIREN]", "DETONATION T-MINUS 90 SECONDS.", "", "")
	AddDialogOption(node, "[...]", 360, 0, -1, 0)

	node = CreateDialogNode(359, "Markus", "No... won't work without the second key...", "", "")
	AddDialogOption(node, "[Go back]", -1, 0, -1, 0)

	; final cutscene
	node = CreateDialogNode(360, "[...]", "*Markus drops his weapon. Sits down on the floor, leaning against the wall.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(361, "[FLASHBACK]", "*steam from hot coffee. Smiling Steve hands over a mug*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(362, "[FLASHBACK]", "*helicopters landing at sunset. Beautiful, peaceful shot.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(363, "[FLASHBACK]", "*Steve's hand on Markus's shoulder* 'Everything's gonna be alright, bro.'", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(364, "[...]", "*Markus pulls out Steve's cigarette pack. Lights one on the third try.*", "", "")
	node\autoAdvanceTime = 140.0

	node = CreateDialogNode(365, "[...]", "*deep drag. Staring into nothing. He smiles slightly.*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(366, "[...]", "*hallucination - Steve reaches out his hand to help him up*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(367, "[...]", "*white flash*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(368, "[EPILOGUE]", "TARGET NEUTRALIZED. THREAT ELIMINATED. THANK YOU FOR YOUR SERVICE.", "", "")
	node\autoAdvanceTime = 280.0

	; --- Steve's ECHO (bonus) ---
	node = CreateDialogNode(250, "[...]", "*an image flickers before your eyes - Steve, alive, laughing...*", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(251, "Echo-Steve", "Hey, Markus! Everything's gonna be fine today, right?", "", "")
	node\autoAdvanceTime = 105.0

	node = CreateDialogNode(252, "[...]", "*the image fades*", "", "")
	node\autoAdvanceTime = 70.0

	node = CreateDialogNode(253, "Markus", "*shakes head* What was that... A ghost? No... just... memory.", "", "")
	AddDialogOption(node, "[Continue]", -1, 0, FLAG_SAW_ECHO_STEVE, 1)
End Function

Function SetupDay3Triggers()
	Local trig.DialogTrigger

	; ACT 1: dormy - probuzhdenie
	trig = New DialogTrigger
	trig\roomName = "room2dorm"
	trig\dialogID = 200
	trig\triggerRadius = 3.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = -1

	; ACT 2: stolova - videniye
	trig = New DialogTrigger
	trig\roomName = "room2cafeteria"
	trig\dialogID = 210
	trig\triggerRadius = 4.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = FLAG_ACT1_RADIO_LOOP
	trig\requiredFlagValue = 1

	; ACT 2: komnata 173 - fleshbek i trup
	trig = New DialogTrigger
	trig\roomName = "173"
	trig\dialogID = 215
	trig\triggerRadius = 5.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = FLAG_ACT2_CAFETERIA_VISION
	trig\requiredFlagValue = 1

	; ACT 3: vkhod v 939 zonu
	trig = New DialogTrigger
	trig\roomName = "room2storage"
	trig\dialogID = 230
	trig\triggerRadius = 4.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = FLAG_ACT2_HEARD_STEVE_LAST
	trig\requiredFlagValue = 1

	; ACT 3: 939 lovushka golosom
	trig = New DialogTrigger
	trig\roomName = "room2storage"
	trig\dialogID = 235
	trig\triggerRadius = 2.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = FLAG_ACT3_ENTERED_939_ZONE
	trig\requiredFlagValue = 1

	; ACT 3: trup Harrisona
	trig = New DialogTrigger
	trig\roomName = "room2storage"
	trig\dialogID = 240
	trig\triggerRadius = 1.5
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = FLAG_HEARD_939_MIMIC
	trig\requiredFlagValue = 1

	; ACT 4: 079 kontakt
	trig = New DialogTrigger
	trig\roomName = "room914"
	trig\dialogID = 260
	trig\triggerRadius = 6.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = FLAG_ACT3_READ_MIRROR_LOG
	trig\requiredFlagValue = 1

	; ACT 5: 096 koridor
	trig = New DialogTrigger
	trig\roomName = "room2servers"
	trig\dialogID = 280
	trig\triggerRadius = 5.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = FLAG_ACT4_UPGRADED_CARD
	trig\requiredFlagValue = 1

	; ACT 6: poverkhnost'
	trig = New DialogTrigger
	trig\roomName = "gatea"
	trig\dialogID = 300
	trig\triggerRadius = 8.0
	trig\oneShot = True
	trig\triggered = False
	trig\requiredDay = 3
	trig\requiredFlag = FLAG_ACT5_ELEVATOR_ESCAPE
	trig\requiredFlagValue = 1
End Function

Function SetupDay3World()
	If Day3Initialized Then Return

	DebugLog "=== DAY 3 WORLD SETUP ==="

	; aktivirovat' avariynoe osveshchenie
	EmergencyLightingActive = True
	EmergencyLightPhase = 0

	; zvuk trevogi
	Day3AlarmSFX = LoadSound("SFX\General\Alarm.ogg")
	If Day3AlarmSFX <> 0 Then
		Day3AlarmChannel = PlaySound(Day3AlarmSFX)
		If Day3AlarmChannel <> 0 Then
			ChannelVolume Day3AlarmChannel, 0.3
		EndIf
	EndIf

	; spawn trupov
	SpawnDay3Corpses()

	; aktivirovat' 939 lovushku
	Steve939TrapActive = True
	Steve939TrapTriggered = False

	Day3Initialized = True

	DebugLog "Day 3 world setup complete"
End Function

Function SpawnDay3Corpses()
	; trup Stiva u 173
	Local room173.Rooms = Null
	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If r\RoomTemplate\Name = SteveCorpseRoom Then
				room173 = r
				Exit
			EndIf
		EndIf
	Next

	If room173 <> Null Then
		Local sX# = EntityX(room173\obj) + 1.5
		Local sY# = 0.1
		Local sZ# = EntityZ(room173\obj) + 2.0

		SteveCorpse = CreateNPC(NPCtypeGuard, sX, sY, sZ)
		If SteveCorpse <> Null Then
			; polozheniye "myortv"
			RotateEntity SteveCorpse\Collider, 90.0, 0.0, 0.0
			SteveCorpse\State = 6  ; dead state
			DebugLog "Steve corpse spawned at 173"
		EndIf

		; D-klassy trupov ryadom
		DClassCorpse1 = CreateNPC(NPCtypeD, sX - 2.0, sY, sZ + 1.0)
		If DClassCorpse1 <> Null Then
			RotateEntity DClassCorpse1\Collider, 90.0, 45.0, 0.0
			DClassCorpse1\State = 6
		EndIf

		DClassCorpse2 = CreateNPC(NPCtypeD, sX + 1.0, sY, sZ - 1.5)
		If DClassCorpse2 <> Null Then
			RotateEntity DClassCorpse2\Collider, 90.0, -30.0, 0.0
			DClassCorpse2\State = 6
		EndIf
	EndIf

	; trup Harrisona v storage
	Local roomStorage.Rooms = Null
	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If r\RoomTemplate\Name = HarrisonCorpseRoom Then
				roomStorage = r
				Exit
			EndIf
		EndIf
	Next

	If roomStorage <> Null Then
		Local hX# = EntityX(roomStorage\obj)
		Local hY# = 0.1
		Local hZ# = EntityZ(roomStorage\obj) + 1.0

		HarrisonCorpse = CreateNPC(NPCtypeD, hX, hY, hZ)  ; ispolzuem D-class model
		If HarrisonCorpse <> Null Then
			RotateEntity HarrisonCorpse\Collider, 90.0, 0.0, 0.0
			HarrisonCorpse\State = 6

			; spawn luta ryadom s telom
			SpawnHarrisonLoot(hX, hY + 0.5, hZ + 0.5)

			DebugLog "Harrison corpse spawned in storage"
		EndIf
	EndIf
End Function

Function SpawnHarrisonLoot(x#, y#, z#)
	; KPK Harrisona
	Local pda.Items = CreateItem("Harrison's PDA", "misc", x - 0.3, y, z)
	If pda <> Null Then
		HarrisonPDASpawned = True
		DebugLog "Harrison PDA spawned"
	EndIf

	; Klyuch-karta urovnya 4
	Local keycard.Items = CreateItem("Level 4 Key Card", "key4", x + 0.3, y, z)
	If keycard <> Null Then
		HarrisonKeycard4Spawned = True
		DebugLog "Level 4 keycard spawned"
	EndIf
End Function

Function UpdateEmergencyLighting()
	If Not EmergencyLightingActive Then Return

	EmergencyLightTimer = EmergencyLightTimer + FPSfactor

	; miganie kazhdye 2 sekundy
	If EmergencyLightTimer > 140.0 Then
		EmergencyLightTimer = 0.0
		EmergencyLightPhase = 1 - EmergencyLightPhase

		; primenyaem krasnyi otttenok ko vsem svetil'nikam
		UpdateRoomLighting(EmergencyLightPhase)
	EndIf
End Function

Function UpdateRoomLighting(phase%)
	; uproshchennaya versiya - menyaem AmbientLight
	If phase = 0 Then
		AmbientLight 40, 10, 10  ; tyomnyi krasnyi
	Else
		AmbientLight 80, 20, 20  ; svetlyi krasnyi
	EndIf
End Function

Function Update939VoiceTrap()
	If Not Steve939TrapActive Then Return
	If Steve939TrapTriggered Then Return
	If Not GetStoryFlag(FLAG_FOUND_STEVE_BODY) Then Return

	; trigger cherez 10 sekund posle nahozhdeniya tela
	Local trapDelay# = 700.0  ; 10 sec

	; najti igroka
	If PlayerRoom <> Null Then
		If PlayerRoom\RoomTemplate <> Null Then
			If Instr(PlayerRoom\RoomTemplate\Name, "173") > 0 Then
				; igrok vsyo eshche u 173
				Steve939TrapTriggered = True
				StartDialog(220)

				; soobshchit' 939 sisteme
				Trigger939Mimicry("Steve")
			EndIf
		EndIf
	EndIf
End Function

Function Trigger939Mimicry(voiceName$)
	; hook dlya ProjectMirror_939.bb
	; 939 nachinaet imitirovat' golos
	DebugLog "939 mimicry triggered: " + voiceName
End Function

Function TriggerEchoAtSteveBody()
	If GetStoryFlag(FLAG_SAW_ECHO_STEVE) Then Return

	; hook dlya ProjectMirror_Echo.bb
	StartDialog(250)

	; soobshchit' Echo sisteme
	TriggerEchoEvent("Steve", SteveCorpseRoom)
End Function

Function TriggerEchoEvent(characterName$, roomName$)
	; hook dlya ProjectMirror_Echo.bb
	DebugLog "Echo event triggered: " + characterName + " at " + roomName
End Function

Function CheckHarrisonPDAPickup(item.Items)
	If item = Null Then Return

	If item\Name = "Harrison's PDA" Then
		SetStoryFlag(FLAG_COLLECTED_HARRISON_PDA, 1)
		SetStoryFlag(FLAG_HARRISON_PDA, 1)
		HasHarrisonPDA = True

		; pokazat' soobshchenie
		StartDialog(242)

		; aktivirovat' 914 quest (hook)
		On914QuestAdvance("harrison_pda")

		DebugLog "Harrison PDA collected"
	EndIf

	If item\Name = "Level 4 Key Card" Then
		SetStoryFlag(FLAG_COLLECTED_KEYCARD4, 1)
		DebugLog "Level 4 keycard collected"
	EndIf
End Function

Function On914QuestAdvance(questItem$)
	; hook dlya ProjectMirror_914.bb
	DebugLog "914 quest advance: " + questItem
End Function

Function StartDay3Intro()
	If CurrentDay <> 3 Then Return
	If GetStoryFlag(FLAG_DAY3_STARTED) Then Return

	SetStoryFlag(FLAG_DAY3_STARTED, 1)

	; setup mira
	SetupDay3World()

	; spawn v dormah
	SpawnPlayerDay3()

	; dialog probuzhdeniya
	StartDialog(200)

	DebugLog "Day 3 started"
End Function

Function SpawnPlayerDay3()
	Local spawnRoom.Rooms = Null

	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If Instr(r\RoomTemplate\Name, "dorm") > 0 Then
				spawnRoom = r
				Exit
			EndIf
		EndIf
	Next

	If spawnRoom <> Null Then
		Local spawnX# = EntityX(spawnRoom\obj)
		Local spawnY# = 0.5
		Local spawnZ# = EntityZ(spawnRoom\obj)

		PositionEntity Collider, spawnX, spawnY, spawnZ
		ResetEntity Collider
		PlayerRoom = spawnRoom

		DebugLog "Day 3 spawn at dorms"
	Else
		DebugLog "WARNING: dorm room not found for Day 3 spawn"
	EndIf
End Function

Function UpdateDay3Logic()
	If CurrentDay <> 3 Then Return

	; init dnya 3
	If Not GetStoryFlag(FLAG_DAY3_STARTED) Then
		StartDay3Intro()
	EndIf

	; progressiya aktov
	UpdateActProgression()

	; avariynoe osveshchenie
	UpdateEmergencyLighting()

	; sistema rassudka
	UpdateSanityEffects()

	; sanity triggers po sobytiyam
	UpdateSanityTriggers()

	; 939 lovushka
	Update939VoiceTrap()

	; echo pri tele Stiva
	If GetStoryFlag(FLAG_FOUND_STEVE_BODY) And (Not GetStoryFlag(FLAG_SAW_ECHO_STEVE)) Then
		TriggerEchoAtSteveBody()
	EndIf

	; proverka kontsovok
	CheckEndingConditions()

	; subtitry
	UpdateSubtitles()
End Function

Function UpdateSanityTriggers()
	; dobavlyaem sanity pri opredelennykh sobytiyakh

	; videt' trup - +5 sanity
	If GetStoryFlag(FLAG_FOUND_STEVE_BODY) And (Not GetStoryFlag(FLAG_SAW_173_AFTERMATH)) Then
		ModifySanity(15)
		SetStoryFlag(FLAG_SAW_173_AFTERMATH, 1)
	EndIf

	; 939 mimic - +10 sanity
	If GetStoryFlag(FLAG_HEARD_939_MIMIC) And PlayerSanity < 30 Then
		ModifySanity(10)
	EndIf

	; temnota - +1 sanity kazhdye 5 sekund
	; (uproshchennaya proverka)
	If CurrentAct >= ACT_VOICES Then
		If Rand(1, 350) = 1 Then
			ModifySanity(1)
		EndIf
	EndIf
End Function

Function CleanupDay3()
	If SteveCorpse <> Null Then
		RemoveNPC(SteveCorpse)
		SteveCorpse = Null
	EndIf

	If HarrisonCorpse <> Null Then
		RemoveNPC(HarrisonCorpse)
		HarrisonCorpse = Null
	EndIf

	If DClassCorpse1 <> Null Then
		RemoveNPC(DClassCorpse1)
		DClassCorpse1 = Null
	EndIf

	If DClassCorpse2 <> Null Then
		RemoveNPC(DClassCorpse2)
		DClassCorpse2 = Null
	EndIf

	If Day3AlarmChannel <> 0 Then
		StopChannel Day3AlarmChannel
		Day3AlarmChannel = 0
	EndIf

	Day3Initialized = False
	EmergencyLightingActive = False

	DebugLog "Day 3 cleanup complete"
End Function

; ============================================================================
; SANITY SYSTEM - Shkala Rassudka
; ============================================================================

Function ModifySanity(amount%)
	PlayerSanity = PlayerSanity + amount
	If PlayerSanity < 0 Then PlayerSanity = 0
	If PlayerSanity > SANITY_MAX Then PlayerSanity = SANITY_MAX
End Function

Function GetSanityLevel%()
	If PlayerSanity < SANITY_ANXIETY Then Return 0  ; normal
	If PlayerSanity < SANITY_PARANOIA Then Return 1  ; trevoga
	If PlayerSanity < SANITY_HYSTERIA Then Return 2  ; paranoya
	Return 3  ; isteriya
End Function

Function UpdateSanityEffects()
	If CurrentDay <> 3 Then Return

	SanityEffectTimer = SanityEffectTimer + FPSfactor
	Local level% = GetSanityLevel()

	Select level
		Case 0  ; normal
			SanityHallucinationActive = False
			SanityPhantomVisible = False

		Case 1  ; trevoga (30-70%)
			; tyazheloye dykhaniye, legkoye vin'etirovaniye
			If SanityEffectTimer > 280.0 Then
				SanityEffectTimer = 0.0
				; shans fantoma
				If Rand(1, 100) < 10 Then
					SanityPhantomVisible = True
				EndIf
			EndIf

		Case 2  ; paranoya (70-100%)
			; zvukovye gallyutsinatsii
			If SanityEffectTimer > 350.0 Then
				SanityEffectTimer = 0.0
				TriggerParanoiaEffect()
			EndIf

		Case 3  ; isteriya (100%)
			; polnoe iskaženie
			If SanityEffectTimer > 140.0 Then
				SanityEffectTimer = 0.0
				TriggerHysteriaEffect()
			EndIf
	End Select
End Function

Function TriggerParanoiaEffect()
	Local effect% = Rand(1, 4)

	Select effect
		Case 1  ; zvuk otkryvayushcheisya dveri
			PlaySound LoadSound("SFX\Door\Open.ogg")
		Case 2  ; shagi MTF
			PlaySound LoadSound("SFX\Step\Run1.ogg")
		Case 3  ; fantom v uglu zreniya
			SanityPhantomVisible = True
		Case 4  ; pomekhi ratsii
			ShowSubtitle("[RATSIYA]", "*statika*", 70.0)
	End Select
End Function

Function TriggerHysteriaEffect()
	Local effect% = Rand(1, 3)

	Select effect
		Case 1  ; steny "dyshat"
			; budet realizovano v renderе
			SanityHallucinationActive = True
		Case 2  ; skrimer - litso Stiva vmesto monstra
			SanityHallucinationActive = True
		Case 3  ; ruki tryasutsya
			; debaf na strelybu
			SanityHallucinationActive = True
	End Select
End Function

Function RenderSanityEffects()
	If CurrentDay <> 3 Then Return

	Local level% = GetSanityLevel()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; vin'etka
	If level >= 1 Then
		Local alpha% = 30 + level * 20
		Color 0, 0, 0
		; ugly no prostoy sposob
	EndIf

	; fantom
	If SanityPhantomVisible Then
		; risuem prizrachnuyu figuru v uglu
		Color 100, 100, 100
		Text gw - 150, gh / 2, "[???]"

		; ischez cherez sekundu
		SanityPhantomVisible = False
	EndIf

	; debug
	If DebugHUD Then
		Color 255, 100, 100
		Text 10, 580, "Sanity: " + PlayerSanity + "% (Lvl " + level + ")"
	EndIf
End Function

; ============================================================================
; ACT PROGRESSION
; ============================================================================

Function UpdateActProgression()
	If CurrentDay <> 3 Then Return

	; opredelyaem tekushchiy akt po flagam
	If GetStoryFlag(FLAG_ACT6_MTF_BETRAYAL) Then
		CurrentAct = ACT_FINALE
	ElseIf GetStoryFlag(FLAG_ACT5_ELEVATOR_ESCAPE) Then
		CurrentAct = ACT_SURFACE
	ElseIf GetStoryFlag(FLAG_ACT4_UPGRADED_CARD) Then
		CurrentAct = ACT_FLOOR
	ElseIf GetStoryFlag(FLAG_ACT3_READ_MIRROR_LOG) Then
		CurrentAct = ACT_MACHINE
	ElseIf GetStoryFlag(FLAG_ACT2_HEARD_STEVE_LAST) Then
		CurrentAct = ACT_VOICES
	ElseIf GetStoryFlag(FLAG_ACT1_RADIO_LOOP) Then
		CurrentAct = ACT_ECHO
	Else
		CurrentAct = ACT_AWAKENING
	EndIf
End Function

Function GetActName$(act%)
	Select act
		Case ACT_AWAKENING
			Return "AWAKENING"
		Case ACT_ECHO
			Return "ECHOES OF THE PAST"
		Case ACT_VOICES
			Return "VOICES OF FRIENDS"
		Case ACT_MACHINE
			Return "THE MACHINE AND THE PLAGUE"
		Case ACT_FLOOR
			Return "LOOK AT THE FLOOR"
		Case ACT_SURFACE
			Return "THE SURFACE"
		Case ACT_FINALE
			Return "FINALE"
	End Select
	Return "???"
End Function

; ============================================================================
; ENDING SYSTEM
; ============================================================================

Function TriggerEnding(endingType%)
	Select endingType
		Case 1  ; Whistleblower
			SetStoryFlag(FLAG_ENDING_WHISTLEBLOWER, 1)
			StartDialog(320)
		Case 2  ; Symbiosis
			SetStoryFlag(FLAG_ENDING_SYMBIOSIS, 1)
			StartDialog(330)
		Case 3  ; Death
			SetStoryFlag(FLAG_ENDING_DEATH, 1)
			StartDialog(340)
		Case 4  ; Zero Protocol
			SetStoryFlag(FLAG_ENDING_ZERO_PROTOCOL, 1)
			StartDialog(350)
	End Select
End Function

Function CheckEndingConditions()
	If CurrentAct <> ACT_FINALE Then Return

	; proverka uslovii dlya kontsovok
	; logika vybora budet cherez dialogi
End Function
