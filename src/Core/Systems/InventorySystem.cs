/*
 * Source: Items.bb (lines 3-5, 595-739, 741-810)
 * Type: Inventory Management System
 */

using System;
using System.Numerics;
using SCPContainmentBreach.Core.Types;

namespace SCPContainmentBreach.Core.Systems;

public static class InventorySystem
{
    public const int MaxItemAmount = 10;

    private static readonly Items?[] Inventory = new Items?[MaxItemAmount + 1];
    private static int _itemAmount = 0;

    public static Items? SelectedItem { get; set; }
    public static int SelectedSlot { get; set; } = -1;

    public static Action<Items>? OnItemPicked;
    public static Action<Items>? OnItemDropped;
    public static Action<Items>? OnItemUsed;
    public static Action<string>? OnMessage;

    public static void Clear()
    {
        for (int i = 0; i < MaxItemAmount; i++)
        {
            Inventory[i] = null;
        }
        _itemAmount = 0;
        SelectedItem = null;
        SelectedSlot = -1;
    }

    public static Items? GetItem(int slot)
    {
        if (slot < 0 || slot >= MaxItemAmount)
            return null;
        return Inventory[slot];
    }

    public static int GetItemCount()
    {
        return _itemAmount;
    }

    public static bool IsFull()
    {
        for (int i = 0; i < MaxItemAmount; i++)
        {
            if (Inventory[i] == null)
                return false;
        }
        return true;
    }

    public static int FindEmptySlot()
    {
        for (int i = 0; i < MaxItemAmount; i++)
        {
            if (Inventory[i] == null)
                return i;
        }
        return -1;
    }

    public static int FindItemSlot(Items item)
    {
        for (int i = 0; i < MaxItemAmount; i++)
        {
            if (Inventory[i] == item)
                return i;
        }
        return -1;
    }

    public static bool PickItem(PlayerState player, Items item)
    {
        if (player.WearingHazmat > 0)
        {
            OnMessage?.Invoke("You cannot pick up any items while wearing a hazmat suit.");
            return false;
        }

        if (IsFull())
        {
            OnMessage?.Invoke("You cannot carry any more items.");
            return false;
        }

        string tempName = item.itemtemplate?.tempname ?? "";

        if (!CanPickItem(player, item, tempName))
            return false;

        int slot = FindEmptySlot();
        if (slot < 0)
            return false;

        HandleSpecialPickup(player, item, tempName);

        item.Picked = 1;
        item.Dropped = -1;

        if (item.itemtemplate != null)
            item.itemtemplate.found = 1;

        _itemAmount++;
        Inventory[slot] = item;

        InteractionSystem.SetInteractableVisible(item.ID, false);

        OnItemPicked?.Invoke(item);

        return true;
    }

    private static bool CanPickItem(PlayerState player, Items item, string tempName)
    {
        switch (tempName)
        {
            case "veryfinevest":
                OnMessage?.Invoke("The vest is too heavy to pick up.");
                return false;

            case "hazmatsuit":
            case "hazmatsuit2":
            case "hazmatsuit3":
                for (int i = 0; i < MaxItemAmount; i++)
                {
                    var inv = Inventory[i];
                    if (inv?.itemtemplate == null) continue;

                    string invTemp = inv.itemtemplate.tempname;
                    if (invTemp == "hazmatsuit" || invTemp == "hazmatsuit2" || invTemp == "hazmatsuit3")
                    {
                        OnMessage?.Invoke("You are not able to wear two hazmat suits at the same time.");
                        return false;
                    }
                    if (invTemp == "vest" || invTemp == "finevest")
                    {
                        OnMessage?.Invoke("You are not able to wear a vest and a hazmat suit at the same time.");
                        return false;
                    }
                }
                break;

            case "vest":
            case "finevest":
                for (int i = 0; i < MaxItemAmount; i++)
                {
                    var inv = Inventory[i];
                    if (inv?.itemtemplate == null) continue;

                    string invTemp = inv.itemtemplate.tempname;
                    if (invTemp == "vest" || invTemp == "finevest")
                    {
                        OnMessage?.Invoke("You are not able to wear two vests at the same time.");
                        return false;
                    }
                    if (invTemp == "hazmatsuit" || invTemp == "hazmatsuit2" || invTemp == "hazmatsuit3")
                    {
                        OnMessage?.Invoke("You are not able to wear a vest and a hazmat suit at the same time.");
                        return false;
                    }
                }
                break;
        }

        return true;
    }

    private static void HandleSpecialPickup(PlayerState player, Items item, string tempName)
    {
        switch (tempName)
        {
            case "firstaid":
            case "finefirstaid":
            case "veryfinefirstaid":
            case "firstaid2":
                item.state = 0;
                break;

            case "hazmatsuit":
            case "hazmatsuit2":
            case "hazmatsuit3":
            case "vest":
            case "finevest":
                SelectedItem = item;
                break;
        }
    }

    public static bool DropItem(PlayerState player, Items item, Vector3 dropPosition, float dropYaw)
    {
        if (player.WearingHazmat > 0)
        {
            OnMessage?.Invoke("You cannot drop any items while wearing a hazmat suit.");
            return false;
        }

        int slot = FindItemSlot(item);
        if (slot < 0)
            return false;

        item.Dropped = 1;
        item.Picked = 0;

        float yawRad = dropYaw * MathF.PI / 180.0f;
        float randomYaw = (dropYaw + RandomRange(-20, 20)) * MathF.PI / 180.0f;

        item.x = dropPosition.X - MathF.Sin(yawRad) * 0.1f;
        item.y = dropPosition.Y - 0.1f;
        item.z = dropPosition.Z - MathF.Cos(yawRad) * 0.1f;

        item.DropSpeed = 0;
        item.xspeed = 0;
        item.zspeed = 0;

        Inventory[slot] = null;
        _itemAmount--;

        HandleSpecialDrop(player, item);

        InteractionSystem.SetInteractableVisible(item.ID, true);
        InteractionSystem.UpdateInteractablePosition(item.ID,
            new Vector3(item.x, item.y, -item.z));

        OnItemDropped?.Invoke(item);

        return true;
    }

    private static void HandleSpecialDrop(PlayerState player, Items item)
    {
        string tempName = item.itemtemplate?.tempname ?? "";

        switch (tempName)
        {
            case "gasmask":
            case "supergasmask":
            case "gasmask3":
                player.WearingGasMask = 0;
                break;

            case "hazmatsuit":
            case "hazmatsuit2":
            case "hazmatsuit3":
                player.WearingHazmat = 0;
                break;

            case "vest":
            case "finevest":
                player.WearingVest = 0;
                break;

            case "nvgoggles":
            case "supernv":
                player.WearingNightVision = 0;
                break;

            case "scp714":
            case "coarse714":
                player.Wearing714 = 0;
                break;
        }

        if (SelectedItem == item)
            SelectedItem = null;
    }

    public static bool UseItem(PlayerState player, Items item)
    {
        if (item.itemtemplate == null)
            return false;

        string tempName = item.itemtemplate.tempname;

        switch (tempName)
        {
            case "gasmask":
            case "supergasmask":
            case "gasmask3":
                player.WearingGasMask = player.WearingGasMask == 0 ? 1 : 0;
                OnItemUsed?.Invoke(item);
                return true;

            case "nvgoggles":
            case "supernv":
                player.WearingNightVision = player.WearingNightVision == 0 ? 1 : 0;
                OnItemUsed?.Invoke(item);
                return true;

            case "scp714":
            case "coarse714":
                player.Wearing714 = player.Wearing714 == 0 ? 1 : 0;
                OnItemUsed?.Invoke(item);
                return true;

            case "firstaid":
            case "finefirstaid":
            case "veryfinefirstaid":
            case "firstaid2":
                if (player.Injuries > 0 || player.Bloodloss > 0)
                {
                    SelectedItem = item;
                    OnItemUsed?.Invoke(item);
                    return true;
                }
                OnMessage?.Invoke("You don't need to use this right now.");
                return false;

            case "eyedrops":
            case "fineeyedrops":
            case "supereyedrops":
                player.EyeStuck = tempName switch
                {
                    "eyedrops" => 9000f,
                    "fineeyedrops" => 22000f,
                    "supereyedrops" => 55000f,
                    _ => 9000f
                };
                RemoveItem(item);
                OnItemUsed?.Invoke(item);
                return true;

            case "scp500":
            case "scp500pill":
                player.Injuries = 0;
                player.Bloodloss = 0;
                player.Infect = 0;
                for (int i = 0; i < player.SCP1025state.Length; i++)
                    player.SCP1025state[i] = 0;
                RemoveItem(item);
                OnItemUsed?.Invoke(item);
                return true;

            default:
                SelectedItem = item;
                OnItemUsed?.Invoke(item);
                return true;
        }
    }

    public static void RemoveItem(Items item)
    {
        int slot = FindItemSlot(item);
        if (slot >= 0)
        {
            Inventory[slot] = null;
            _itemAmount--;
        }

        if (SelectedItem == item)
            SelectedItem = null;

        InteractionSystem.UnregisterInteractable(item.ID);
    }

    public static bool HasItemOfType(string tempName)
    {
        for (int i = 0; i < MaxItemAmount; i++)
        {
            if (Inventory[i]?.itemtemplate?.tempname == tempName)
                return true;
        }
        return false;
    }

    public static Items? FindItemOfType(string tempName)
    {
        for (int i = 0; i < MaxItemAmount; i++)
        {
            if (Inventory[i]?.itemtemplate?.tempname == tempName)
                return Inventory[i];
        }
        return null;
    }

    private static float RandomRange(float min, float max)
    {
        return min + (float)Random.Shared.NextDouble() * (max - min);
    }

    /// <summary>
    /// Main.bb lines 5371-5374: Move item between slots
    /// For z% = 0 To MaxItemAmount - 1 / If Inventory(z) = SelectedItem Then Inventory(z) = Null
    /// Inventory(n) = SelectedItem
    /// </summary>
    public static void MoveToSlot(Items item, int targetSlot)
    {
        if (targetSlot < 0 || targetSlot >= MaxItemAmount) return;
        if (Inventory[targetSlot] != null) return;

        int sourceSlot = FindItemSlot(item);
        if (sourceSlot < 0) return;

        Inventory[sourceSlot] = null;
        Inventory[targetSlot] = item;
    }

    /// <summary>
    /// Swap two inventory slots
    /// </summary>
    public static void SwapSlots(int slotA, int slotB)
    {
        if (slotA < 0 || slotA >= MaxItemAmount) return;
        if (slotB < 0 || slotB >= MaxItemAmount) return;

        (Inventory[slotA], Inventory[slotB]) = (Inventory[slotB], Inventory[slotA]);
    }
}
