/*
 * Source: Main.bb
 * Lines: 117-199, 2809-3011, 4200-4500
 * Type: Game Loop
 */

using System;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Engine;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.World;
using SCPContainmentBreach.Core.Systems;

namespace SCPContainmentBreach.Core;

public class Game : IDisposable
{
    public const int ScreenWidth = 1280;
    public const int ScreenHeight = 720;
    public const string Title = "SCP: Containment Breach";

    private bool _running;
    private bool _disposed;

    private Camera3D _camera;
    private MapSystem _map;
    private PlayerState _player;

    private float _userCameraPitch;
    private float _mouseXSpeed;
    private float _mouseYSpeed;

    private const float MouseSens = 1.0f;
    private const float MouseSmooth = 0.5f;
    private const float MousLookXInc = 0.3f;
    private const float MouselookYInc = 0.3f;
    private const float WalkSpeed = 0.018f;
    private const float RunMultiplier = 2.5f;

    public static Game Instance { get; private set; }
    public float FPSfactor { get; private set; }
    public float DeltaTime { get; private set; }

    public Game()
    {
        Instance = this;
    }

    public void Run()
    {
        Init();
        LoadContent();

        _running = true;
        float prevTime = (float)Raylib.GetTime();

        while (_running && !Raylib.WindowShouldClose())
        {
            float curTime = (float)Raylib.GetTime();
            DeltaTime = curTime - prevTime;
            prevTime = curTime;

            FPSfactor = Math.Clamp(DeltaTime * 70.0f, 0.2f, 5.0f);

            Update();
            Draw();
        }

        Shutdown();
    }

    private void Init()
    {
        Raylib.SetConfigFlags(ConfigFlags.ResizableWindow | ConfigFlags.Msaa4xHint);
        Raylib.InitWindow(ScreenWidth, ScreenHeight, Title);
        Raylib.SetTargetFPS(60);
        Raylib.InitAudioDevice();

        AssetManager.Initialize("");

        // Initialize Blitz3D adapter - wires all delegates
        BlitzAdapter.Init();

        _player = new PlayerState();
        _player.X = 0;
        _player.Y = 1.0f;
        _player.Z = 0;

        _camera = new Camera3D
        {
            Position = new Vector3(0, 1.0f, 0),
            Target = new Vector3(0, 1.0f, -1),
            Up = Vector3.UnitY,
            FovY = 70.0f,
            Projection = CameraProjection.Perspective
        };

        Raylib.DisableCursor();
        Console.WriteLine($"[Game] Init {ScreenWidth}x{ScreenHeight}");
    }

    private void LoadContent()
    {
        _map = new MapSystem("");
        _map.LoadRoomTemplates("Data/rooms.ini");

        var testRoom = _map.CreateRoom(1, RoomShapes.ROOM2C, 0, 0, 0, "lockroom");
        if (testRoom != null)
        {
            Console.WriteLine($"[Game] Room created: {testRoom.RoomTemplate?.Name}");
        }
    }

    private void Update()
    {
        if (Raylib.IsKeyPressed(KeyboardKey.Escape))
        {
            _running = false;
            return;
        }

        if (Raylib.IsKeyPressed(KeyboardKey.F11))
            Raylib.ToggleFullscreen();

        UpdateMouseLook();
        UpdateMovement();
        UpdateCamera();

        // Update Blitz adapter state
        BlitzAdapter.Update(DeltaTime, _player, _camera);
        BlitzAdapter.UpdateStreams();

        // Update FPSfactor for systems
        RoomEventSystem.FPSfactor = FPSfactor;
        RoomEventSystem.FPSfactor2 = FPSfactor;

        // Update game systems
        RoomEventSystem.UpdateEvents();
        NPCSystem.Update(_player, FPSfactor, _player.BlinkTimer, false);
    }

    private void UpdateMouseLook()
    {
        Vector2 mouseDelta = Raylib.GetMouseDelta();

        float rawX = mouseDelta.X * (MouseSens + 0.6f);
        float rawY = mouseDelta.Y * (MouseSens + 0.6f);

        _mouseXSpeed = CurveValue(rawX, _mouseXSpeed, (6.0f / (MouseSens + 1.0f)) * MouseSmooth);
        _mouseYSpeed = CurveValue(rawY, _mouseYSpeed, (6.0f / (MouseSens + 1.0f)) * MouseSmooth);

        float theYaw = _mouseXSpeed * MouslookXInc;
        float thePitch = _mouseYSpeed * MouselookYInc;

        _player.Yaw -= theYaw;
        _userCameraPitch += thePitch;

        _userCameraPitch = Math.Clamp(_userCameraPitch, -70.0f, 70.0f);
    }

    private void UpdateMovement()
    {
        bool moving = false;
        float angle = 0;

        if (Raylib.IsKeyDown(KeyboardKey.S))
        {
            moving = true;
            angle = 180;
            if (Raylib.IsKeyDown(KeyboardKey.A)) angle = 135;
            if (Raylib.IsKeyDown(KeyboardKey.D)) angle = -135;
        }
        else if (Raylib.IsKeyDown(KeyboardKey.W))
        {
            moving = true;
            angle = 0;
            if (Raylib.IsKeyDown(KeyboardKey.A)) angle = 45;
            if (Raylib.IsKeyDown(KeyboardKey.D)) angle = -45;
        }
        else
        {
            if (Raylib.IsKeyDown(KeyboardKey.A)) { angle = 90; moving = true; }
            if (Raylib.IsKeyDown(KeyboardKey.D)) { angle = -90; moving = true; }
        }

        if (moving)
        {
            float speed = WalkSpeed;
            if (Raylib.IsKeyDown(KeyboardKey.LeftShift))
                speed *= RunMultiplier;

            float moveAngle = (_player.Yaw + angle + 90.0f) * MathF.PI / 180.0f;

            _player.X += MathF.Cos(moveAngle) * speed * FPSfactor;
            _player.Z += MathF.Sin(moveAngle) * speed * FPSfactor;
        }

        if (Raylib.IsKeyDown(KeyboardKey.Space))
            _player.Y += WalkSpeed * FPSfactor;
        if (Raylib.IsKeyDown(KeyboardKey.LeftControl))
            _player.Y -= WalkSpeed * FPSfactor;
    }

    private void UpdateCamera()
    {
        _camera.Position = new Vector3(_player.X, _player.Y + 0.6f, -_player.Z);

        float yawRad = _player.Yaw * MathF.PI / 180.0f;
        float pitchRad = _userCameraPitch * MathF.PI / 180.0f;

        float dirX = MathF.Cos(yawRad) * MathF.Cos(pitchRad);
        float dirY = -MathF.Sin(pitchRad);
        float dirZ = MathF.Sin(yawRad) * MathF.Cos(pitchRad);

        _camera.Target = _camera.Position + new Vector3(dirX, dirY, -dirZ);
    }

    private void Draw()
    {
        Raylib.BeginDrawing();
        Raylib.ClearBackground(Color.Black);

        Raylib.BeginMode3D(_camera);
        DrawRooms();
        DrawDebugAxes();
        Raylib.EndMode3D();

        DrawHUD();
        Raylib.EndDrawing();
    }

    private void DrawRooms()
    {
        foreach (var room in _map.RoomsList)
        {
            var entity = _map.GetRoomEntity(room);
            if (entity == null || !entity.Visible)
                continue;

            if (AssetManager.IsModelValid(entity.Model))
            {
                Raylib.DrawModel(entity.Model, entity.Position, 1.0f, Color.White);
            }
            else
            {
                Vector3 pos = new Vector3(room.x, room.y, -room.z);
                Raylib.DrawCube(pos, 4.0f, 3.0f, 4.0f, Color.DarkGray);
                Raylib.DrawCubeWires(pos, 4.0f, 3.0f, 4.0f, Color.White);
            }
        }

        Raylib.DrawGrid(100, 1.0f);
    }

    private void DrawDebugAxes()
    {
        Raylib.DrawLine3D(Vector3.Zero, new Vector3(3, 0, 0), Color.Red);
        Raylib.DrawLine3D(Vector3.Zero, new Vector3(0, 3, 0), Color.Green);
        Raylib.DrawLine3D(Vector3.Zero, new Vector3(0, 0, 3), Color.Blue);
    }

    private void DrawHUD()
    {
        Raylib.DrawFPS(10, 10);

        Raylib.DrawText($"X:{_player.X:F2} Y:{_player.Y:F2} Z:{_player.Z:F2}", 10, 35, 16, Color.Lime);
        Raylib.DrawText($"Yaw:{_player.Yaw:F1} Pitch:{_userCameraPitch:F1}", 10, 55, 16, Color.Lime);
        Raylib.DrawText($"Rooms:{_map.RoomsList.Count} FPSfactor:{FPSfactor:F2}", 10, 75, 16, Color.Lime);
        Raylib.DrawText("WASD:Move Mouse:Look Space/Ctrl:Up/Down Shift:Run", 10, 95, 14, Color.Gray);
    }

    private static float CurveValue(float target, float current, float rate)
    {
        if (rate <= 0) return target;
        return current + (target - current) / rate;
    }

    private void Shutdown()
    {
        BlitzAdapter.Shutdown();
        AssetManager.UnloadAll();
        Raylib.CloseAudioDevice();
        Raylib.CloseWindow();
        Console.WriteLine("[Game] Shutdown");
    }

    public void Exit() => _running = false;

    public void Dispose()
    {
        if (_disposed) return;
        Shutdown();
        _disposed = true;
        GC.SuppressFinalize(this);
    }
}
