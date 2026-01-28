namespace SCPContainmentBreach.Core.Types;

/// <summary>
/// Player state data - direct port from Main.bb Global variables.
/// Original: Main.bb lines 278-365, 1534, 2719
/// </summary>
public class PlayerState
{
    // Entity references (Main.bb:2719)
    public int Collider;  // Player collision pivot
    public int Head;      // Head pivot for camera

    // Death/Kill state (Main.bb:278)
    public float KillTimer;
    public int KillAnim;
    public float FallTimer;
    public float DeathTimer;

    // Mental state (Main.bb:279-280)
    public float Sanity;
    public float ForceMove;
    public float ForceAngle;
    public int RestoreSanity;

    // Playability (Main.bb:282)
    public int Playable = 1; // True

    // Blink system (Main.bb:284-285)
    public float BLINKFREQ;
    public float BlinkTimer;
    public float EyeIrritation;
    public float EyeStuck;
    public float BlinkEffect = 1.0f;
    public float BlinkEffectTimer;

    // Stamina system (Main.bb:287)
    public float Stamina;
    public float StaminaEffect = 1.0f;
    public float StaminaEffectTimer;

    // Effects (Main.bb:289)
    public float CameraShakeTimer;
    public int Vomit;
    public float VomitTimer;
    public int Regurgitate;

    // SCP-1025 disease states (Main.bb:291)
    public float[] SCP1025state = new float[7];

    // Heartbeat (Main.bb:293)
    public float HeartBeatRate;
    public float HeartBeatTimer;
    public float HeartBeatVolume;

    // Equipment worn (Main.bb:295-296)
    public int WearingGasMask;
    public int WearingHazmat;
    public int WearingVest;
    public int Wearing714;
    public int WearingNightVision;
    public float NVTimer;

    // Superman mode / cheat (Main.bb:298)
    public int SuperMan;
    public float SuperManTimer;

    // Health/Damage (Main.bb:300)
    public float Injuries;
    public float Bloodloss;
    public float Infect;
    public float HealTimer;

    // SCP-914 refined items count (Main.bb:302)
    public int RefinedItems;

    // Movement (Main.bb:307-309)
    public float DropSpeed;
    public float HeadDropSpeed;
    public float CurrSpeed;
    public float user_camera_pitch;
    public float side;
    public int Crouch;
    public float CrouchState;

    // Location (Main.bb:311)
    public int PlayerZone;
    public Rooms? PlayerRoom;

    // Interaction (Main.bb:313)
    public int GrabbedEntity;

    // Cheats (Main.bb:318)
    public int GodMode;
    public int NoClip;
    public float NoClipSpeed = 2.0f;

    // SCP-895 coffin distance (Main.bb:320)
    public float CoffinDistance = 100.0f;

    // Audio (Main.bb:322)
    public float PlayerSoundVolume;

    // Camera (Main.bb:1534)
    public int Camera;
    public float CameraShake;
    public float CurrCameraZoom;

    // Night vision (Main.bb:364-365)
    public int NVBlink;
    public int IsNVGBlinking;

    // Infinite stamina cheat (Main.bb:363)
    public int InfiniteStamina;
}

/// <summary>
/// Input state - key bindings and mouse state.
/// Original: Main.bb lines 257-269, 315-316
/// </summary>
public class InputState
{
    // Key bindings (Main.bb:257-267)
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

    // Mouse settings (Main.bb:269, 315)
    public float MouseSmooth = 1.0f;
    public int InvertMouse;

    // Mouse state (Main.bb:316)
    public int MouseHit1;
    public int MouseDown1;
    public int MouseHit2;
    public int DoubleClick;
    public int LastMouseHit1;
    public int MouseUp1;

    // Mouselook (Main.bb:250-255)
    public float mouselook_x_inc = 0.3f;
    public float mouselook_y_inc = 0.3f;
    public int mouse_left_limit = 250;
    public int mouse_right_limit;
    public int mouse_top_limit = 150;
    public int mouse_bottom_limit;
    public float mouse_x_speed_1;
    public float mouse_y_speed_1;
}

/// <summary>
/// UI/Menu state.
/// Original: Main.bb lines 334-343
/// </summary>
public class MenuState
{
    public int MainMenuOpen;
    public int MenuOpen;
    public float StopHidingTimer;
    public int InvOpen;
    public Items? OtherOpen;

    // Ending (Main.bb:337)
    public string SelectedEnding = "";
    public int EndingScreen;
    public float EndingTimer;

    // Messages (Main.bb:339)
    public float MsgTimer;
    public string Msg = "";
    public string DeathMSG = "";

    // Keypad (Main.bb:341)
    public int AccessCode;
    public string KeypadInput = "";
    public float KeypadTimer;
    public string KeypadMSG = "";

    // UI icons (Main.bb:343)
    public int DrawHandIcon;
    public int[] DrawArrowIcon = new int[5];
}

/// <summary>
/// Game timing state.
/// Original: Main.bb lines 189, 359
/// </summary>
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

/// <summary>
/// Graphics/Visual effects state.
/// Original: Main.bb lines 325-329, 1527-1529
/// </summary>
public class VisualState
{
    public float Shake;
    public float ExplosionTimer;
    public int ExplosionSFX;
    public int LightsOn = 1; // True
    public int SoundTransmission;

    // Blur (Main.bb:1527)
    public float BlurVolume;
    public float BlurTimer;

    // Light effects (Main.bb:1529)
    public float LightBlink;
    public float LightFlash;
}
