/*
 * Source: Main.bb (lines 2081-2114), Items.bb (lines 490-591)
 * Type: Player Interaction System (EntityPick equivalent)
 */

using System;
using System.Collections.Generic;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.Physics;

namespace SCPContainmentBreach.Core.Systems;

public enum InteractableType
{
    None,
    Item,
    DoorButton,
    Screen,
    Lever
}

public struct InteractionTarget
{
    public InteractableType Type;
    public int EntityHandle;
    public object? Reference;
    public float Distance;
    public Vector3 HitPoint;
}

public static class InteractionSystem
{
    public const float ItemPickupRange = 1.2f;
    public const float DoorButtonRange = 0.7f;
    public const float DoorButtonCheckRange = 1.0f;
    public const float EntityPickRange = 0.6f;

    private static readonly Dictionary<int, InteractableData> RegisteredInteractables = new();
    private static int _nextInteractableId = 1;

    public static InteractionTarget CurrentTarget { get; private set; }
    public static bool HasTarget => CurrentTarget.Type != InteractableType.None;

    public struct InteractableData
    {
        public InteractableType Type;
        public object Reference;
        public Vector3 Position;
        public float Radius;
        public bool Visible;
    }

    public static int RegisterInteractable(InteractableType type, object reference, Vector3 position, float radius = 0.3f)
    {
        int id = _nextInteractableId++;
        RegisteredInteractables[id] = new InteractableData
        {
            Type = type,
            Reference = reference,
            Position = position,
            Radius = radius,
            Visible = true
        };
        return id;
    }

    public static void UpdateInteractablePosition(int id, Vector3 position)
    {
        if (RegisteredInteractables.TryGetValue(id, out var data))
        {
            data.Position = position;
            RegisteredInteractables[id] = data;
        }
    }

    public static void SetInteractableVisible(int id, bool visible)
    {
        if (RegisteredInteractables.TryGetValue(id, out var data))
        {
            data.Visible = visible;
            RegisteredInteractables[id] = data;
        }
    }

    public static void UnregisterInteractable(int id)
    {
        RegisteredInteractables.Remove(id);
    }

    public static void ClearInteractables()
    {
        RegisteredInteractables.Clear();
        _nextInteractableId = 1;
    }

    public static void Update(PlayerState player, Camera3D camera)
    {
        CurrentTarget = new InteractionTarget { Type = InteractableType.None };

        if (player.KillTimer < 0)
            return;

        Vector3 cameraPos = camera.Position;
        Vector3 cameraDir = Vector3.Normalize(camera.Target - camera.Position);

        InteractionTarget? closestItem = FindClosestItem(player, cameraPos, cameraDir);
        InteractionTarget? closestButton = FindClosestDoorButton(player, cameraPos, cameraDir);

        if (closestButton.HasValue && closestButton.Value.Distance < DoorButtonRange)
        {
            CurrentTarget = closestButton.Value;
        }
        else if (closestItem.HasValue && closestItem.Value.Distance < ItemPickupRange)
        {
            CurrentTarget = closestItem.Value;
        }
    }

    private static InteractionTarget? FindClosestItem(PlayerState player, Vector3 cameraPos, Vector3 cameraDir)
    {
        InteractionTarget? closest = null;
        float closestDist = float.MaxValue;

        foreach (var kvp in RegisteredInteractables)
        {
            var data = kvp.Value;
            if (data.Type != InteractableType.Item || !data.Visible)
                continue;

            Vector3 itemPos = data.Position;
            float dist = Vector3.Distance(cameraPos, itemPos);

            if (dist >= ItemPickupRange)
                continue;

            if (!IsInView(cameraPos, cameraDir, itemPos, 70.0f))
                continue;

            if (!IsVisible(cameraPos, itemPos))
                continue;

            if (dist < closestDist)
            {
                closestDist = dist;
                closest = new InteractionTarget
                {
                    Type = InteractableType.Item,
                    EntityHandle = kvp.Key,
                    Reference = data.Reference,
                    Distance = dist,
                    HitPoint = itemPos
                };
            }
        }

        return closest;
    }

    private static InteractionTarget? FindClosestDoorButton(PlayerState player, Vector3 cameraPos, Vector3 cameraDir)
    {
        InteractionTarget? closest = null;
        float closestDist = float.MaxValue;

        Vector3 playerPos = new Vector3(player.X, player.Y, -player.Z);

        foreach (var kvp in RegisteredInteractables)
        {
            var data = kvp.Value;
            if (data.Type != InteractableType.DoorButton || !data.Visible)
                continue;

            Vector3 buttonPos = data.Position;

            float dx = MathF.Abs(playerPos.X - buttonPos.X);
            float dz = MathF.Abs(playerPos.Z - buttonPos.Z);

            if (dx >= DoorButtonCheckRange || dz >= DoorButtonCheckRange)
                continue;

            float dist2D = MathF.Sqrt(dx * dx + dz * dz);
            if (dist2D >= DoorButtonRange)
                continue;

            Vector3 dirToButton = Vector3.Normalize(buttonPos - cameraPos);
            var pickResult = CollisionSystem.Raycast(cameraPos, dirToButton, EntityPickRange);

            bool canReach = !pickResult.Hit ||
                Vector3.Distance(cameraPos, buttonPos) < pickResult.Distance + 0.1f;

            if (!canReach)
                continue;

            if (dist2D < closestDist)
            {
                closestDist = dist2D;
                closest = new InteractionTarget
                {
                    Type = InteractableType.DoorButton,
                    EntityHandle = kvp.Key,
                    Reference = data.Reference,
                    Distance = dist2D,
                    HitPoint = buttonPos
                };
            }
        }

        return closest;
    }

    private static bool IsInView(Vector3 cameraPos, Vector3 cameraDir, Vector3 targetPos, float fovDegrees)
    {
        Vector3 toTarget = Vector3.Normalize(targetPos - cameraPos);
        float dot = Vector3.Dot(cameraDir, toTarget);
        float angle = MathF.Acos(Math.Clamp(dot, -1f, 1f)) * 180f / MathF.PI;
        return angle < fovDegrees * 0.5f;
    }

    private static bool IsVisible(Vector3 from, Vector3 to)
    {
        Vector3 dir = to - from;
        float dist = dir.Length();
        if (dist < 0.01f) return true;

        var result = CollisionSystem.Raycast(from, dir, dist - 0.05f);
        return !result.Hit;
    }

    public static bool TryInteract(PlayerState player)
    {
        if (!HasTarget)
            return false;

        switch (CurrentTarget.Type)
        {
            case InteractableType.Item:
                return TryPickupItem(player, CurrentTarget);

            case InteractableType.DoorButton:
                return TryUseDoorButton(player, CurrentTarget);

            default:
                return false;
        }
    }

    private static bool TryPickupItem(PlayerState player, InteractionTarget target)
    {
        if (target.Reference is not Items item)
            return false;

        return InventorySystem.PickItem(player, item);
    }

    private static bool TryUseDoorButton(PlayerState player, InteractionTarget target)
    {
        if (target.Reference is not Doors door)
            return false;

        if (door.openstate > 0 && door.openstate < 180)
            return false;

        if (!string.IsNullOrEmpty(door.Code))
        {
            OnDoorCodeRequired?.Invoke(door);
            return true;
        }

        UseDoor(door);
        return true;
    }

    public static void UseDoor(Doors door)
    {
        door.open = door.open == 0 ? 1 : 0;
        OnDoorUsed?.Invoke(door);
    }

    public static Action<Doors>? OnDoorUsed;
    public static Action<Doors>? OnDoorCodeRequired;

    public static void DrawDebug()
    {
        foreach (var kvp in RegisteredInteractables)
        {
            var data = kvp.Value;
            if (!data.Visible) continue;

            Color color = data.Type switch
            {
                InteractableType.Item => Color.Yellow,
                InteractableType.DoorButton => Color.Green,
                _ => Color.White
            };

            Raylib.DrawSphereWires(data.Position, data.Radius, 8, 8, color);
        }

        if (HasTarget)
        {
            Raylib.DrawSphere(CurrentTarget.HitPoint, 0.05f, Color.Red);
        }
    }
}
