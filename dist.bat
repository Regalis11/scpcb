@echo off

if exist dist\ (
rmdir /s /q dist\
)

xcopy /s "Data\" "dist\Data\"
xcopy /s "GFX\" "dist\GFX\"
xcopy /s "Loadingscreens\" "dist\Loadingscreens\"
xcopy /s "Map Creator\" "dist\Map Creator\"
xcopy /s "SFX\" "dist\SFX\"
copy "*.txt" "dist\"
copy "userlibs\*.dll" "dist\"
copy "dplayx.dll" "dist\"
copy "options.ini" "dist\"
copy "*.exe" "dist\"

del "dist\MavlessB3DSetup.exe"
del "dist\cpuid.dll"
del "dist\Map Creator\*.bb"

pause
