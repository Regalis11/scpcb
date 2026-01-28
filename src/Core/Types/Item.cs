namespace SCPContainmentBreach.Core.Types;

/// <summary>
/// Item template - shared data for items of same type.
/// Original: Items.bb lines 12-29
/// </summary>
public class ItemTemplates
{
    public string name = "";
    public string tempname = "";

    public int sound;
    public int found;

    // Model
    public int obj;
    public string objpath = "";
    public string parentobjpath = "";

    // Inventory images
    public int invimg;
    public int invimg2;
    public string invimgpath = "";

    // Document image
    public string imgpath = "";
    public int img;

    // Animation
    public int isAnim;

    // Scale
    public float scale;

    // Texture
    public int tex;
    public string texpath = "";
}

/// <summary>
/// Item instance - placed item in the world.
/// Original: Items.bb lines 335-359
/// </summary>
public class Items
{
    public string name = "";

    // Entities
    public int collider;
    public int model;

    // Template reference
    public ItemTemplates? itemtemplate;

    // Physics
    public float DropSpeed;

    // Color
    public int r, g, b;
    public float a;

    // Zone level
    public int level;

    // Sound
    public int SoundChn;

    // Distance
    public float dist;
    public float disttimer;

    // State
    public float state;
    public float state2;

    // Pickup state
    public int Picked;
    public int Dropped;

    // Inventory
    public int invimg;
    public int WontColl;

    // Physics velocity
    public float xspeed;
    public float zspeed;

    // Container (for items that hold other items)
    public Items?[] SecondInv = new Items?[21];

    // ID
    public int ID;
    public int invSlots;
}

/// <summary>
/// Inventory constants.
/// Original: Items.bb lines 3-5
/// </summary>
public static class InventoryConstants
{
    public const int MaxItemAmount = 10;
}
