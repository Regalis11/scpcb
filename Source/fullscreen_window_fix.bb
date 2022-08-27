;----------------------------------------------------------------------------------------------
;----------This code will hopefully fix some issues regarding the FullScreen problems----------
;----------------------------------------------------------------------------------------------


; Requires:-
; user32.decls - http://www.blitzbasic.com/codearcs/codearcs.php?code=1179

; Place this file into your Blitz3D 'userlibs' folder (eg. C:\Program Files (x86)\Blitz3D\userlibs).

;--

; Probably also a good idea to have these in your userlibs folder, just on general principles.
; kernel32.decls - http://www.blitzbasic.com/codearcs/codearcs.php?code=1180
; gdi32.decls - http://www.blitzbasic.com/codearcs/codearcs.php?code=1181

; The Win32 Constants are also useful for windows API stuff.
; Win32 Constants - http://www.blitzbasic.com/codearcs/codearcs.php?code=1607

;--

; Additional reference links:-
; http://www.gamedev.net/topic/418170-win32-borderless-window/
; http://stackoverflow.com/questions/15254078/win32-fullscreen-borderless-window-overlapping-taskbar



; -- Declare Windows API constants.
Const C_GWL_STYLE = -16
Const C_WS_POPUP = $80000000
Const C_HWND_TOP = 0
Const C_SWP_SHOWWINDOW = $0040

; -- Get the width and height of the desktop and place them into these globals.
Global G_desktop_screen_width
Global G_desktop_screen_height
GetDesktopSize()

Global G_viewport_x = 0
Global G_viewport_y = 0
Global G_viewport_width = G_desktop_screen_width
Global G_viewport_height = G_desktop_screen_height

; -- Get the OS handle of the app window.
Global G_app_handle = SystemProperty( "AppHWND" )

If Not Windowed3D() Then RuntimeError "FATAL ERROR: Your computer does not support the rendering of 3D graphics within a window."

;Graphics3D G_viewport_width, G_viewport_height, 0, 2

; -- Change the window style to 'WS_POPUP' and then set the window position to force the style to update.
;api_SetWindowLong( G_app_handle, C_GWL_STYLE, C_WS_POPUP )
;api_SetWindowPos( G_app_handle, C_HWND_TOP, G_viewport_x, G_viewport_y, G_viewport_width, G_viewport_height, C_SWP_SHOWWINDOW )

;SetBuffer BackBuffer()


;SyncGame



; == FUNCTIONS ==


Function GetDesktopSize()
	; Gets the width and height of the desktop on the main monitor and returns them in
	; the globals G_desktop_screen_width and G_desktop_screen_height.
	
	Local rectangle = CreateBank( 16 )
	api_GetClientRect( api_GetDesktopWindow(), rectangle )
	G_desktop_screen_width = PeekInt( rectangle, 8 ) - PeekInt( rectangle, 0 )
	G_desktop_screen_height = PeekInt( rectangle, 12 ) - PeekInt( rectangle, 4 )
	FreeBank rectangle
End Function


Function SyncGame()
	; NOTES:
	; This function should be run immediately before a game session begins and also after resuming from a pause.

	; *** At this point everything should be setup and ready to start/restart the game immediately. ***

	; Reset the input devices.
	MoveMouse G_viewport_center_x, G_viewport_center_y
	FlushMouse
	FlushKeys 
	MouseXSpeed()
	MouseYSpeed()
	MouseHit(1)
	MouseHit(2)
	MouseHit(3)

	; Set and render the backbuffer, and then flip it to the frontbuffer.
	SetBuffer BackBuffer()
	RenderWorld
	Flip

	; Synch the timing. This assumes that the 'G_old_time' global holds the Millisecs() time
	; set at the start of the previous loop and is used with render-tweening or delta-timing.
	G_old_time = MilliSecs()
	
End Function
;~IDEal Editor Parameters:
;~F#40#4C
;~C#Blitz3D