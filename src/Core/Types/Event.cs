namespace SCPContainmentBreach.Core.Types;

/// <summary>
/// Game event data structure.
/// Original: Main.bb lines 2435-2447
/// </summary>
public class Events
{
    public string EventName = "";

    // Room reference
    public Rooms? room;

    // State machine
    public float EventState;
    public float EventState2;
    public float EventState3;

    // Sound
    public int SoundCHN;
    public int SoundCHN2;
    public int Sound;
    public int Sound2;
    public int SoundCHN_isStream;
    public int SoundCHN2_isStream;

    // String data
    public string EventStr = "";

    // Image
    public int img;
}
