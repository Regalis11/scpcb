using System.Numerics;

namespace SCPContainmentBreach.Utils;

/// <summary>
/// Конвертер координат между Blitz3D и Raylib системами.
///
/// Blitz3D использует:
/// - Левостороннюю систему координат
/// - Y-вверх
/// - Единицы: условные "юниты" (в SCP:CB ~1 unit = 1 метр для персонажей)
///
/// Raylib использует:
/// - Правостороннюю систему координат
/// - Y-вверх
/// - OpenGL-стиль
///
/// Основное различие: Z-ось инвертирована.
/// </summary>
public class CoordinateConverter
{
    /// <summary>
    /// Масштаб конвертации. В оригинале 1 unit ≈ размер человека (RoomScale).
    /// Настраиваем под наши нужды.
    /// </summary>
    public float Scale { get; set; } = 1.0f;

    /// <summary>
    /// Конвертирует позицию из Blitz3D в Raylib
    /// </summary>
    public Vector3 FromBlitz3D(float x, float y, float z)
    {
        // Инвертируем Z для перехода из левосторонней в правостороннюю систему
        return new Vector3(x * Scale, y * Scale, -z * Scale);
    }

    /// <summary>
    /// Конвертирует позицию из Blitz3D в Raylib
    /// </summary>
    public Vector3 FromBlitz3D(Vector3 blitzPosition)
    {
        return FromBlitz3D(blitzPosition.X, blitzPosition.Y, blitzPosition.Z);
    }

    /// <summary>
    /// Конвертирует позицию из Raylib в Blitz3D
    /// </summary>
    public Vector3 ToBlitz3D(Vector3 raylibPosition)
    {
        return new Vector3(
            raylibPosition.X / Scale,
            raylibPosition.Y / Scale,
            -raylibPosition.Z / Scale
        );
    }

    /// <summary>
    /// Конвертирует углы вращения из Blitz3D в Raylib (Euler angles).
    /// Blitz3D: pitch, yaw, roll в градусах
    /// </summary>
    public Vector3 RotationFromBlitz3D(float pitch, float yaw, float roll)
    {
        // Blitz3D использует порядок pitch-yaw-roll
        // Raylib (через rlgl) также поддерживает Euler углы
        // Инвертируем yaw из-за смены системы координат
        return new Vector3(pitch, -yaw, roll);
    }

    /// <summary>
    /// Конвертирует Quaternion из Blitz3D
    /// </summary>
    public Quaternion QuaternionFromBlitz3D(float x, float y, float z, float w)
    {
        // При смене хиральности системы координат,
        // нужно инвертировать компоненты X и Y кватерниона
        return new Quaternion(-x, -y, z, w);
    }

    /// <summary>
    /// Конвертирует UV координаты (обычно без изменений)
    /// </summary>
    public Vector2 UVFromBlitz3D(float u, float v)
    {
        // Blitz3D может использовать перевёрнутые V координаты
        return new Vector2(u, 1.0f - v);
    }
}

/// <summary>
/// Константы масштаба из оригинального SCP:CB
/// </summary>
public static class BlitzUnits
{
    /// <summary>
    /// Масштаб комнат в оригинале (RoomScale в Main.bb)
    /// </summary>
    public const float RoomScale = 8.0f / 2048.0f;

    /// <summary>
    /// Высота игрока (примерно)
    /// </summary>
    public const float PlayerHeight = 1.8f;

    /// <summary>
    /// Радиус коллизии игрока
    /// </summary>
    public const float PlayerRadius = 0.3f;

    /// <summary>
    /// Скорость ходьбы игрока
    /// </summary>
    public const float WalkSpeed = 0.018f;

    /// <summary>
    /// Скорость бега игрока
    /// </summary>
    public const float SprintSpeed = 0.03f;
}
