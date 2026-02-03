Const MAX_LEARNED_VOICES% = 32
Const MAX_VOICE_CATEGORIES% = 8
Const VOICE_CACHE_SIZE% = 64

Const VOICE_CAT_STEVE% = 0
Const VOICE_CAT_GUARD% = 1
Const VOICE_CAT_SCIENTIST% = 2
Const VOICE_CAT_MTF% = 3
Const VOICE_CAT_D_CLASS% = 4
Const VOICE_CAT_HARRISON% = 5
Const VOICE_CAT_INTERCOM% = 6
Const VOICE_CAT_UNKNOWN% = 7

Const LURE_STATE_IDLE% = 0
Const LURE_STATE_SELECTING% = 1
Const LURE_STATE_PLAYING% = 2
Const LURE_STATE_COOLDOWN% = 3

Const LURE_MIN_DISTANCE# = 5.0
Const LURE_MAX_DISTANCE# = 25.0
Const LURE_MIN_DISTANCE_SQ# = 25.0
Const LURE_MAX_DISTANCE_SQ# = 625.0
Const LURE_OPTIMAL_DISTANCE# = 12.0
Const LURE_OPTIMAL_DISTANCE_SQ# = 144.0

Type LearnedVoice
	Field id%
	Field category%
	Field soundPath$
	Field dayLearned%
	Field speakerName$
	Field emotionalWeight%
	Field useCount%
	Field lastUsedTime%
	Field isPersonal%
End Type

Type SCP939VoiceState
	Field npcRef.NPCs
	Field lureState%
	Field lureTimer#
	Field lureCooldown#
	Field currentVoice.LearnedVoice
	Field currentSoundChannel%
	Field currentSound%
	Field preferredCategory%
	Field learningActive%
	Field aggressionLevel%
	Field lastPlayerDistSq#
	Field playerApproaching%
	Field lurePivot%
End Type

Global VoiceMimicryEnabled% = True
Global VoiceLearningActive% = True
Global TotalLearnedVoices% = 0

Dim VoiceCacheByCategory.LearnedVoice(MAX_VOICE_CATEGORIES, MAX_LEARNED_VOICES)
Dim VoiceCacheCounts%(MAX_VOICE_CATEGORIES)

Global SteveVoiceCount% = 0
Dim SteveVoicePaths$(16)

Global LureVolumeBase# = 0.8
Global LureVolumeMin# = 0.2
Global LureAttenuationRate# = 0.04

Function InitVoiceMimicrySystem()
	For cat% = 0 To MAX_VOICE_CATEGORIES - 1
		VoiceCacheCounts(cat) = 0
		For i% = 0 To MAX_LEARNED_VOICES - 1
			VoiceCacheByCategory(cat, i) = Null
		Next
	Next

	TotalLearnedVoices = 0

	RegisterSteveVoices()
	RegisterGuardVoices()
	RegisterScientistVoices()

	VoiceMimicryEnabled = True
	VoiceLearningActive = True
End Function

Function RegisterSteveVoices()
	SteveVoiceCount = 0
	SteveVoicePaths(0) = "SFX\Character\Steve\Greeting.ogg"
	SteveVoicePaths(1) = "SFX\Character\Steve\Help1.ogg"
	SteveVoicePaths(2) = "SFX\Character\Steve\Help2.ogg"
	SteveVoicePaths(3) = "SFX\Character\Steve\Warning.ogg"
	SteveVoicePaths(4) = "SFX\Character\Steve\Fear1.ogg"
	SteveVoicePaths(5) = "SFX\Character\Steve\Fear2.ogg"
	SteveVoicePaths(6) = "SFX\Character\Steve\Plan.ogg"
	SteveVoicePaths(7) = "SFX\Character\Steve\Trust.ogg"
	SteveVoicePaths(8) = "SFX\Character\Steve\Escape.ogg"
	SteveVoicePaths(9) = "SFX\Character\Steve\LastWords1.ogg"
	SteveVoicePaths(10) = "SFX\Character\Steve\LastWords2.ogg"
	SteveVoicePaths(11) = "SFX\Character\Steve\Running.ogg"
	SteveVoiceCount = 12
End Function

Function RegisterGuardVoices()
	LearnVoiceFromPath("SFX\Character\Guard\Halt.ogg", VOICE_CAT_GUARD, "Guard", 30, False)
	LearnVoiceFromPath("SFX\Character\Guard\Freeze.ogg", VOICE_CAT_GUARD, "Guard", 35, False)
	LearnVoiceFromPath("SFX\Character\Guard\Radio1.ogg", VOICE_CAT_GUARD, "Guard", 25, False)
End Function

Function RegisterScientistVoices()
	LearnVoiceFromPath("SFX\Character\Scientist\Help.ogg", VOICE_CAT_SCIENTIST, "Scientist", 40, False)
	LearnVoiceFromPath("SFX\Character\Scientist\Plea.ogg", VOICE_CAT_SCIENTIST, "Scientist", 50, False)
End Function

Function LearnVoice.LearnedVoice(soundPath$, category%, speakerName$, emotionalWeight%, isPersonal%)
	If Not VoiceLearningActive Then Return Null
	If TotalLearnedVoices >= VOICE_CACHE_SIZE Then Return Null

	For v.LearnedVoice = Each LearnedVoice
		If v\soundPath = soundPath Then Return v
	Next

	If VoiceCacheCounts(category) >= MAX_LEARNED_VOICES Then Return Null

	Local voice.LearnedVoice = New LearnedVoice
	voice\id = TotalLearnedVoices
	voice\category = category
	voice\soundPath = soundPath
	voice\dayLearned = CurrentDay
	voice\speakerName = speakerName
	voice\emotionalWeight = emotionalWeight
	voice\useCount = 0
	voice\lastUsedTime = 0
	voice\isPersonal = isPersonal

	VoiceCacheByCategory(category, VoiceCacheCounts(category)) = voice
	VoiceCacheCounts(category) = VoiceCacheCounts(category) + 1

	TotalLearnedVoices = TotalLearnedVoices + 1

	Return voice
End Function

Function LearnVoiceFromPath.LearnedVoice(soundPath$, category%, speakerName$, emotionalWeight%, isPersonal%)
	Return LearnVoice(soundPath, category, speakerName, emotionalWeight, isPersonal)
End Function

Function OnDialogPlayed(soundPath$, speakerName$, room.Rooms)
	If Not VoiceLearningActive Then Return
	If CurrentDay > 2 Then Return

	Local category% = VOICE_CAT_UNKNOWN
	Local emotional% = 30
	Local personal% = False

	Select Lower(speakerName)
		Case "steve", "d-9341's friend"
			category = VOICE_CAT_STEVE
			emotional = 80
			personal = True
		Case "guard", "security guard"
			category = VOICE_CAT_GUARD
			emotional = 40
		Case "scientist", "dr.", "researcher"
			category = VOICE_CAT_SCIENTIST
			emotional = 45
		Case "mtf", "mobile task force"
			category = VOICE_CAT_MTF
			emotional = 50
		Case "d-class", "prisoner"
			category = VOICE_CAT_D_CLASS
			emotional = 35
		Case "harrison", "dr. harrison"
			category = VOICE_CAT_HARRISON
			emotional = 60
			personal = True
	End Select

	LearnVoice(soundPath, category, speakerName, emotional, personal)
End Function

Function LearnAllSteveVoices()
	For i% = 0 To SteveVoiceCount - 1
		Local emotional% = 70 + Rand(-10, 20)
		LearnVoice(SteveVoicePaths(i), VOICE_CAT_STEVE, "Steve", emotional, True)
	Next
	SetStoryFlag(FLAG_STEVE_MET, 1)
End Function

Function CreateVoiceState.SCP939VoiceState(n.NPCs)
	If n = Null Then Return Null
	If n\NPCtype <> NPCtype939 Then Return Null

	Local state.SCP939VoiceState = New SCP939VoiceState
	state\npcRef = n
	state\lureState = LURE_STATE_IDLE
	state\lureTimer = 0.0
	state\lureCooldown = 0.0
	state\currentVoice = Null
	state\currentSoundChannel = 0
	state\currentSound = 0
	state\preferredCategory = VOICE_CAT_STEVE
	state\learningActive = True
	state\aggressionLevel = 50
	state\lastPlayerDistSq = 10000.0
	state\playerApproaching = False

	state\lurePivot = CreatePivot()
	If n\Collider <> 0 Then
		EntityParent state\lurePivot, n\Collider
		PositionEntity state\lurePivot, 0, 0.5, 0
	EndIf

	Return state
End Function

Function GetVoiceStateFor939.SCP939VoiceState(n.NPCs)
	If n = Null Then Return Null

	For state.SCP939VoiceState = Each SCP939VoiceState
		If state\npcRef = n Then Return state
	Next

	Return CreateVoiceState(n)
End Function

Function UpdateSCP939VoiceMimicry(n.NPCs)
	If n = Null Then Return
	If n\NPCtype <> NPCtype939 Then Return
	If Not VoiceMimicryEnabled Then Return
	If CurrentDay <> 3 Then Return

	If n\State = 3 Then Return

	Local state.SCP939VoiceState = GetVoiceStateFor939(n)
	If state = Null Then Return

	Local distSq# = 10000.0
	If Collider <> 0 And n\Collider <> 0 Then
		Local dx# = EntityX(Collider, True) - EntityX(n\Collider, True)
		Local dy# = EntityY(Collider, True) - EntityY(n\Collider, True)
		Local dz# = EntityZ(Collider, True) - EntityZ(n\Collider, True)
		distSq = dx*dx + dy*dy + dz*dz
	EndIf

	If distSq < state\lastPlayerDistSq - 0.1 Then
		state\playerApproaching = True
	Else
		state\playerApproaching = False
	EndIf
	state\lastPlayerDistSq = distSq

	Select state\lureState

		Case LURE_STATE_IDLE
			If distSq > LURE_MIN_DISTANCE_SQ And distSq < LURE_MAX_DISTANCE_SQ Then
				Local lureChance# = CalculateLureChance(state, distSq)
				If Rnd(0.0, 100.0) < lureChance * FPSfactor * 0.1 Then
					state\lureState = LURE_STATE_SELECTING
					state\lureTimer = 0.0
				EndIf
			EndIf

		Case LURE_STATE_SELECTING
			state\currentVoice = SelectLureVoice(state)

			If state\currentVoice <> Null Then
				state\currentSound = LoadSound(state\currentVoice\soundPath)
				If state\currentSound <> 0 Then
					PositionLurePivot(state, n)
					Local volume# = CalculateLureVolume(distSq)
					state\currentSoundChannel = EmitSound(state\currentSound, state\lurePivot)
					If state\currentSoundChannel <> 0 Then
						ChannelVolume state\currentSoundChannel, volume
					EndIf
					state\currentVoice\useCount = state\currentVoice\useCount + 1
					state\currentVoice\lastUsedTime = MilliSecs()
					state\lureState = LURE_STATE_PLAYING
					state\lureTimer = 0.0
				Else
					state\lureState = LURE_STATE_COOLDOWN
					state\lureCooldown = 350.0
				EndIf
			Else
				state\lureState = LURE_STATE_COOLDOWN
				state\lureCooldown = 700.0
			EndIf

		Case LURE_STATE_PLAYING
			state\lureTimer = state\lureTimer + FPSfactor

			If state\lurePivot <> 0 Then
				PositionLurePivot(state, n)
			EndIf

			If state\currentSoundChannel <> 0 Then
				Local newVolume# = CalculateLureVolume(distSq)
				ChannelVolume state\currentSoundChannel, newVolume
			EndIf

			If state\currentSoundChannel = 0 Or (Not ChannelPlaying(state\currentSoundChannel)) Then
				If state\currentSound <> 0 Then
					FreeSound state\currentSound
					state\currentSound = 0
				EndIf
				state\currentSoundChannel = 0
				state\lureState = LURE_STATE_COOLDOWN

				If state\playerApproaching Then
					state\lureCooldown = 210.0 + Rnd(0.0, 140.0)
					state\aggressionLevel = Min(state\aggressionLevel + 10, 100)
				Else
					state\lureCooldown = 490.0 + Rnd(0.0, 350.0)
				EndIf
			EndIf

			If state\lureTimer > 700.0 Then
				StopLure(state)
				state\lureState = LURE_STATE_COOLDOWN
				state\lureCooldown = 350.0
			EndIf

		Case LURE_STATE_COOLDOWN
			state\lureCooldown = state\lureCooldown - FPSfactor
			If state\lureCooldown <= 0.0 Then
				state\lureState = LURE_STATE_IDLE
				state\lureCooldown = 0.0
			EndIf
	End Select
End Function

Function SelectLureVoice.LearnedVoice(state.SCP939VoiceState)
	If TotalLearnedVoices = 0 Then Return Null

	Local preferredCat% = state\preferredCategory

	If GetStoryFlag(FLAG_STEVE_DEAD) Then
		preferredCat = VOICE_CAT_STEVE
	EndIf

	If VoiceCacheCounts(preferredCat) > 0 Then
		If Rand(1, 100) <= 70 Then
			Return SelectFromCategory(preferredCat)
		EndIf
	EndIf

	; Count personal voices and maybe select one
	Local personalCount% = 0
	Local selectedPersonal.LearnedVoice = Null

	For v.LearnedVoice = Each LearnedVoice
		If v\isPersonal Then
			personalCount = personalCount + 1
			; Random selection with decreasing probability
			If Rand(1, personalCount) = 1 Then
				selectedPersonal = v
			EndIf
		EndIf
	Next

	If personalCount > 0 And Rand(1, 100) <= 60 Then
		Return selectedPersonal
	EndIf

	Return SelectWeightedRandom()
End Function

Function SelectFromCategory.LearnedVoice(category%)
	If VoiceCacheCounts(category) = 0 Then Return Null
	Local index% = Rand(0, VoiceCacheCounts(category) - 1)
	Return VoiceCacheByCategory(category, index)
End Function

Function SelectWeightedRandom.LearnedVoice()
	Local totalWeight% = 0
	For v.LearnedVoice = Each LearnedVoice
		totalWeight = totalWeight + v\emotionalWeight
	Next

	If totalWeight = 0 Then Return Null

	Local roll% = Rand(1, totalWeight)
	Local cumulative% = 0

	For v.LearnedVoice = Each LearnedVoice
		cumulative = cumulative + v\emotionalWeight
		If roll <= cumulative Then Return v
	Next

	Return First LearnedVoice
End Function

Function CalculateLureChance#(state.SCP939VoiceState, distSq#)
	Local distFactor# = 1.0

	If distSq < LURE_OPTIMAL_DISTANCE_SQ Then
		distFactor = distSq / LURE_OPTIMAL_DISTANCE_SQ
	Else
		distFactor = LURE_OPTIMAL_DISTANCE_SQ / distSq
	EndIf

	Local aggroFactor# = Float(state\aggressionLevel) / 50.0
	Local dayBonus# = 1.5

	Local chance# = 2.0 * distFactor * aggroFactor * dayBonus

	If state\playerApproaching Then
		chance = chance * 1.5
	EndIf

	Return chance
End Function

Function CalculateLureVolume#(distSq#)
	Local dist# = Sqr(distSq)
	If dist < LURE_MIN_DISTANCE Then dist = LURE_MIN_DISTANCE

	Local attenuation# = LURE_OPTIMAL_DISTANCE / dist
	If attenuation > 1.0 Then attenuation = 1.0

	Local volume# = LureVolumeBase * attenuation
	If volume < LureVolumeMin Then volume = LureVolumeMin
	If volume > 1.0 Then volume = 1.0

	Return volume
End Function

Function PositionLurePivot(state.SCP939VoiceState, n.NPCs)
	If state\lurePivot = 0 Then Return
	If n\Collider = 0 Then Return

	Local npcX# = EntityX(n\Collider, True)
	Local npcY# = EntityY(n\Collider, True)
	Local npcZ# = EntityZ(n\Collider, True)

	Local playerX# = EntityX(Collider, True)
	Local playerZ# = EntityZ(Collider, True)

	Local dx# = playerX - npcX
	Local dz# = playerZ - npcZ
	Local len# = Sqr(dx*dx + dz*dz)

	If len > 0.1 Then
		dx = dx / len
		dz = dz / len
	EndIf

	Local pivotX# = npcX + dx * 2.0
	Local pivotY# = npcY + 0.5
	Local pivotZ# = npcZ + dz * 2.0

	PositionEntity state\lurePivot, pivotX, pivotY, pivotZ, True
End Function

Function StopLure(state.SCP939VoiceState)
	If state\currentSoundChannel <> 0 Then
		StopChannel state\currentSoundChannel
		state\currentSoundChannel = 0
	EndIf
	If state\currentSound <> 0 Then
		FreeSound state\currentSound
		state\currentSound = 0
	EndIf
	state\currentVoice = Null
End Function

Function StopAll939Lures()
	For state.SCP939VoiceState = Each SCP939VoiceState
		StopLure(state)
		state\lureState = LURE_STATE_COOLDOWN
		state\lureCooldown = 140.0
	Next
End Function

Function Set939Aggression(n.NPCs, level%)
	Local state.SCP939VoiceState = GetVoiceStateFor939(n)
	If state <> Null Then
		state\aggressionLevel = Max(0, Min(100, level))
	EndIf
End Function

Function Set939PreferredVoice(n.NPCs, category%)
	Local state.SCP939VoiceState = GetVoiceStateFor939(n)
	If state <> Null Then
		state\preferredCategory = category
	EndIf
End Function

Function Integrate939VoiceMimicry(n.NPCs)
	If n\State = 1 Or n\State = 2 Then
		UpdateSCP939VoiceMimicry(n)
	EndIf

	If n\State = 3 Then
		Local state.SCP939VoiceState = GetVoiceStateFor939(n)
		If state <> Null And state\lureState = LURE_STATE_PLAYING Then
			StopLure(state)
			state\lureState = LURE_STATE_COOLDOWN
			state\lureCooldown = 700.0
		EndIf
	EndIf
End Function

Function SaveVoiceMimicryState(file%)
	WriteInt file, TotalLearnedVoices

	For v.LearnedVoice = Each LearnedVoice
		WriteInt file, v\id
		WriteInt file, v\category
		WriteLine file, v\soundPath
		WriteInt file, v\dayLearned
		WriteLine file, v\speakerName
		WriteInt file, v\emotionalWeight
		WriteInt file, v\useCount
		WriteInt file, v\isPersonal
	Next

	Local stateCount% = 0
	For state.SCP939VoiceState = Each SCP939VoiceState
		stateCount = stateCount + 1
	Next
	WriteInt file, stateCount

	For state.SCP939VoiceState = Each SCP939VoiceState
		If state\npcRef <> Null Then
			WriteInt file, state\npcRef\ID
		Else
			WriteInt file, -1
		EndIf
		WriteInt file, state\preferredCategory
		WriteInt file, state\aggressionLevel
	Next
End Function

Function LoadVoiceMimicryState(file%)
	Local oldVoice.LearnedVoice
	For oldVoice = Each LearnedVoice
		Delete oldVoice
	Next
	For cat% = 0 To MAX_VOICE_CATEGORIES - 1
		VoiceCacheCounts(cat) = 0
	Next

	TotalLearnedVoices = ReadInt(file)

	For i% = 0 To TotalLearnedVoices - 1
		Local newVoice.LearnedVoice = New LearnedVoice
		newVoice\id = ReadInt(file)
		newVoice\category = ReadInt(file)
		newVoice\soundPath = ReadLine(file)
		newVoice\dayLearned = ReadInt(file)
		newVoice\speakerName = ReadLine(file)
		newVoice\emotionalWeight = ReadInt(file)
		newVoice\useCount = ReadInt(file)
		newVoice\isPersonal = ReadInt(file)
		newVoice\lastUsedTime = 0

		If newVoice\category >= 0 And newVoice\category < MAX_VOICE_CATEGORIES Then
			If VoiceCacheCounts(newVoice\category) < MAX_LEARNED_VOICES Then
				VoiceCacheByCategory(newVoice\category, VoiceCacheCounts(newVoice\category)) = newVoice
				VoiceCacheCounts(newVoice\category) = VoiceCacheCounts(newVoice\category) + 1
			EndIf
		EndIf
	Next

	Local stateCount% = ReadInt(file)
	For i% = 0 To stateCount - 1
		Local npcID% = ReadInt(file)
		Local prefCat% = ReadInt(file)
		Local aggro% = ReadInt(file)

		For n.NPCs = Each NPCs
			If n\ID = npcID And n\NPCtype = NPCtype939 Then
				Local state.SCP939VoiceState = GetVoiceStateFor939(n)
				If state <> Null Then
					state\preferredCategory = prefCat
					state\aggressionLevel = aggro
				EndIf
				Exit
			EndIf
		Next
	Next
End Function

Function CleanupVoiceMimicrySystem()
	For state.SCP939VoiceState = Each SCP939VoiceState
		StopLure(state)
		If state\lurePivot <> 0 Then FreeEntity state\lurePivot
		Delete state
	Next

	For v.LearnedVoice = Each LearnedVoice
		Delete v
	Next

	For cat% = 0 To MAX_VOICE_CATEGORIES - 1
		VoiceCacheCounts(cat) = 0
		For i% = 0 To MAX_LEARNED_VOICES - 1
			VoiceCacheByCategory(cat, i) = Null
		Next
	Next

	TotalLearnedVoices = 0
End Function

Function DebugVoiceMimicry()
	Color 0, 255, 200
	Text 200, 10, "=== 939 VOICE ==="
	Text 200, 25, "Learned: " + TotalLearnedVoices
	Text 200, 40, "Steve: " + VoiceCacheCounts(VOICE_CAT_STEVE)
	Text 200, 55, "Guard: " + VoiceCacheCounts(VOICE_CAT_GUARD)
	Text 200, 70, "Sci: " + VoiceCacheCounts(VOICE_CAT_SCIENTIST)

	Local y% = 90
	For state.SCP939VoiceState = Each SCP939VoiceState
		Local stateName$ = ""
		Select state\lureState
			Case LURE_STATE_IDLE : stateName = "IDLE"
			Case LURE_STATE_SELECTING : stateName = "SELECT"
			Case LURE_STATE_PLAYING : stateName = "PLAY"
			Case LURE_STATE_COOLDOWN : stateName = "CD"
		End Select

		Local npcID% = 0
		If state\npcRef <> Null Then npcID = state\npcRef\ID

		Text 200, y, "939#" + npcID + ": " + stateName + " agg=" + state\aggressionLevel
		y = y + 15

		If state\currentVoice <> Null And state\lureState = LURE_STATE_PLAYING Then
			Text 210, y, "  >> " + state\currentVoice\speakerName
			y = y + 15
		EndIf
	Next
End Function
