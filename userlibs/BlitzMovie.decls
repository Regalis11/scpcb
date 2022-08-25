;BlitzMovie 1.3 (C) Harriman Software 2004, 2005
.lib "BlitzMovie.dll"

BlitzMovie_Open%(strName$):"_BLITZMOVIE_Open@4"
BlitzMovie_Close():"_BLITZMOVIE_Close@0"
BlitzMovie_GetWidth%():"_BLITZMOVIE_GetWidth@0"
BlitzMovie_GetHeight%():"_BLITZMOVIE_GetHeight@0"
BlitzMovie_GetPixel%(offset%):"_BLITZMOVIE_GetPixel@4"
BlitzMovie_Play%():"_BLITZMOVIE_Play@0"
BlitzMovie_Stop%():"_BLITZMOVIE_Stop@0"

; Direct3D7 specific stuff (much faster than the software versions)
BlitzMovie_OpenD3D%(strName$, device%, ddraw%):"_BLITZMOVIE_OpenD3D@12"
BlitzMovie_DrawD3D%(x%, y%, width%, height%):"_BLITZMOVIE_DrawD3D@16"

; new stuff for 1.2
BlitzMovie_MuteAudio%():"_BLITZMOVIE_MuteAudio@0"
BlitzMovie_SetVolume%(volume%):"_BLITZMOVIE_SetVolume@4"
BlitzMovie_IsPlaying%():"_BLITZMOVIE_IsPlaying@0"

; new stuff for 1.3
BlitzMovie_OpenDecodeToImage%(strName$, buffer%, looping%):"_BLITZMOVIE_OpenDecodeToImage@12"
BlitzMovie_OpenDecodeToTexture%(strName$, buffer%, looping%):"_BLITZMOVIE_OpenDecodeToTexture@12"
BlitzMovie_Pause%():"_BLITZMOVIE_Pause@0"


