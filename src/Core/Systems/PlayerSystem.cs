/*
 * Source: Main.bb
 * Lines: 3030-3090, 4043-4300, 4400-4500
 * Type: Player Update System
 */

using System;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Types;

namespace SCPContainmentBreach.Core.Systems;

public static class PlayerSystem
{
    public const float BaseSpeed = 0.018f;
    public const float SprintMultiplier = 2.5f;
    public const float Gravity = 0.006f;
    public const float MaxFallSpeed = 2.0f;
    public const float PlayerHeight = 0.6f;

    public const float MouseSens = 1.0f;
    public const float MouseSmooth = 0.5f;
    public const float MouseLookXInc = 0.3f;
    public const float MouseLookYInc = 0.3f;

    private static float _mouseXSpeed;
    private static float _mouseYSpeed;
    private static float _userCameraPitch;
    private static float _shake;

    private static float _blinkFreq = 560f;
    private static bool _forceBlinkKeyDown;

    public static void Update(PlayerState player, float fpsFactor)
    {
        UpdateMouseLook(player, fpsFactor);
        UpdateMovement(player, fpsFactor);
        UpdateGravity(player, fpsFactor);
        UpdateBlink(player, fpsFactor);
        UpdateStamina(player, fpsFactor);
    }

    private static void UpdateMouseLook(PlayerState player, float fpsFactor)
    {
        Vector2 mouseDelta = Raylib.GetMouseDelta();

        float rawX = mouseDelta.X * (MouseSens + 0.6f);
        float rawY = mouseDelta.Y * (MouseSens + 0.6f);

        float smoothRate = (6.0f / (MouseSens + 1.0f)) * MouseSmooth;
        _mouseXSpeed = CurveValue(rawX, _mouseXSpeed, smoothRate);
        _mouseYSpeed = CurveValue(rawY, _mouseYSpeed, smoothRate);

        float theYaw = _mouseXSpeed * MouseLookXInc;
        float thePitch = _mouseYSpeed * MouseLookYInc;

        player.Yaw -= theYaw;
        _userCameraPitch += thePitch;
        _userCameraPitch = Math.Clamp(_userCameraPitch, -70.0f, 70.0f);

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
        bool keyCrouch = Raylib.IsKeyPressed(KeyboardKey.C);

        if (keyCrouch)
            player.Crouch = player.Crouch == 0 ? 1 : 0;

        if (player.CrouchState != player.Crouch)
        {
            if (MathF.Abs(player.CrouchState - player.Crouch) < 0.001f)
                player.CrouchState = player.Crouch;
            else
                player.CrouchState = CurveValue(player.Crouch, player.CrouchState, 10.0f);
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

        if (moving)
        {
            if (player.Crouch == 0 && keySprint && player.Stamina > 0)
            {
                sprint = SprintMultiplier;
                player.Stamina -= fpsFactor * 0.4f * player.StaminaEffect;
                if (player.Stamina <= 0)
                    player.Stamina = -20.0f;
            }

            float temp2 = (speed * sprint) / (1.0f + player.CrouchState);
            temp2 /= MathF.Max((player.Injuries + 3.0f) / 3.0f, 1.0f);

            float moveAngle = (player.Yaw + angle + 90.0f) * MathF.PI / 180.0f;

            player.X += MathF.Cos(moveAngle) * temp2 * fpsFactor;
            player.Z += MathF.Sin(moveAngle) * temp2 * fpsFactor;

            _shake = (_shake + fpsFactor * MathF.Min(sprint, 1.5f) * 7.0f) % 720.0f;
        }

        if (Raylib.IsKeyDown(KeyboardKey.Space) && player.DropSpeed == 0)
            player.Y += speed * fpsFactor;
        if (Raylib.IsKeyDown(KeyboardKey.LeftControl))
            player.Y -= speed * fpsFactor;
    }

    private static void UpdateGravity(PlayerState player, float fpsFactor)
    {
        bool onGround = CheckFloorCollision(player);

        if (onGround)
        {
            if (player.DropSpeed < -0.07f)
            {
                // Landing sound would play here
            }
            player.DropSpeed = 0;
        }
        else
        {
            player.DropSpeed = MathF.Max(player.DropSpeed - Gravity * fpsFactor, -MaxFallSpeed);
        }

        player.Y += player.DropSpeed * fpsFactor;

        if (player.Y < 0.1f)
        {
            player.Y = 0.1f;
            player.DropSpeed = 0;
        }
    }

    private static bool CheckFloorCollision(PlayerState player)
    {
        // Simple ground check - Y near zero
        // Real implementation would raycast against room geometry
        return player.Y <= 0.1f && player.DropSpeed <= 0;
    }

    private static void UpdateBlink(PlayerState player, float fpsFactor)
    {
        bool blinkKey = Raylib.IsKeyDown(KeyboardKey.Space);

        if (player.EyeStuck > 0)
        {
            player.BlinkTimer = _blinkFreq;
            player.EyeStuck = MathF.Max(player.EyeStuck - fpsFactor, 0);
        }

        if (player.BlinkTimer < 0)
        {
            float darkA = 0;

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

        // Manual blink with key
        if (blinkKey && !_forceBlinkKeyDown && player.BlinkTimer > 0)
        {
            player.BlinkTimer = -1;
        }
        _forceBlinkKeyDown = blinkKey;
    }

    private static void UpdateStamina(PlayerState player, float fpsFactor)
    {
        bool moving = Raylib.IsKeyDown(KeyboardKey.W) || Raylib.IsKeyDown(KeyboardKey.S) ||
                      Raylib.IsKeyDown(KeyboardKey.A) || Raylib.IsKeyDown(KeyboardKey.D);

        if (moving)
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
        }
    }

    public static void UpdateCamera(ref Camera3D camera, PlayerState player)
    {
        float headOffset = PlayerHeight - player.CrouchState * 0.3f;
        camera.Position = new Vector3(player.X, player.Y + headOffset, -player.Z);

        float yawRad = player.Yaw * MathF.PI / 180.0f;
        float pitchRad = player.Pitch * MathF.PI / 180.0f;

        float dirX = MathF.Cos(yawRad) * MathF.Cos(pitchRad);
        float dirY = -MathF.Sin(pitchRad);
        float dirZ = MathF.Sin(yawRad) * MathF.Cos(pitchRad);

        camera.Target = camera.Position + new Vector3(dirX, dirY, -dirZ);
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

    private static float RandomRange(float min, float max)
    {
        return min + (float)Random.Shared.NextDouble() * (max - min);
    }
}
