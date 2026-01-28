/*
 * Source: Main.bb
 * Lines: 278-365, 1534, 2719
 * Type: Global variables (Player)
 */

namespace SCPContainmentBreach.Core.Types;

public class PlayerState
{
    public float X;
    public float Y;
    public float Z;
    public float Yaw;
    public float Pitch;

    public int Collider;
    public int Head;

    public float KillTimer;
    public int KillAnim;
    public float FallTimer;
    public float DeathTimer;

    public float Sanity;
    public float ForceMove;
    public float ForceAngle;
    public int RestoreSanity;

    public int Playable = 1;

    public float BLINKFREQ;
    public float BlinkTimer;
    public float EyeIrritation;
    public float EyeStuck;
    public float BlinkEffect = 1.0f;
    public float BlinkEffectTimer;

    public float Stamina;
    public float StaminaEffect = 1.0f;
    public float StaminaEffectTimer;

    public float CameraShakeTimer;
    public int Vomit;
    public float VomitTimer;
    public int Regurgitate;

    public float[] SCP1025state = new float[7]; // Disease flags

    public float HeartBeatRate;
    public float HeartBeatTimer;
    public float HeartBeatVolume;

    public int WearingGasMask;
    public int WearingHazmat;
    public int WearingVest;
    public int Wearing714; // SCP-714 ring
    public int WearingNightVision;
    public float NVTimer;

    public int SuperMan; // Cheat flag
    public float SuperManTimer;

    public float Injuries;
    public float Bloodloss;
    public float Infect;
    public float HealTimer;

    public int RefinedItems; // SCP-914 counter

    public float DropSpeed;
    public float HeadDropSpeed;
    public float CurrSpeed;
    public float user_camera_pitch;
    public float side;
    public int Crouch;
    public float CrouchState;

    public int PlayerZone;
    public Rooms? PlayerRoom;

    public int GrabbedEntity;

    public int GodMode;
    public int NoClip;
    public float NoClipSpeed = 2.0f;

    public float CoffinDistance = 100.0f; // SCP-895

    public float PlayerSoundVolume;

    public int Camera;
    public float CameraShake;
    public float CurrCameraZoom;

    public int NVBlink;
    public int IsNVGBlinking;

    public int InfiniteStamina;
}

/*
 * Source: Main.bb
 * Lines: 250-269, 315-316
 * Type: Global variables (Input)
 */

public class InputState
{
    public int KEY_RIGHT;
    public int KEY_LEFT;
    public int KEY_UP;
    public int KEY_DOWN;
    public int KEY_BLINK;
    public int KEY_SPRINT;
    public int KEY_INV;
    public int KEY_CROUCH;
    public int KEY_SAVE;
    public int KEY_CONSOLE;

    public float MouseSmooth = 1.0f;
    public int InvertMouse;

    public int MouseHit1;
    public int MouseDown1;
    public int MouseHit2;
    public int DoubleClick;
    public int LastMouseHit1;
    public int MouseUp1;

    public float mouselook_x_inc = 0.3f;
    public float mouselook_y_inc = 0.3f;
    public int mouse_left_limit = 250;
    public int mouse_right_limit;
    public int mouse_top_limit = 150;
    public int mouse_bottom_limit;
    public float mouse_x_speed_1;
    public float mouse_y_speed_1;
}

/*
 * Source: Main.bb
 * Lines: 334-343
 * Type: Global variables (Menu/UI)
 */

public class MenuState
{
    public int MainMenuOpen;
    public int MenuOpen;
    public float StopHidingTimer;
    public int InvOpen;
    public Items? OtherOpen;

    public string SelectedEnding = "";
    public int EndingScreen;
    public float EndingTimer;

    public float MsgTimer;
    public string Msg = "";
    public string DeathMSG = "";

    public int AccessCode;
    public string KeypadInput = "";
    public float KeypadTimer;
    public string KeypadMSG = "";

    public int DrawHandIcon;
    public int[] DrawArrowIcon = new int[5];
}

/*
 * Source: Main.bb
 * Lines: 189, 359
 * Type: Global variables (Timing)
 */

public class TimeState
{
    public int CurTime;
    public int PrevTime;
    public int LoopDelay;
    public float FPSfactor;
    public float FPSfactor2;
    public float PrevFPSFactor;
    public int PlayTime;
}

/*
 * Source: Main.bb
 * Lines: 325-329, 1527-1529
 * Type: Global variables (Visual)
 */

public class VisualState
{
    public float Shake;
    public float ExplosionTimer;
    public int ExplosionSFX;
    public int LightsOn = 1;
    public int SoundTransmission;

    public float BlurVolume;
    public float BlurTimer;

    public float LightBlink;
    public float LightFlash;
}
