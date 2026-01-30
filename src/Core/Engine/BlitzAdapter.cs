/*
 * Source: Engine Adapter
 * Type: BlitzAdapter - Wires Blitz3D-style delegates to Raylib implementations
 * Purpose: Bridge between BB-style game logic and C#/Raylib engine
 */

using System;
using System.Collections.Generic;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Systems;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.World;

namespace SCPContainmentBreach.Core.Engine;

/// <summary>
/// BlitzAdapter wires all the delegate-based callbacks used by NPCSystem and RoomEventSystem
/// to actual Raylib implementations. Call Init() once at game startup.
/// </summary>
public static class BlitzAdapter
{
    // Entity management
    private static readonly Dictionary<int, BlitzEntity> _entities = new();
    private static int _nextEntityHandle = 1;

    // Sound management
    private static readonly Dictionary<int, Sound> _sounds = new();
    private static readonly Dictionary<int, Music> _streams = new();
    private static int _nextSoundHandle = 1;

    // Channel management (sound instances)
    private static readonly Dictionary<int, Sound> _channels = new();
    private static int _nextChannelHandle = 1;

    // 3D Audio tracking (entity + range for each channel)
    private static readonly Dictionary<int, int> _channelEntities = new();
    private static readonly Dictionary<int, float> _channelRanges = new();

    // Camera reference
    public static Camera3D Camera;
    public static int CameraHandle;
    public static int ColliderHandle;
    public static int FogHandle;

    // Player reference
    public static PlayerState? Player;

    // Map reference
    public static MapSystem? Map;

    /// <summary>
    /// Initialize all delegate wiring. Call once at startup.
    /// </summary>
    public static void Init()
    {
        // Create special handles
        CameraHandle = CreatePivotInternal();
        ColliderHandle = CreatePivotInternal();
        FogHandle = CreatePivotInternal();

        WireRoomEventSystemDelegates();
        WireNPCSystemDelegates();

        Console.WriteLine("[BlitzAdapter] Initialized - all delegates wired");
    }

    /// <summary>
    /// Update adapter state each frame
    /// </summary>
    public static void Update(float deltaTime, PlayerState player, Camera3D camera)
    {
        Player = player;
        Camera = camera;

        // Update collider position to match player
        if (_entities.TryGetValue(ColliderHandle, out var collider))
        {
            collider.Position = new Vector3(player.X, player.Y, -player.Z);
        }

        // Update camera entity position
        if (_entities.TryGetValue(CameraHandle, out var camEntity))
        {
            camEntity.Position = camera.Position;
        }
    }

    // ========================================================================
    // ROOM EVENT SYSTEM DELEGATES
    // ========================================================================

    private static void WireRoomEventSystemDelegates()
    {
        // Entity position getters
        RoomEventSystem.EntityX = (handle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                return e.Position.X;
            return 0;
        };

        RoomEventSystem.EntityY = (handle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                return e.Position.Y;
            return 0;
        };

        RoomEventSystem.EntityZ = (handle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                return -e.Position.Z; // Convert back to Blitz3D coords
            return 0;
        };

        RoomEventSystem.EntityYaw = (handle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                return e.Yaw;
            return 0;
        };

        RoomEventSystem.EntityPitch = (handle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                return e.Pitch;
            return 0;
        };

        // Entity distance
        RoomEventSystem.EntityDistance = (h1, h2) =>
        {
            if (_entities.TryGetValue(h1, out var e1) && _entities.TryGetValue(h2, out var e2))
                return Vector3.Distance(e1.Position, e2.Position);
            return 9999f;
        };

        // Entity visibility
        RoomEventSystem.EntityVisible = (h1, h2) =>
        {
            // Simplified visibility check - raycast would be full implementation
            if (_entities.TryGetValue(h1, out var e1) && _entities.TryGetValue(h2, out var e2))
            {
                float dist = Vector3.Distance(e1.Position, e2.Position);
                return dist < 50f; // Basic range check
            }
            return false;
        };

        RoomEventSystem.EntityInView = (handle, camHandle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
            {
                // Check if entity is in camera frustum
                Vector3 screenPos = Raylib.GetWorldToScreen(e.Position, Camera);
                return screenPos.X >= 0 && screenPos.X <= Raylib.GetScreenWidth() &&
                       screenPos.Y >= 0 && screenPos.Y <= Raylib.GetScreenHeight() &&
                       screenPos.Z > 0;
            }
            return false;
        };

        // Entity manipulation
        RoomEventSystem.ShowEntity = (handle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                e.Visible = true;
        };

        RoomEventSystem.HideEntity = (handle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                e.Visible = false;
        };

        RoomEventSystem.EntityAlpha = (handle, alpha) =>
        {
            // Alpha storage - would need texture tinting in full impl
            if (_entities.TryGetValue(handle, out var e))
                e.Visible = alpha > 0.01f;
        };

        RoomEventSystem.PositionEntity = (handle, x, y, z, global, _) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                BlitzEntity.PositionEntity(e, x, y, z, global != 0);
        };

        RoomEventSystem.RotateEntity = (handle, p, y, r, global) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                BlitzEntity.RotateEntity(e, p, y, r, global != 0);
        };

        RoomEventSystem.MoveEntity = (handle, x, y, z, global) =>
        {
            if (_entities.TryGetValue(handle, out var e))
            {
                if (global != 0)
                    BlitzEntity.TranslateEntity(e, x, y, z, true);
                else
                    BlitzEntity.MoveEntity(e, x, y, z);
            }
        };

        RoomEventSystem.TurnEntity = (handle, p, y, r) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                BlitzEntity.TurnEntity(e, p, y, r, false);
        };

        RoomEventSystem.ScaleEntity = (handle, sx, sy, sz) =>
        {
            if (_entities.TryGetValue(handle, out var e))
                BlitzEntity.ScaleEntity(e, sx, sy, sz, true);
        };

        RoomEventSystem.PointEntity = (handle, targetHandle) =>
        {
            if (_entities.TryGetValue(handle, out var e) && _entities.TryGetValue(targetHandle, out var target))
            {
                Vector3 dir = target.Position - e.Position;
                if (dir.LengthSquared() > 0.0001f)
                {
                    dir = Vector3.Normalize(dir);
                    e.Yaw = MathF.Atan2(dir.X, -dir.Z) * 180f / MathF.PI;
                    e.Pitch = MathF.Asin(dir.Y) * 180f / MathF.PI;
                }
            }
        };

        RoomEventSystem.FreeEntity = (handle) =>
        {
            _entities.Remove(handle);
        };

        RoomEventSystem.ResetEntity = (handle) =>
        {
            if (_entities.TryGetValue(handle, out var e))
            {
                e.Position = Vector3.Zero;
                e.Pitch = 0;
                e.Yaw = 0;
                e.Roll = 0;
            }
        };

        // Animation
        RoomEventSystem.AnimTime = (handle) =>
        {
            // Animation time tracking - placeholder
            return 0f;
        };

        RoomEventSystem.SetAnimTime = (handle, time) =>
        {
            // Animation time setting - placeholder
        };

        // Sound system
        RoomEventSystem.LoadSoundStrict = (path) =>
        {
            string fullPath = AssetManager.NormalizePath(path);
            if (System.IO.File.Exists(fullPath))
            {
                Sound sound = Raylib.LoadSound(fullPath);
                int handle = _nextSoundHandle++;
                _sounds[handle] = sound;
                return handle;
            }
            Console.WriteLine($"[BlitzAdapter] Sound not found: {fullPath}");
            return 0;
        };

        RoomEventSystem.LoadTempSound = (path) =>
        {
            return RoomEventSystem.LoadSoundStrict!(path);
        };

        RoomEventSystem.PlaySoundStrict = () =>
        {
            // Play a random/generic sound - placeholder
            return 0;
        };

        // =====================================================================
        // 3D AUDIO SYSTEM - Simulated spatial audio using volume + pan
        // Distance attenuation: volume = 1.0 / (1 + dist * dropoff)
        // Pan calculation: based on angle between camera forward and sound direction
        // =====================================================================

        RoomEventSystem.PlaySound2 = (soundHandle, cameraHandle, entityHandle, range, volume) =>
        {
            if (_sounds.TryGetValue(soundHandle, out var sound))
            {
                var (vol, pan) = Calculate3DAudio(cameraHandle, entityHandle, range, volume);

                if (vol > 0.01f)
                {
                    Raylib.SetSoundVolume(sound, vol);
                    Raylib.SetSoundPan(sound, pan);
                    Raylib.PlaySound(sound);
                }

                int channelHandle = _nextChannelHandle++;
                _channels[channelHandle] = sound;
                _channelEntities[channelHandle] = entityHandle;
                _channelRanges[channelHandle] = range;
                return channelHandle;
            }
            return 0;
        };

        RoomEventSystem.LoopSound2 = (soundHandle, existingChannel, cameraHandle, entityHandle, range, volume) =>
        {
            if (existingChannel != 0 && _channels.TryGetValue(existingChannel, out var existingSound))
            {
                // Update existing looping sound with new 3D position
                var (vol, pan) = Calculate3DAudio(cameraHandle, entityHandle, range, volume);
                Raylib.SetSoundVolume(existingSound, vol);
                Raylib.SetSoundPan(existingSound, pan);

                // Restart if not playing (for looping)
                if (!Raylib.IsSoundPlaying(existingSound) && vol > 0.01f)
                {
                    Raylib.PlaySound(existingSound);
                }
                return existingChannel;
            }

            // Start new looping sound
            if (_sounds.TryGetValue(soundHandle, out var sound))
            {
                var (vol, pan) = Calculate3DAudio(cameraHandle, entityHandle, range, volume);

                if (vol > 0.01f)
                {
                    Raylib.SetSoundVolume(sound, vol);
                    Raylib.SetSoundPan(sound, pan);
                    Raylib.PlaySound(sound);
                }

                int channelHandle = _nextChannelHandle++;
                _channels[channelHandle] = sound;
                _channelEntities[channelHandle] = entityHandle;
                _channelRanges[channelHandle] = range;
                return channelHandle;
            }
            return 0;
        };

        RoomEventSystem.UpdateSoundOrigin = (channelHandle, cameraHandle, entityHandle, range) =>
        {
            // Update 3D sound position in real-time
            if (_channels.TryGetValue(channelHandle, out var sound))
            {
                var (vol, pan) = Calculate3DAudio(cameraHandle, entityHandle, range, 1.0f);
                Raylib.SetSoundVolume(sound, vol);
                Raylib.SetSoundPan(sound, pan);

                // Store updated tracking info
                _channelEntities[channelHandle] = entityHandle;
                _channelRanges[channelHandle] = range;
            }
        };

        RoomEventSystem.ChannelPlaying = (channelHandle) =>
        {
            if (_channels.TryGetValue(channelHandle, out var sound))
            {
                return Raylib.IsSoundPlaying(sound);
            }
            return false;
        };

        RoomEventSystem.StopChannel = (channelHandle) =>
        {
            if (_channels.TryGetValue(channelHandle, out var sound))
            {
                Raylib.StopSound(sound);
                _channels.Remove(channelHandle);
            }
        };

        RoomEventSystem.FreeSoundStrict = (soundHandle) =>
        {
            if (_sounds.TryGetValue(soundHandle, out var sound))
            {
                Raylib.UnloadSound(sound);
                _sounds.Remove(soundHandle);
            }
        };

        RoomEventSystem.StreamSoundStrict = (path, volume, loop) =>
        {
            string fullPath = AssetManager.NormalizePath(path);
            if (System.IO.File.Exists(fullPath))
            {
                Music music = Raylib.LoadMusicStream(fullPath);
                music.Looping = loop != 0;
                Raylib.SetMusicVolume(music, volume);
                Raylib.PlayMusicStream(music);

                int handle = _nextSoundHandle++;
                _streams[handle] = music;
                return handle;
            }
            return 0;
        };

        RoomEventSystem.StopStreamStrict = (handle) =>
        {
            if (_streams.TryGetValue(handle, out var music))
            {
                Raylib.StopMusicStream(music);
                Raylib.UnloadMusicStream(music);
                _streams.Remove(handle);
            }
        };

        // Camera controls
        RoomEventSystem.CameraFogRange = (camHandle, near, far) =>
        {
            // Fog range - would need shader implementation
        };

        RoomEventSystem.CameraFogMode = (camHandle, mode) =>
        {
            // Fog mode
        };

        RoomEventSystem.CameraRange = (camHandle, near, far) =>
        {
            // Camera clipping planes
        };

        RoomEventSystem.AmbientLight = (r, g, b) =>
        {
            // Ambient light setting
        };

        // Utility functions
        RoomEventSystem.Distance = (x1, y1, z1, x2, y2) =>
        {
            // 2D distance (x1,y1,z1 to x2,y2,0) - BB's Distance function
            float dx = x2 - x1;
            float dy = y2 - y1;
            return MathF.Sqrt(dx * dx + dy * dy);
        };

        RoomEventSystem.CurveValue = (target, current, rate) =>
        {
            if (rate <= 0) return target;
            return current + (target - current) / rate;
        };

        RoomEventSystem.CurveAngle = (target, current, rate) =>
        {
            if (rate <= 0) return target;
            float diff = target - current;
            while (diff > 180) diff -= 360;
            while (diff < -180) diff += 360;
            return current + diff / rate;
        };

        RoomEventSystem.WrapAngle = (angle) =>
        {
            while (angle >= 360) angle -= 360;
            while (angle < 0) angle += 360;
            return angle;
        };

        RoomEventSystem.DeltaYaw = (handle) =>
        {
            // Delta yaw from entity to player
            if (_entities.TryGetValue(handle, out var e) && Player != null)
            {
                float dx = Player.X - e.Position.X;
                float dz = -Player.Z - e.Position.Z;
                float targetYaw = MathF.Atan2(dx, dz) * 180f / MathF.PI;
                float diff = targetYaw - e.Yaw;
                while (diff > 180) diff -= 360;
                while (diff < -180) diff += 360;
                return diff;
            }
            return 0;
        };

        // Entity creation
        RoomEventSystem.CreatePivot = () =>
        {
            return CreatePivotInternal();
        };

        RoomEventSystem.LoadAnimMeshStrict = (path) =>
        {
            // Load animated mesh
            var entity = new BlitzEntity();
            string fullPath = AssetManager.NormalizePath(path);
            entity.Model = AssetManager.LoadMesh(fullPath);
            int handle = _nextEntityHandle++;
            _entities[handle] = entity;
            return handle;
        };

        // Picking
        RoomEventSystem.EntityPick = (handle, range) =>
        {
            // Raycast picking - placeholder
        };

        RoomEventSystem.PickedEntity = () => 0;
        RoomEventSystem.PickedX = () => 0;
        RoomEventSystem.PickedY = () => 0;
        RoomEventSystem.PickedZ = () => 0;
        RoomEventSystem.PickedNX = () => 0;
        RoomEventSystem.PickedNY = () => 1;
        RoomEventSystem.PickedNZ = () => 0;

        // Child entities
        RoomEventSystem.GetChild = (handle, index) =>
        {
            // Child entity access - placeholder
            return handle;
        };

        RoomEventSystem.OnEntityAlpha = RoomEventSystem.EntityAlpha;
        RoomEventSystem.EntityColor = (handle, r, g, b) =>
        {
            // Entity color tinting - placeholder
        };

        // Additional wiring for heavy containment rooms
        RoomEventSystem.UpdateButton = (buttonHandle) =>
        {
            // Update button interaction state
            if (_entities.TryGetValue(buttonHandle, out var button))
            {
                // Check if player is looking at and near button
                if (Player != null)
                {
                    Vector3 playerPos = new Vector3(Player.X, Player.Y, -Player.Z);
                    float dist = Vector3.Distance(playerPos, button.Position);
                    if (dist < 1.5f)
                    {
                        RoomEventSystem.ClosestButton = buttonHandle;
                    }
                }
            }
        };

        RoomEventSystem.GetChild = (handle) =>
        {
            // Get child entity - placeholder (returns same handle)
        };

        // Wire OnUpdateLever
        RoomEventSystem.OnUpdateLever = (leverHandle) =>
        {
            // Check lever state based on pitch
            if (_entities.TryGetValue(leverHandle, out var lever))
            {
                return lever.Pitch > 45.0f;
            }
            return false;
        };

        // Achievement system
        RoomEventSystem.GiveAchievement = (name) =>
        {
            Console.WriteLine($"[Achievement] {name}");
        };

        // Wire OnKill
        RoomEventSystem.OnKill = () =>
        {
            Console.WriteLine("[Kill] Player killed");
        };

        // OnCreateDecal
        RoomEventSystem.OnCreateDecal = (type, x, y, z, pitch, yaw, roll) =>
        {
            // Create decal at position - placeholder
            Console.WriteLine($"[Decal] Type {type} at ({x}, {y}, {z})");
        };

        // Time
        RoomEventSystem.MilliSecs2 = (scale) =>
        {
            return (int)(Raylib.GetTime() * 1000 * scale);
        };

        // Set global handles
        RoomEventSystem.Camera = CameraHandle;
        RoomEventSystem.Collider = ColliderHandle;
        RoomEventSystem.Fog = FogHandle;
    }

    // ========================================================================
    // NPC SYSTEM DELEGATES
    // ========================================================================

    private static void WireNPCSystemDelegates()
    {
        NPCSystem.OnDeath = (msg) =>
        {
            Console.WriteLine($"[Death] {msg}");
            // Trigger death screen
        };

        NPCSystem.OnKill = () =>
        {
            Console.WriteLine("[Kill] Player killed");
        };

        NPCSystem.OnPlaySound = (soundId) =>
        {
            // Play sound by ID - would need sound bank
            Console.WriteLine($"[Sound] Play ID {soundId}");
        };

        NPCSystem.OnPlaySound2 = (soundId, position, range) =>
        {
            Console.WriteLine($"[Sound3D] Play ID {soundId} at {position}");
        };

        NPCSystem.OnLoopSound = (soundId, position, range, volume) =>
        {
            // Loop 3D sound
        };

        NPCSystem.OnCreateDecal = (id, x, y, z, pitch, yaw, roll, sizeX, sizeY, alpha) =>
        {
            // Create decal (blood splatter, etc.)
        };

        NPCSystem.OnAchievement = (name) =>
        {
            Console.WriteLine($"[Achievement] {name}");
        };

        NPCSystem.OnMoveToPocketDimension = () =>
        {
            Console.WriteLine("[Event] Moved to Pocket Dimension");
        };

        NPCSystem.OnMessage = (msg) =>
        {
            RoomEventSystem.Msg = msg;
            RoomEventSystem.MsgTimer = 5.0f * 70f; // 5 seconds at 70 FPS
        };

        NPCSystem.OnCameraShake = (intensity) =>
        {
            RoomEventSystem.CameraShake = intensity;
        };

        NPCSystem.OnHeartbeat = (volume, rate) =>
        {
            // Heartbeat effect
        };

        NPCSystem.OnBlur = (intensity) =>
        {
            RoomEventSystem.BlurTimer = intensity;
        };
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /// <summary>
    /// Calculate 3D audio volume and pan based on listener/emitter positions.
    /// Returns (volume, pan) where pan is 0.0 (left) to 1.0 (right), 0.5 = center.
    ///
    /// Volume formula: volume = baseVolume * (1.0 / (1.0 + dist / range))
    /// This gives smooth attenuation: at distance=0, vol=1; at distance=range, vol=0.5
    ///
    /// Pan formula: Calculate angle from camera forward to sound source,
    /// then map to stereo: left = 0.0, center = 0.5, right = 1.0
    /// </summary>
    private static (float volume, float pan) Calculate3DAudio(int cameraHandle, int entityHandle, float range, float baseVolume)
    {
        if (!_entities.TryGetValue(entityHandle, out var emitter))
            return (0, 0.5f);

        // Get camera/listener position
        Vector3 listenerPos = Camera.Position;
        Vector3 emitterPos = emitter.Position;

        // Calculate distance
        float dist = Vector3.Distance(listenerPos, emitterPos);

        // Volume attenuation: smooth falloff curve
        // At dist=0: vol=1.0, at dist=range: vol=0.5, at dist=2*range: vol=0.33
        float dropoff = range > 0 ? range : 10.0f; // Prevent division by zero
        float volume = baseVolume / (1.0f + dist / dropoff);
        volume = Math.Clamp(volume, 0, 1);

        // If too quiet, skip pan calculation
        if (volume < 0.01f)
            return (0, 0.5f);

        // Pan calculation: based on angle from camera forward to sound
        // Camera forward in XZ plane
        float camYawRad = Camera.Target.X * MathF.PI / 180.0f; // Approximate
        Vector3 forward = new Vector3(
            MathF.Sin(Player?.Yaw ?? 0 * MathF.PI / 180.0f),
            0,
            -MathF.Cos(Player?.Yaw ?? 0 * MathF.PI / 180.0f)
        );
        Vector3 right = new Vector3(forward.Z, 0, -forward.X);

        // Direction to emitter (normalized XZ)
        Vector3 toEmitter = emitterPos - listenerPos;
        toEmitter.Y = 0; // Only horizontal pan
        if (toEmitter.LengthSquared() > 0.0001f)
        {
            toEmitter = Vector3.Normalize(toEmitter);

            // Dot product with right vector gives left/right positioning
            // -1 = full left, 0 = center, 1 = full right
            float rightDot = Vector3.Dot(toEmitter, right);

            // Map to pan: 0.0 (left) to 1.0 (right)
            float pan = 0.5f + rightDot * 0.5f;
            pan = Math.Clamp(pan, 0, 1);

            return (volume, pan);
        }

        return (volume, 0.5f); // Center if directly on listener
    }

    /// <summary>
    /// Update all active 3D sound channels (call each frame)
    /// </summary>
    public static void Update3DAudio()
    {
        foreach (var kvp in _channels)
        {
            int channelHandle = kvp.Key;
            Sound sound = kvp.Value;

            if (Raylib.IsSoundPlaying(sound))
            {
                if (_channelEntities.TryGetValue(channelHandle, out int entityHandle) &&
                    _channelRanges.TryGetValue(channelHandle, out float range))
                {
                    var (vol, pan) = Calculate3DAudio(CameraHandle, entityHandle, range, 1.0f);
                    Raylib.SetSoundVolume(sound, vol);
                    Raylib.SetSoundPan(sound, pan);
                }
            }
        }
    }

    private static int CreatePivotInternal()
    {
        var entity = new BlitzEntity();
        int handle = _nextEntityHandle++;
        _entities[handle] = entity;
        return handle;
    }

    public static BlitzEntity? GetEntity(int handle)
    {
        return _entities.TryGetValue(handle, out var e) ? e : null;
    }

    public static int CreateEntity()
    {
        return CreatePivotInternal();
    }

    public static int RegisterEntity(BlitzEntity entity)
    {
        int handle = _nextEntityHandle++;
        _entities[handle] = entity;
        return handle;
    }

    /// <summary>
    /// Update all streaming music
    /// </summary>
    public static void UpdateStreams()
    {
        foreach (var kvp in _streams)
        {
            Raylib.UpdateMusicStream(kvp.Value);
        }
    }

    /// <summary>
    /// Cleanup all resources
    /// </summary>
    public static void Shutdown()
    {
        foreach (var kvp in _sounds)
        {
            Raylib.UnloadSound(kvp.Value);
        }
        _sounds.Clear();

        foreach (var kvp in _streams)
        {
            Raylib.UnloadMusicStream(kvp.Value);
        }
        _streams.Clear();

        _entities.Clear();
        _channels.Clear();
        _channelEntities.Clear();
        _channelRanges.Clear();

        Console.WriteLine("[BlitzAdapter] Shutdown complete");
    }
}
