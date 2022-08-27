; ----------------------------------------------------------------------
; -- Blitz_File_FileName.bb
; -- 
; -- Functions for finding out the names of files, directories
; -- and their extensions.
; -- 
; -- Author  : Phil Newton (http://www.sodaware.net/)
; -- Licence : Free to use & modify. Credit appreciated but not required.
; -- Version : 1.1
; ----------------------------------------------------------------------

; --------------------------------------------------
; -- API Functions
; --------------------------------------------------

; File_GetDirName
; File_GetFileName
; File_GetExtension

;;; <summary>Returns the directory name component of path.</summary>
;;; <param name="path">The path to use.</param>
;;; <remarks>A version of PHP's "dirname" function.</remarks>
;;; <returns>The directory name from a path.</returns>
;;; <example>File_GetDirName$("c:\my-dir\another-dir\file.txt") will return ""c:\my-dir\another-dir\"</example>
;;; <subsystem>Blitz.File</subsystem>
Function File_GetDirName$(path$)
	; Strip trailing slashes & return the part of the path that isn't the filename
	If Right(path, 1) = "/" Or Right(path, 1) = "\" Then path = Left(path, Len(path) - 1)
	Return Left(path, Len(path) - Len(File_GetFileName(path)))
End Function

;;; <summary>Returns the file name component of a path.</summary>
;;; <param name="path">The path to use.</param>
;;; <remarks>A version of PHP's "basename" function.</remarks>
;;; <returns>The file name from a path, or "" if not found</returns>
;;; <example>File_GetFileName$("c:\my-dir\another-dir\file.txt") will return "file.txt"</example>
;;; <subsystem>Blitz.File</subsystem>
Function File_GetFileName$(path$)
	
	If path = "" Then Return ""
	
	Local fileName$	= File_SplitAfterChar(File_ConvertSlashes(path$), "/")
	If fileName = "" Then fileName = path
	
	Return fileName
	
End Function

;;; <summary>Returns the extension part of a filename.</summary>
;;; <param name="fileName">The file name to get the extension of.</param>
;;; <remarks>Will return text after the final "." character.</remarks>
;;; <returns>The file extension found, or "" if not found.</returns>
;;; <example>File_GetFileName$("c:\my-dir\another-dir\file.txt") will return "txt"</example>
;;; <subsystem>Blitz.File</subsystem>
Function File_GetExtension$(fileName$)
	Return File_SplitAfterChar(File_ConvertSlashes(fileName$), ".")
End Function

; --------------------------------------------------
; -- Utility Functions
; --------------------------------------------------

;;; <summary>Convert backslashes in a filename to forward slashes.</summary>
;;; <param name="fileName">The filename to convert.</param>
;;; <returns>Filename with only forward slashes.</returns>
;;; <subsystem>Blitz.File</subsystem>
Function File_ConvertSlashes$(fileName$)	
	Return Replace(fileName, "\", "/")
End Function

;;; <summary>Gets the remainder of a string after the last instance of a character "char" has been reached.</summary>
;;; <param name="fileName">The file name to split.</param>
;;; <param name="char">The character to look for.</param>
;;; <returns>The remainder of the string after char, or "" if not found.</returns>
;;; <subsystem>Blitz.File</subsystem>
Function File_SplitAfterChar$(fileName$, char$)
	
	Local afterChar$	= ""
	
	; Start at the end of the name, and look for the char
	For stringPos = Len(fileName) To 1 Step -1
		If Mid(fileName, stringPos, 1) = char Then
			afterChar = Right(fileName, Len(fileName) - stringpos)
			Exit
		EndIf
	Next
	
	Return afterChar$
	
End Function