Const CARD_LEVEL_0% = 0
Const CARD_LEVEL_1% = 1
Const CARD_LEVEL_2% = 2
Const CARD_LEVEL_3% = 3
Const CARD_LEVEL_4% = 4
Const CARD_LEVEL_5% = 5
Const CARD_LEVEL_O5% = 6
Const CARD_LEVEL_OMNI% = 7

Const REFINE_ROUGH% = 0
Const REFINE_COARSE% = 1
Const REFINE_1TO1% = 2
Const REFINE_FINE% = 3
Const REFINE_VERY_FINE% = 4

Const MAX_SPECIAL_RECIPES% = 32

Type SpecialRecipe
	Field id%
	Field inputItemName$
	Field inputTempName$
	Field setting%
	Field requiredFlag%
	Field requiredFlagValue%
	Field outputItemName$
	Field outputTempName$
	Field karmaChange%
	Field flagToSet%
	Field flagValueToSet%
	Field failOutputName$
	Field failTempName$
	Field failMessage$
	Field successMessage$
End Type

Global Mirror914Enabled% = True
Global Mirror914LastResult$ = ""
Global Mirror914LastSuccess% = False

Global HarrisonPDAInIntake% = False
Global HarrisonEyeInIntake% = False
Global Level4CardInIntake% = False

Dim SpecialRecipes.SpecialRecipe(MAX_SPECIAL_RECIPES)
Global SpecialRecipeCount% = 0

Function InitMirror914System()
	SpecialRecipeCount = 0
	RegisterMirrorRecipes()
	Mirror914Enabled = True
	HarrisonPDAInIntake = False
	HarrisonEyeInIntake = False
	Level4CardInIntake = False
End Function

Function CreateSpecialRecipe.SpecialRecipe(inputName$, inputTemp$, setting%, outputName$, outputTemp$)
	If SpecialRecipeCount >= MAX_SPECIAL_RECIPES Then Return Null

	Local r.SpecialRecipe = New SpecialRecipe
	r\id = SpecialRecipeCount
	r\inputItemName = inputName
	r\inputTempName = inputTemp
	r\setting = setting
	r\outputItemName = outputName
	r\outputTempName = outputTemp
	r\requiredFlag = -1
	r\requiredFlagValue = 0
	r\karmaChange = 0
	r\flagToSet = -1
	r\flagValueToSet = 0
	r\failOutputName = ""
	r\failTempName = ""
	r\failMessage = ""
	r\successMessage = ""

	SpecialRecipes(SpecialRecipeCount) = r
	SpecialRecipeCount = SpecialRecipeCount + 1

	Return r
End Function

Function SetRecipeRequirement(r.SpecialRecipe, flagIndex%, flagValue%)
	If r <> Null Then
		r\requiredFlag = flagIndex
		r\requiredFlagValue = flagValue
	EndIf
End Function

Function SetRecipeFailure(r.SpecialRecipe, failItemName$, failTempName$, failMsg$)
	If r <> Null Then
		r\failOutputName = failItemName
		r\failTempName = failTempName
		r\failMessage = failMsg
	EndIf
End Function

Function SetRecipeSuccess(r.SpecialRecipe, successMsg$, karmaChange%, flagToSet%, flagValue%)
	If r <> Null Then
		r\successMessage = successMsg
		r\karmaChange = karmaChange
		r\flagToSet = flagToSet
		r\flagValueToSet = flagValue
	EndIf
End Function

Function RegisterMirrorRecipes()
	Local r.SpecialRecipe

	r = CreateSpecialRecipe("Key Card", "key4", REFINE_FINE, "Key Card", "okey5")
	SetRecipeRequirement(r, FLAG_HARRISON_PDA, 1)
	SetRecipeFailure(r, "Key Card", "key3", "914: Insufficient authorization data. Output degraded.")
	SetRecipeSuccess(r, "914: O5 Council authorization protocols reconstructed.", 15, FLAG_O5_CARD_OBTAINED, 1)

	r = CreateSpecialRecipe("Key Card", "key4", REFINE_VERY_FINE, "Key Card", "okey6")
	SetRecipeRequirement(r, FLAG_HARRISON_EYE, 1)
	SetRecipeFailure(r, "Burnt Key Card", "", "914: Biometric data incomplete. Card destroyed.")
	SetRecipeSuccess(r, "914: Full administrative access compiled. Omni-clearance granted.", 20, -1, 0)

	r = CreateSpecialRecipe("Key Card", "key3", REFINE_FINE, "Key Card", "key4")

	r = CreateSpecialRecipe("Harrison's PDA", "harrisonpda", REFINE_FINE, "Decoded PDA", "decodedpda")
	SetRecipeSuccess(r, "914: Data decryption complete. Authorization codes extracted.", 5, FLAG_HARRISON_PDA, 1)

	r = CreateSpecialRecipe("Harrison's PDA", "harrisonpda", REFINE_VERY_FINE, "Decoded PDA", "decodedpda")
	SetRecipeSuccess(r, "914: Full data extraction. Hidden files revealed.", 10, FLAG_HARRISON_PDA, 1)

	r = CreateSpecialRecipe("Eyeball", "harrisoneye", REFINE_1TO1, "Preserved Eye", "preservedeye")
	SetRecipeSuccess(r, "914: Tissue preserved. Biometric data intact.", 0, FLAG_HARRISON_EYE, 1)

	r = CreateSpecialRecipe("Eyeball", "harrisoneye", REFINE_FINE, "Enhanced Eye", "enhancedeye")
	SetRecipeSuccess(r, "914: Optical enhancement complete. Retinal patterns amplified.", 0, FLAG_HARRISON_EYE, 1)

	r = CreateSpecialRecipe("SCP-500-01", "scp500", REFINE_VERY_FINE, "Refined Panacea", "scp500vf")
	SetRecipeRequirement(r, FLAG_049_CURED, 0)
	SetRecipeSuccess(r, "914: Molecular structure enhanced. Cure potential amplified.", 5, -1, 0)

	r = CreateSpecialRecipe("Identification Card", "steveid", REFINE_FINE, "Memorial Badge", "memorial")
	SetRecipeRequirement(r, FLAG_STEVE_DEAD, 1)
	SetRecipeSuccess(r, "914: Personal item preserved. Memory persists.", -5, -1, 0)

	r = CreateSpecialRecipe("Radio", "fineradio", REFINE_VERY_FINE, "Encrypted Radio", "encradio")
	SetRecipeSuccess(r, "914: Encryption protocols installed. Secure channel enabled.", 5, FLAG_MTF_CONTACTED, 1)

	r = CreateSpecialRecipe("Broken Camera", "brokencam", REFINE_FINE, "Spectral Camera", "spectralcam")
	SetRecipeRequirement(r, FLAG_ECHO_STEVE_SEEN, 1)
	SetRecipeSuccess(r, "914: Temporal imaging capability restored.", 10, -1, 0)

	r = CreateSpecialRecipe("SCP-714", "scp714", REFINE_FINE, "Warding Ring", "wardingring")

	r = CreateSpecialRecipe("SCP-714", "scp714", REFINE_VERY_FINE, "Tainted Ring", "taintedring")
End Function

Function ProcessMirror914%(item.Items, setting$, x#, y#, z#)
	If Not Mirror914Enabled Then Return False
	If item = Null Then Return False
	If item\itemtemplate = Null Then Return False

	Local itemName$ = item\itemtemplate\name
	Local tempName$ = item\itemtemplate\tempname
	Local settingNum% = GetSettingNumber(setting)

	TrackIntakeItems(item, True)

	Local r.SpecialRecipe = FindMatchingRecipe(itemName, tempName, settingNum)

	If r <> Null Then
		Local requirementsMet% = True

		If r\requiredFlag >= 0 Then
			If GetStoryFlag(r\requiredFlag) <> r\requiredFlagValue Then
				requirementsMet = False
			EndIf
		EndIf

		If tempName = "scp714" Then
			If settingNum = REFINE_FINE And CurrentKarma < 25 Then
				requirementsMet = False
			EndIf
			If settingNum = REFINE_VERY_FINE And CurrentKarma > -25 Then
				requirementsMet = False
			EndIf
		EndIf

		If requirementsMet Then
			ExecuteSuccessRecipe(r, item, x, y, z)
			Return True
		Else
			ExecuteFailureRecipe(r, item, x, y, z)
			Return True
		EndIf
	EndIf

	Return ProcessConditionalVanilla(item, settingNum, x, y, z)
End Function

Function ExecuteSuccessRecipe(r.SpecialRecipe, item.Items, x#, y#, z#)
	RemoveItem(item)

	If r\outputItemName <> "" And r\outputTempName <> "" Then
		Local newItem.Items = CreateItem(r\outputItemName, r\outputTempName, x, y, z)
	EndIf

	If r\karmaChange <> 0 Then ModifyKarma(r\karmaChange)
	If r\flagToSet >= 0 Then SetStoryFlag(r\flagToSet, r\flagValueToSet)

	If r\successMessage <> "" Then
		Mirror914LastResult = r\successMessage
		Mirror914LastSuccess = True
	EndIf

	RefinedItems = RefinedItems + 1
End Function

Function ExecuteFailureRecipe(r.SpecialRecipe, item.Items, x#, y#, z#)
	RemoveItem(item)

	If r\failOutputName <> "" Then
		If r\failTempName <> "" Then
			Local newItem.Items = CreateItem(r\failOutputName, r\failTempName, x, y, z)
		Else
			Local d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
			If d <> Null Then
				d\Size = 0.15
				ScaleSprite(d\obj, d\Size, d\Size)
			EndIf
		EndIf
	EndIf

	If r\failMessage <> "" Then
		Mirror914LastResult = r\failMessage
		Mirror914LastSuccess = False
	EndIf

	If r\requiredFlag = FLAG_HARRISON_PDA Or r\requiredFlag = FLAG_HARRISON_EYE Then
		ModifyKarma(-2)
	EndIf

	RefinedItems = RefinedItems + 1
End Function

Function ProcessConditionalVanilla%(item.Items, settingNum%, x#, y#, z#)
	If item\itemtemplate = Null Then Return False

	Local tempName$ = item\itemtemplate\tempname

	If tempName = "key4" And settingNum = REFINE_FINE Then
		If HasHarrisonPDA Or HarrisonPDAInIntake Then
			Return False
		Else
			RemoveItem(item)
			Local degradedCard.Items = CreateItem("Key Card", "key3", x, y, z)
			Mirror914LastResult = "914: Insufficient data for security elevation. Clearance reduced."
			Mirror914LastSuccess = False
			RefinedItems = RefinedItems + 1
			Return True
		EndIf
	EndIf

	If tempName = "key4" And settingNum = REFINE_VERY_FINE Then
		If (Not HasHarrisonEye) And (Not HarrisonEyeInIntake) Then
			RemoveItem(item)
			Local d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
			If d <> Null Then
				d\Size = 0.1
				ScaleSprite(d\obj, d\Size, d\Size)
			EndIf
			Mirror914LastResult = "914: Critical data missing. Catastrophic failure."
			Mirror914LastSuccess = False
			RefinedItems = RefinedItems + 1
			Return True
		EndIf
	EndIf

	If CurrentDay = 3 Then
		If tempName = "gasmask" And settingNum = REFINE_VERY_FINE Then
			RemoveItem(item)
			Local spectralMask.Items = CreateItem("Spectral Mask", "spectralmask", x, y, z)
			Mirror914LastResult = "914: Ectoplasmic filtration layer added."
			Mirror914LastSuccess = True
			ModifyKarma(3)
			RefinedItems = RefinedItems + 1
			Return True
		EndIf
	EndIf

	Return False
End Function

Function GetSettingNumber%(setting$)
	Select Lower(setting)
		Case "rough" : Return REFINE_ROUGH
		Case "coarse" : Return REFINE_COARSE
		Case "1:1" : Return REFINE_1TO1
		Case "fine" : Return REFINE_FINE
		Case "very fine" : Return REFINE_VERY_FINE
	End Select
	Return REFINE_1TO1
End Function

Function FindMatchingRecipe.SpecialRecipe(itemName$, tempName$, setting%)
	For i% = 0 To SpecialRecipeCount - 1
		Local r.SpecialRecipe = SpecialRecipes(i)
		If r <> Null Then
			If r\inputTempName <> "" Then
				If Lower(r\inputTempName) = Lower(tempName) And r\setting = setting Then
					Return r
				EndIf
			ElseIf r\inputItemName <> "" Then
				If Lower(r\inputItemName) = Lower(itemName) And r\setting = setting Then
					Return r
				EndIf
			EndIf
		EndIf
	Next
	Return Null
End Function

Function TrackIntakeItems(item.Items, inIntake%)
	If item\itemtemplate = Null Then Return
	Local tempName$ = item\itemtemplate\tempname

	Select Lower(tempName)
		Case "harrisonpda", "decodedpda"
			HarrisonPDAInIntake = inIntake
		Case "harrisoneye", "preservedeye", "enhancedeye"
			HarrisonEyeInIntake = inIntake
		Case "key4"
			Level4CardInIntake = inIntake
	End Select
End Function

Function ResetIntakeTracking()
	HarrisonPDAInIntake = False
	HarrisonEyeInIntake = False
	Level4CardInIntake = False
End Function

Function CheckIntakeCombo%(setting$)
	If Level4CardInIntake And HarrisonPDAInIntake Then
		If Lower(setting) = "fine" Or Lower(setting) = "very fine" Then
			SetStoryFlag(FLAG_HARRISON_PDA, 1)
			Return 1
		EndIf
	EndIf

	If Level4CardInIntake And HarrisonEyeInIntake Then
		If Lower(setting) = "very fine" Then
			SetStoryFlag(FLAG_HARRISON_EYE, 1)
			Return 2
		EndIf
	EndIf

	Return 0
End Function

Function Use914Mirror(item.Items, setting$, x#, y#, z#)
	CheckIntakeCombo(setting)
	Local handled% = ProcessMirror914(item, setting, x, y, z)
	Return handled
End Function

Function OnItemPickedUp(item.Items)
	If item\itemtemplate = Null Then Return
	Local tempName$ = item\itemtemplate\tempname

	Select Lower(tempName)
		Case "harrisonpda"
			If Not GetStoryFlag(FLAG_HARRISON_PDA) Then
				Mirror914LastResult = "Harrison's PDA acquired. Data encrypted."
			EndIf
		Case "harrisoneye"
			Mirror914LastResult = "Biometric sample acquired."
		Case "okey5", "okey6"
			If Not GetStoryFlag(FLAG_O5_CARD_OBTAINED) Then
				SetStoryFlag(FLAG_O5_CARD_OBTAINED, 1)
				ModifyKarma(10)
			EndIf
	End Select
End Function

Function Render914Status()
	If Mirror914LastResult = "" Then Return

	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	Local boxW% = 400
	Local boxH% = 60
	Local boxX% = (gw - boxW) / 2
	Local boxY% = gh - 150

	Color 20, 20, 30
	Rect boxX, boxY, boxW, boxH, True

	If Mirror914LastSuccess Then
		Color 50, 150, 50
	Else
		Color 150, 50, 50
	EndIf
	Rect boxX, boxY, boxW, boxH, False

	Color 200, 200, 200
	Local textW% = StringWidth(Mirror914LastResult)
	If textW > boxW - 20 Then
		DrawWrappedText(Mirror914LastResult, boxX + 10, boxY + 10, boxW - 20)
	Else
		Text boxX + (boxW - textW) / 2, boxY + 20, Mirror914LastResult
	EndIf
End Function

Function Clear914Status()
	Mirror914LastResult = ""
	Mirror914LastSuccess = False
End Function

Function Save914State(file%)
	WriteInt file, Mirror914Enabled
	WriteInt file, HarrisonPDAInIntake
	WriteInt file, HarrisonEyeInIntake
	WriteInt file, Level4CardInIntake
	WriteLine file, Mirror914LastResult
	WriteInt file, Mirror914LastSuccess
End Function

Function Load914State(file%)
	Mirror914Enabled = ReadInt(file)
	HarrisonPDAInIntake = ReadInt(file)
	HarrisonEyeInIntake = ReadInt(file)
	Level4CardInIntake = ReadInt(file)
	Mirror914LastResult = ReadLine(file)
	Mirror914LastSuccess = ReadInt(file)
End Function

Function Cleanup914System()
	For r.SpecialRecipe = Each SpecialRecipe
		Delete r
	Next

	For i% = 0 To MAX_SPECIAL_RECIPES - 1
		SpecialRecipes(i) = Null
	Next

	SpecialRecipeCount = 0
	ResetIntakeTracking()
	Clear914Status()
End Function

Function Debug914System()
	Color 255, 200, 0
	Text 400, 10, "=== 914 MIRROR ==="
	Text 400, 25, "Recipes: " + SpecialRecipeCount
	Text 400, 40, "PDA: " + GetStoryFlag(FLAG_HARRISON_PDA)
	Text 400, 55, "Eye: " + GetStoryFlag(FLAG_HARRISON_EYE)
	Text 400, 70, "O5: " + GetStoryFlag(FLAG_O5_CARD_OBTAINED)
	Text 400, 85, "PDA intake: " + HarrisonPDAInIntake
	Text 400, 100, "Eye intake: " + HarrisonEyeInIntake
	Text 400, 115, "L4 intake: " + Level4CardInIntake

	If Mirror914LastResult <> "" Then
		If Mirror914LastSuccess Then
			Color 100, 255, 100
		Else
			Color 255, 100, 100
		EndIf
		Text 400, 135, "Last: " + Left$(Mirror914LastResult, 50)
	EndIf
End Function
