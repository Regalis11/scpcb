; ----------------------------------------------------------------------
; -- Blitz_Basic_Bank.bb
; -- 
; -- PeekString, PokeString and DumpBank (for debugging).
; -- 
; -- Version : 1.1
; -- Licence : Public Domain
; -- Homepage: http://www.sodaware.net/dev/blitz/
; ----------------------------------------------------------------------

;;; <summary>Pokes a NULL terminated string to a bank.</summary>
;;; <param name="bankHandle">Bank handle to poke to.</param>
;;; <param name="offset">Offset in bytes that the poke operation will be started at.</param>
;;; <param name="value">The string to poke.</param>
;;; <remarks>If the end of the bank is reached, string will still end with a NULL byte.</remarks>
;;; <subsystem>Blitz.Bank</subsystem>
Function PokeString(bankHandle, offset, value$)
	
	Local stringPos		= 1
	Local bankPos		= offset + stringPos - 1
	Local bankLength	= BankSize(bankHandle) - 1
	
	While (stringPos <= Len(value)) And (bankPos < bankLength)
		PokeByte bankHandle, bankPos, Asc(Mid(value$, stringPos, 1))
		
		; Move to next char
		bankPos 	= bankPos + 1
		stringPos 	= stringPos + 1
	Wend
	
	; Add the null byte to end of string
	PokeByte bankHandle, bankPos, 0
	
End Function

;;; <summary>Reads a string from a memory bank.</summary>
;;; <param name="bankHandle">Bank handle to peek from.</param>
;;; <param name="offset">Offset in bytes that the peek operation will be started at.</param>
;;; <returns>The Peeked string.</returns>
;;; <subsystem>Blitz.Bank</subsystem>
Function PeekString$(bankHandle, offset)
	
	Local bankPos		= offset
	Local bankLength	= BankSize(bankHandle)
	Local peekedString$	= ""
	Local endFound%		= False
	Local currentByte	= 0
	
	While (bankPos < bankLength) And (endFound = False)
		
		currentByte	= PeekByte(bankHandle, bankPos)
		
		If currentByte = 0 Then
			endFound = True
		Else
			peekedString = peekedString + Chr(currentByte)
		EndIf
		
		bankPos = bankPos + 1
	Wend
	
	Return peekedString
	
End Function

;;; <summary>Dumps the contents of a bank to the debuglog. Useful for examining the contents of a bank.</summary>
;;; <param name="bankHandle">Handle of the bank to dump.</param>
;;; <param name="rowSize">Optional value to control the length of a dumped row.</param>
;;; <subsystem>Blitz.Bank</subsystem>
Function DumpBank(bankHandle, rowSize = 16)
	
	Local dumpString$	= ""
	Local ascString$	= ""
	
	; Header
	DebugLog "Bank Size: " + BankSize(bankHandle)
	For i = 0 To BankSize(bankHandle) - 1
		
		; Dump is raw data (as hex), asc is the ascii data
		dumpString = dumpString + Right(Hex(PeekByte(bankHandle, i)), 2) + " "
		ascString	= ascString + Chr(PeekByte(bankHandle, i))
		
		; Dump the line if we're at the end of the row
		If i Mod rowSize = rowSize - 1 Then
			DebugLog dumpString + "   " + ascString
			dumpString = ""
			ascString = ""
		EndIf
	Next
	
	; Any extra data not dumped.
	DebugLog dumpString + "   " + ascString
	
End Function