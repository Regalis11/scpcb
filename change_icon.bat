@echo off

echo Enter executable number
echo 1 - B3D - RoomMesh Conversion.exe
echo 2 - LightMapPNG.exe
echo 3 - MusicPlayer.exe
echo 4 - RMesh_Model_Viewer.exe
echo 5 - SCP - Containment Breach.exe

set /p exe=""

if "%exe%"=="1" (
	set exen="B3D - RoomMesh Conversion.exe"
) else if "%exe%"=="2" (
	set exen="LightMapPNG.exe"
) else if "%exe%"=="3" (
	set exen="MusicPlayer.exe"
) else if "%exe%"=="4" (
	set exen="RMesh_Model_Viewer.exe"
) else if "%exe%"=="5" (
	set exen="SCP - Containment Breach.exe"
) else (
	echo Invalid!
	pause
	exit
)

tools\ResourceHacker.exe -open %exen% -save %exen% -action addoverwrite -res "logo.ico" -mask ICONGROUP,MAINICON,

pause
