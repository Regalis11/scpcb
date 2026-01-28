using Raylib_cs;
using System.Numerics;
using SCPContainmentBreach.Core;
using SCPContainmentBreach.Utils;

namespace SCPContainmentBreach.Scenes;

/// <summary>
/// Основная игровая сцена.
/// Содержит 3D мир, камеру от первого лица, и базовую инфраструктуру.
/// </summary>
public class GameWorldScene : IScene
{
    public string Name => "Game";
    public bool IsLoaded { get; private set; }

    // Камера
    private Camera3D _camera;
    private float _cameraPitch;
    private float _cameraYaw = -90f; // Начинаем смотря вперёд по -Z

    // Настройки камеры
    private const float MouseSensitivity = 0.1f;
    private const float MoveSpeed = 5.0f;
    private const float SprintMultiplier = 2.0f;

    // Координатный конвертер (Blitz3D -> Raylib)
    private readonly CoordinateConverter _coordConverter = new();

    // Debug
    private bool _showDebugGrid = true;
    private Vector3 _playerPosition;

    public void Load()
    {
        IsLoaded = true;

        // Инициализация камеры от первого лица
        _playerPosition = new Vector3(0, 1.7f, 5); // Высота глаз ~1.7м

        _camera = new Camera3D
        {
            Position = _playerPosition,
            Target = _playerPosition + Vector3.UnitZ * -1, // Смотрим вперёд
            Up = Vector3.UnitY,
            FovY = 75.0f,
            Projection = CameraProjection.Perspective
        };

        // Захват мыши для FPS управления
        Raylib.DisableCursor();

        Console.WriteLine("[GameWorldScene] Loaded");
        Console.WriteLine("[GameWorldScene] Controls: WASD - move, Mouse - look, Shift - sprint, Tab - debug grid");
    }

    public void Update(float deltaTime)
    {
        UpdateCamera(deltaTime);
        HandleInput(deltaTime);
    }

    private void UpdateCamera(float deltaTime)
    {
        // Получаем движение мыши
        Vector2 mouseDelta = Raylib.GetMouseDelta();

        // Обновляем углы камеры
        _cameraYaw += mouseDelta.X * MouseSensitivity;
        _cameraPitch -= mouseDelta.Y * MouseSensitivity;

        // Ограничиваем pitch чтобы не перевернуться
        _cameraPitch = Math.Clamp(_cameraPitch, -89f, 89f);

        // Вычисляем направление взгляда
        float pitchRad = _cameraPitch * MathF.PI / 180f;
        float yawRad = _cameraYaw * MathF.PI / 180f;

        Vector3 direction = new(
            MathF.Cos(pitchRad) * MathF.Cos(yawRad),
            MathF.Sin(pitchRad),
            MathF.Cos(pitchRad) * MathF.Sin(yawRad)
        );
        direction = Vector3.Normalize(direction);

        // Движение игрока
        float speed = MoveSpeed;
        if (Raylib.IsKeyDown(KeyboardKey.LeftShift))
        {
            speed *= SprintMultiplier;
        }

        Vector3 forward = new(direction.X, 0, direction.Z);
        forward = Vector3.Normalize(forward);
        Vector3 right = Vector3.Normalize(Vector3.Cross(forward, Vector3.UnitY));

        Vector3 movement = Vector3.Zero;

        if (Raylib.IsKeyDown(KeyboardKey.W)) movement += forward;
        if (Raylib.IsKeyDown(KeyboardKey.S)) movement -= forward;
        if (Raylib.IsKeyDown(KeyboardKey.D)) movement += right;
        if (Raylib.IsKeyDown(KeyboardKey.A)) movement -= right;

        if (movement != Vector3.Zero)
        {
            movement = Vector3.Normalize(movement) * speed * deltaTime;
            _playerPosition += movement;
        }

        // Обновляем камеру
        _camera.Position = _playerPosition;
        _camera.Target = _playerPosition + direction;
    }

    private void HandleInput(float deltaTime)
    {
        // Tab - toggle debug grid
        if (Raylib.IsKeyPressed(KeyboardKey.Tab))
        {
            _showDebugGrid = !_showDebugGrid;
        }

        // F - toggle flashlight (placeholder)
        if (Raylib.IsKeyPressed(KeyboardKey.F))
        {
            Console.WriteLine("[GameWorldScene] Flashlight toggle - not implemented");
        }

        // E - interact (placeholder)
        if (Raylib.IsKeyPressed(KeyboardKey.E))
        {
            Console.WriteLine("[GameWorldScene] Interact - not implemented");
        }
    }

    public void Draw()
    {
        // 3D отрисовка
        Raylib.BeginMode3D(_camera);

        // Debug сетка
        if (_showDebugGrid)
        {
            DrawDebugEnvironment();
        }

        // Placeholder для тестирования - несколько кубов
        DrawTestObjects();

        Raylib.EndMode3D();

        // 2D UI overlay
        DrawHUD();
    }

    private void DrawDebugEnvironment()
    {
        // Сетка на полу
        Raylib.DrawGrid(50, 1.0f);

        // Оси координат
        Raylib.DrawLine3D(Vector3.Zero, new Vector3(5, 0, 0), Color.Red);   // X
        Raylib.DrawLine3D(Vector3.Zero, new Vector3(0, 5, 0), Color.Green); // Y
        Raylib.DrawLine3D(Vector3.Zero, new Vector3(0, 0, 5), Color.Blue);  // Z

        // Подписи осей
        // (В 3D режиме текст сложнее, пока пропустим)
    }

    private void DrawTestObjects()
    {
        // Тестовые объекты для проверки рендеринга

        // Комната-коробка (стены)
        float roomSize = 10f;
        float wallHeight = 3f;

        // Пол
        Raylib.DrawPlane(
            new Vector3(0, 0, 0),
            new Vector2(roomSize * 2, roomSize * 2),
            new Color(50, 50, 55, 255)
        );

        // Несколько кубов как placeholder для объектов
        Raylib.DrawCube(new Vector3(-3, 0.5f, -3), 1, 1, 1, Color.DarkGray);
        Raylib.DrawCubeWires(new Vector3(-3, 0.5f, -3), 1, 1, 1, Color.Gray);

        Raylib.DrawCube(new Vector3(3, 0.75f, -5), 1.5f, 1.5f, 1.5f, Color.Maroon);
        Raylib.DrawCubeWires(new Vector3(3, 0.75f, -5), 1.5f, 1.5f, 1.5f, Color.Red);

        // SCP-173 placeholder (манекен)
        Vector3 scp173Pos = new(-5, 0, -8);
        Raylib.DrawCube(scp173Pos + new Vector3(0, 0.9f, 0), 0.6f, 1.8f, 0.4f, new Color(180, 160, 140, 255));
        Raylib.DrawSphere(scp173Pos + new Vector3(0, 1.9f, 0), 0.25f, new Color(180, 160, 140, 255));

        // Свет (placeholder сфера)
        Raylib.DrawSphere(new Vector3(0, 2.8f, 0), 0.2f, Color.Yellow);
    }

    private void DrawHUD()
    {
        int screenWidth = Raylib.GetScreenWidth();
        int screenHeight = Raylib.GetScreenHeight();

        // Перекрестие
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        Raylib.DrawLine(centerX - 10, centerY, centerX + 10, centerY, Color.White);
        Raylib.DrawLine(centerX, centerY - 10, centerX, centerY + 10, Color.White);

        // Позиция игрока (debug)
        if (_showDebugGrid)
        {
            string posText = $"Pos: {_playerPosition.X:F1}, {_playerPosition.Y:F1}, {_playerPosition.Z:F1}";
            Raylib.DrawText(posText, 10, 80, 18, Color.White);

            string lookText = $"Look: Yaw={_cameraYaw:F0} Pitch={_cameraPitch:F0}";
            Raylib.DrawText(lookText, 10, 100, 18, Color.White);

            Raylib.DrawText("[TAB] Toggle Grid | [ESC] Menu", 10, screenHeight - 25, 16, Color.Gray);
        }

        // Blink meter placeholder
        DrawBlinkMeter(screenWidth - 220, screenHeight - 40);
    }

    private void DrawBlinkMeter(int x, int y)
    {
        // Placeholder для индикатора моргания (ключевая механика SCP:CB)
        int width = 200;
        int height = 20;
        float blinkValue = 0.7f; // TODO: реальное значение

        Raylib.DrawRectangle(x, y, width, height, new Color(30, 30, 30, 200));
        Raylib.DrawRectangle(x + 2, y + 2, (int)((width - 4) * blinkValue), height - 4, Color.SkyBlue);
        Raylib.DrawRectangleLines(x, y, width, height, Color.White);
        Raylib.DrawText("BLINK", x + width / 2 - 25, y + 3, 14, Color.White);
    }

    public void Unload()
    {
        // Восстановить курсор
        Raylib.EnableCursor();

        IsLoaded = false;
        Console.WriteLine("[GameWorldScene] Unloaded");
    }
}
