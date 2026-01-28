/*
 * Source: Main.bb
 * Lines: 4043-4500, 3030-3090
 * Type: Player Update System
 */

using System;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.Physics;

namespace SCPContainmentBreach.Core.Systems;

public static class PlayerSystem
{
    public const float BaseSpeed = 0.018f;
    public const float SprintMultiplier = 2.5f;
    public const float Gravity = 0.006f;
    public const float MaxFallSpeed = 2.0f;
    public const float ColliderRadius = 0.15f;
    public const float ColliderHeight = 0.30f;

    public const float MouseSensDefault = 1.0f;
    public const float MouseSmoothDefault = 0.5f;
    public const float MouseLookXInc = 0.3f;
    public const float MouseLookYInc = 0.3f;

    private static float _mouseXSpeed1;
    private static float _mouseYSpeed1;
    private static float _userCameraPitch;
    private static float _shake;
    private static float _prevShakeMod;

    private static float _blinkFreq = 560f;
    private static bool _prevBlinkKey;
    private static float _prevFPSfactor;

    public static float MouseSens = MouseSensDefault;
    public static float MouseSmooth = MouseSmoothDefault;
    public static bool InvertMouse = false;

    public static Action<int, bool>? OnFootstep;

    public static void Update(PlayerState player, float fpsFactor)
    {
        if (player.KillTimer < 0)
        {
            UpdateDead(player, fpsFactor);
            return;
        }

        UpdateCrouch(player, fpsFactor);
        UpdateMouseLook(player, fpsFactor);

        if (player.NoClip != 0)
        {
            UpdateNoClip(player, fpsFactor);
        }
        else
        {
            UpdateMovement(player, fpsFactor);
            UpdateGravity(player, fpsFactor);
        }

        UpdateBlink(player, fpsFactor);
        UpdateStamina(player, fpsFactor);

        _prevFPSfactor = fpsFactor;
    }

    private static void UpdateCrouch(PlayerState player, float fpsFactor)
    {
        if (Raylib.IsKeyPressed(KeyboardKey.C) && player.Playable != 0)
        {
            player.Crouch = player.Crouch == 0 ? 1 : 0;
        }

        if (MathF.Abs(player.CrouchState - player.Crouch) < 0.001f)
        {
            player.CrouchState = player.Crouch;
        }
        else
        {
            player.CrouchState = CurveValue(player.Crouch, player.CrouchState, 10.0f);
        }
    }

    private static void UpdateMouseLook(PlayerState player, float fpsFactor)
    {
        Vector2 mouseDelta = Raylib.GetMouseDelta();

        float rawX = mouseDelta.X * (MouseSens + 0.6f);
        float rawY = mouseDelta.Y * (MouseSens + 0.6f);

        float smoothRate = (6.0f / (MouseSens + 1.0f)) * MouseSmooth;

        _mouseXSpeed1 = CurveValue(rawX, _mouseXSpeed1, smoothRate);

        if (InvertMouse)
            _mouseYSpeed1 = CurveValue(-rawY, _mouseYSpeed1, smoothRate);
        else
            _mouseYSpeed1 = CurveValue(rawY, _mouseYSpeed1, smoothRate);

        if (_prevFPSfactor > 0)
        {
            if (MathF.Abs(fpsFactor / _prevFPSfactor - 1.0f) > 1.0f)
            {
                _mouseXSpeed1 = 0;
                _mouseYSpeed1 = 0;
            }
        }

        float vestPenalty = 1.0f + player.WearingVest;
        float theYaw = _mouseXSpeed1 * MouseLookXInc / vestPenalty;
        float thePitch = _mouseYSpeed1 * MouseLookYInc / vestPenalty;

        player.Yaw -= theYaw;
        _userCameraPitch += thePitch;

        if (_userCameraPitch > 70.0f) _userCameraPitch = 70.0f;
        if (_userCameraPitch < -70.0f) _userCameraPitch = -70.0f;

        player.Pitch = _userCameraPitch;
    }

    private static void UpdateMovement(PlayerState player, float fpsFactor)
    {
        float sprint = 1.0f;
        float speed = BaseSpeed;
        bool moving = false;
        float angle = 0;

        bool keyUp = Raylib.IsKeyDown(KeyboardKey.W);
        bool keyDown = Raylib.IsKeyDown(KeyboardKey.S);
        bool keyLeft = Raylib.IsKeyDown(KeyboardKey.A);
        bool keyRight = Raylib.IsKeyDown(KeyboardKey.D);
        bool keySprint = Raylib.IsKeyDown(KeyboardKey.LeftShift);

        if (keyDown || keyUp || keyLeft || keyRight)
        {
            if (player.Crouch == 0 && keySprint && player.Stamina > 0)
            {
                sprint = SprintMultiplier;
                player.Stamina -= fpsFactor * 0.4f * player.StaminaEffect;
                if (player.Stamina <= 0)
                    player.Stamina = -20.0f;
            }
        }

        if (keyDown)
        {
            moving = true;
            angle = 180;
            if (keyLeft) angle = 135;
            if (keyRight) angle = -135;
        }
        else if (keyUp)
        {
            moving = true;
            angle = 0;
            if (keyLeft) angle = 45;
            if (keyRight) angle = -45;
        }
        else
        {
            if (keyLeft) { angle = 90; moving = true; }
            if (keyRight) { angle = -90; moving = true; }
        }

        float temp2 = (speed * sprint) / (1.0f + player.CrouchState);
        temp2 /= MathF.Max((player.Injuries + 3.0f) / 3.0f, 1.0f);

        if (player.Injuries > 0.5f)
        {
            temp2 *= MathF.Min(MathF.Sin(_shake / 2.0f) + 1.2f, 1.0f);
        }

        if (moving)
        {
            float moveAngle = WrapAngle(player.Yaw + angle + 90.0f);
            float moveAngleRad = moveAngle * MathF.PI / 180.0f;

            float moveX = MathF.Cos(moveAngleRad) * temp2 * fpsFactor;
            float moveZ = MathF.Sin(moveAngleRad) * temp2 * fpsFactor;

            Vector3 currentPos = new Vector3(player.X, player.Y, player.Z);
            Vector3 moveVec = new Vector3(moveX, 0, moveZ);
            Vector3 newPos = CollisionSystem.SlideMove(currentPos, moveVec, ColliderRadius);

            player.X = newPos.X;
            player.Z = newPos.Z;

            UpdateFootsteps(player, fpsFactor, sprint);
        }
    }

    private static void UpdateFootsteps(PlayerState player, float fpsFactor, float sprint)
    {
        float prevShakeMod = _shake % 360.0f;
        _shake = (_shake + fpsFactor * MathF.Min(sprint, 1.5f) * 7.0f) % 720.0f;
        float currShakeMod = _shake % 360.0f;

        if (prevShakeMod < 180.0f && currShakeMod >= 180.0f && player.KillTimer >= 0)
        {
            int stepType = 0;
            bool isSprinting = sprint > 1.0f;

            player.PlayerSoundVolume = isSprinting
                ? MathF.Max(4.0f, player.PlayerSoundVolume)
                : MathF.Max(2.5f - player.Crouch * 0.6f, player.PlayerSoundVolume);

            OnFootstep?.Invoke(stepType, isSprinting);
        }
    }

    private static void UpdateNoClip(PlayerState player, float fpsFactor)
    {
        _shake = 0;
        player.CurrSpeed = 0;
        player.CrouchState = 0;
        player.Crouch = 0;

        float sprint = 1.0f;
        if (Raylib.IsKeyDown(KeyboardKey.LeftShift)) sprint = 2.5f;
        if (Raylib.IsKeyDown(KeyboardKey.LeftControl)) sprint = 0.5f;

        float temp2 = (BaseSpeed * sprint) * player.NoClipSpeed;

        float yawRad = player.Yaw * MathF.PI / 180.0f;
        float pitchRad = player.Pitch * MathF.PI / 180.0f;

        float forwardX = MathF.Cos(yawRad) * MathF.Cos(pitchRad);
        float forwardY = -MathF.Sin(pitchRad);
        float forwardZ = MathF.Sin(yawRad) * MathF.Cos(pitchRad);

        float rightX = MathF.Cos(yawRad + MathF.PI / 2.0f);
        float rightZ = MathF.Sin(yawRad + MathF.PI / 2.0f);

        if (Raylib.IsKeyDown(KeyboardKey.W))
        {
            player.X += forwardX * temp2 * fpsFactor;
            player.Y += forwardY * temp2 * fpsFactor;
            player.Z += forwardZ * temp2 * fpsFactor;
        }
        if (Raylib.IsKeyDown(KeyboardKey.S))
        {
            player.X -= forwardX * temp2 * fpsFactor;
            player.Y -= forwardY * temp2 * fpsFactor;
            player.Z -= forwardZ * temp2 * fpsFactor;
        }
        if (Raylib.IsKeyDown(KeyboardKey.A))
        {
            player.X -= rightX * temp2 * fpsFactor;
            player.Z -= rightZ * temp2 * fpsFactor;
        }
        if (Raylib.IsKeyDown(KeyboardKey.D))
        {
            player.X += rightX * temp2 * fpsFactor;
            player.Z += rightZ * temp2 * fpsFactor;
        }
    }

    private static void UpdateGravity(PlayerState player, float fpsFactor)
    {
        Vector3 pos = new Vector3(player.X, player.Y, player.Z);
        bool onGround = CollisionSystem.CheckGroundCollision(pos, ColliderHeight, out float groundY);

        if (onGround)
        {
            if (player.DropSpeed < -0.07f)
            {
                OnFootstep?.Invoke(0, false);
            }
            player.DropSpeed = 0;
            player.Y = groundY + 0.01f;
        }
        else
        {
            player.DropSpeed = MathF.Max(player.DropSpeed - Gravity * fpsFactor, -MaxFallSpeed);
            player.Y += player.DropSpeed * fpsFactor;
        }

        if (player.Y < 0.1f)
        {
            player.Y = 0.1f;
            player.DropSpeed = 0;
        }
    }

    private static void UpdateDead(PlayerState player, float fpsFactor)
    {
        bool onGround = false;
        for (int i = 0; i < 1; i++)
        {
            if (player.Y <= 0.1f) onGround = true;
        }

        if (onGround)
        {
            player.HeadDropSpeed = 0;
        }
        else
        {
            if (player.KillAnim == 0)
            {
                player.Y += player.HeadDropSpeed * fpsFactor;
                player.Pitch = CurveAngle(-90.0f, player.Pitch, 20.0f);
            }
            else
            {
                player.Y -= player.HeadDropSpeed * fpsFactor;
                player.Pitch = CurveAngle(90.0f, player.Pitch, 20.0f);
            }

            player.HeadDropSpeed -= 0.002f * fpsFactor;
        }
    }

    private static void UpdateBlink(PlayerState player, float fpsFactor)
    {
        bool blinkKey = Raylib.IsKeyDown(KeyboardKey.B);

        if (player.EyeStuck > 0)
        {
            player.BlinkTimer = _blinkFreq;
            player.EyeStuck = MathF.Max(player.EyeStuck - fpsFactor, 0);

            if (player.EyeStuck < 9000)
                player.BlurTimer = MathF.Max(player.BlurTimer, (9000 - player.EyeStuck) * 0.5f);
        }

        if (player.BlinkTimer < 0)
        {
            float darkA;

            if (player.BlinkTimer > -5)
            {
                darkA = MathF.Sin(MathF.Abs(player.BlinkTimer * 18.0f) * MathF.PI / 180.0f);
            }
            else if (player.BlinkTimer > -15)
            {
                darkA = 1.0f;
            }
            else
            {
                darkA = MathF.Abs(MathF.Sin(player.BlinkTimer * 18.0f * MathF.PI / 180.0f));
            }

            player.BlinkEffect = darkA;

            if (player.BlinkTimer <= -20)
            {
                _blinkFreq = RandomRange(455, 665);
                player.BlinkTimer = _blinkFreq;
            }

            player.BlinkTimer -= fpsFactor;
        }
        else
        {
            player.BlinkTimer -= fpsFactor * 0.6f * player.BlinkEffectTimer;

            if (player.EyeIrritation > 0)
            {
                float extra = MathF.Min(player.EyeIrritation / 100.0f + 1.0f, 4.0f);
                player.BlinkTimer -= extra * fpsFactor;
            }

            player.BlinkEffect = 0;
        }

        player.EyeIrritation = MathF.Max(0, player.EyeIrritation - fpsFactor);

        if (player.BlinkEffectTimer > 0)
        {
            player.BlinkEffectTimer -= fpsFactor / 70.0f;
        }

        if (blinkKey && !_prevBlinkKey && player.BlinkTimer > 0)
        {
            player.BlinkTimer = -1;
        }
        _prevBlinkKey = blinkKey;
    }

    private static void UpdateStamina(PlayerState player, float fpsFactor)
    {
        bool moving = Raylib.IsKeyDown(KeyboardKey.W) || Raylib.IsKeyDown(KeyboardKey.S) ||
                      Raylib.IsKeyDown(KeyboardKey.A) || Raylib.IsKeyDown(KeyboardKey.D);

        if (player.CurrSpeed > 0 || moving)
        {
            player.Stamina = MathF.Min(player.Stamina + 0.15f * fpsFactor / 1.25f, 100.0f);
        }
        else
        {
            player.Stamina = MathF.Min(player.Stamina + 0.15f * fpsFactor * 1.25f, 100.0f);
        }

        if (player.StaminaEffectTimer > 0)
        {
            player.StaminaEffectTimer -= fpsFactor / 70.0f;
        }
        else
        {
            if (player.StaminaEffect != 1.0f)
                player.StaminaEffect = 1.0f;
        }

        if (player.Wearing714 != 0)
        {
            player.Stamina = MathF.Min(player.Stamina, 10.0f);
            player.Sanity = MathF.Max(-850, player.Sanity);
        }

        if (player.InfiniteStamina != 0)
        {
            player.Stamina = MathF.Min(100, player.Stamina + (100.0f - player.Stamina) * 0.01f * fpsFactor);
        }
    }

    public static void UpdateCamera(ref Camera3D camera, PlayerState player, float fpsFactor)
    {
        float up = (MathF.Sin(_shake * MathF.PI / 180.0f) / (20.0f + player.CrouchState * 20.0f)) * 0.6f;
        float side = MathF.Cos(_shake / 2.0f * MathF.PI / 180.0f) / 35.0f;
        float roll = MathF.Max(MathF.Min(
            MathF.Sin(_shake / 2.0f * MathF.PI / 180.0f) * 2.5f * MathF.Min(player.Injuries + 0.25f, 3.0f),
            8.0f), -8.0f);

        float headHeight = 0.6f + player.CrouchState * -0.3f;

        float yawRad = player.Yaw * MathF.PI / 180.0f;
        float cameraX = player.X + MathF.Sin(yawRad) * side;
        float cameraY = player.Y + headHeight + up;
        float cameraZ = player.Z + MathF.Cos(yawRad) * side;

        camera.Position = new Vector3(cameraX, cameraY, -cameraZ);

        float pitchRad = player.Pitch * MathF.PI / 180.0f;
        float camShake = player.CameraShake;

        float finalYaw = player.Yaw + RandomRange(-camShake, camShake);
        float finalPitch = player.Pitch + RandomRange(-camShake, camShake);
        float finalYawRad = finalYaw * MathF.PI / 180.0f;
        float finalPitchRad = finalPitch * MathF.PI / 180.0f;

        float dirX = MathF.Cos(finalYawRad) * MathF.Cos(finalPitchRad);
        float dirY = -MathF.Sin(finalPitchRad);
        float dirZ = MathF.Sin(finalYawRad) * MathF.Cos(finalPitchRad);

        camera.Target = camera.Position + new Vector3(dirX, dirY, -dirZ);

        float rollRad = roll * 0.5f * MathF.PI / 180.0f;
        camera.Up = new Vector3(MathF.Sin(rollRad), MathF.Cos(rollRad), 0);
    }

    public static float GetBlinkAlpha(PlayerState player)
    {
        return player.BlinkEffect;
    }

    public static float GetStaminaPercent(PlayerState player)
    {
        return MathF.Max(player.Stamina, 0) / 100.0f;
    }

    public static float GetBlinkPercent(PlayerState player)
    {
        if (_blinkFreq <= 0) return 1.0f;
        return MathF.Max(player.BlinkTimer / _blinkFreq, 0);
    }

    private static float CurveValue(float target, float current, float rate)
    {
        if (rate <= 0) return target;
        return current + (target - current) / rate;
    }

    private static float CurveAngle(float target, float current, float rate)
    {
        float diff = WrapAngle(target - current);
        if (diff > 180) diff -= 360;
        return current + diff / rate;
    }

    private static float WrapAngle(float angle)
    {
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        return angle;
    }

    private static float RandomRange(float min, float max)
    {
        return min + (float)Random.Shared.NextDouble() * (max - min);
    }
}
