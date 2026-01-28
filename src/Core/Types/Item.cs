/*
 * Source: Items.bb
 * Lines: 3-5
 * Type: Const (Item limits)
 */

namespace SCPContainmentBreach.Core.Types;

public static class InventoryConstants
{
    public const int MaxItemAmount = 10;
}

/*
 * Source: Items.bb
 * Lines: 12-29
 * Type: ItemTemplates
 */

public class ItemTemplates
{
    public string name = "";
    public string tempname = "";

    public int sound;
    public int found;

    public int obj;
    public string objpath = "";
    public string parentobjpath = "";

    public int invimg;
    public int invimg2;
    public string invimgpath = "";

    public string imgpath = "";
    public int img;

    public int isAnim;

    public float scale;

    public int tex;
    public string texpath = "";
}

/*
 * Source: Items.bb
 * Lines: 335-359
 * Type: Items
 */

public class Items
{
    public string name = "";

    public int collider;
    public int model;

    public ItemTemplates? itemtemplate;

    public float DropSpeed;

    public int r, g, b;
    public float a;

    public int level;

    public int SoundChn;

    public float dist;
    public float disttimer;

    public float state;
    public float state2;

    public int Picked;
    public int Dropped;

    public int invimg;
    public int WontColl;

    public float xspeed;
    public float zspeed;

    public Items?[] SecondInv = new Items?[21];

    public int ID;
    public int invSlots;
}
