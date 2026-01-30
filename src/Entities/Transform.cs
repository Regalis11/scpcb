using System.Numerics;

namespace SCPContainmentBreach.Entities;

/// <summary>
/// Transform компонент - управляет позицией, вращением и масштабом Entity.
/// Поддерживает иерархию (локальные и мировые координаты).
/// </summary>
public class Transform
{
    private readonly Entity _entity;

    // Локальные значения (относительно родителя)
    private Vector3 _localPosition = Vector3.Zero;
    private Quaternion _localRotation = Quaternion.Identity;
    private Vector3 _localScale = Vector3.One;

    // Кэшированная матрица
    private Matrix4x4 _localMatrix = Matrix4x4.Identity;
    private Matrix4x4 _worldMatrix = Matrix4x4.Identity;
    private bool _isDirty = true;

    public Transform(Entity entity)
    {
        _entity = entity;
    }

    #region Local Properties

    public Vector3 LocalPosition
    {
        get => _localPosition;
        set
        {
            _localPosition = value;
            SetDirty();
        }
    }

    public Quaternion LocalRotation
    {
        get => _localRotation;
        set
        {
            _localRotation = Quaternion.Normalize(value);
            SetDirty();
        }
    }

    public Vector3 LocalScale
    {
        get => _localScale;
        set
        {
            _localScale = value;
            SetDirty();
        }
    }

    /// <summary>
    /// Euler углы в градусах (для удобства)
    /// </summary>
    public Vector3 LocalEulerAngles
    {
        get => QuaternionToEuler(_localRotation);
        set => LocalRotation = EulerToQuaternion(value);
    }

    #endregion

    #region World Properties

    public Vector3 WorldPosition
    {
        get
        {
            UpdateMatrices();
            return new Vector3(_worldMatrix.M41, _worldMatrix.M42, _worldMatrix.M43);
        }
        set
        {
            if (_entity.Parent != null)
            {
                // Конвертируем мировую позицию в локальную
                var parentWorld = _entity.Parent.Transform.WorldMatrix;
                Matrix4x4.Invert(parentWorld, out var parentInverse);
                var localPos = Vector3.Transform(value, parentInverse);
                LocalPosition = localPos;
            }
            else
            {
                LocalPosition = value;
            }
        }
    }

    public Quaternion WorldRotation
    {
        get
        {
            if (_entity.Parent != null)
            {
                return _entity.Parent.Transform.WorldRotation * _localRotation;
            }
            return _localRotation;
        }
        set
        {
            if (_entity.Parent != null)
            {
                var parentRotation = _entity.Parent.Transform.WorldRotation;
                LocalRotation = Quaternion.Inverse(parentRotation) * value;
            }
            else
            {
                LocalRotation = value;
            }
        }
    }

    public Vector3 WorldScale
    {
        get
        {
            if (_entity.Parent != null)
            {
                return _localScale * _entity.Parent.Transform.WorldScale;
            }
            return _localScale;
        }
    }

    #endregion

    #region Matrices

    public Matrix4x4 LocalMatrix
    {
        get
        {
            UpdateMatrices();
            return _localMatrix;
        }
    }

    public Matrix4x4 WorldMatrix
    {
        get
        {
            UpdateMatrices();
            return _worldMatrix;
        }
    }

    private void UpdateMatrices()
    {
        if (!_isDirty) return;

        // Локальная матрица: Scale * Rotation * Translation
        _localMatrix = Matrix4x4.CreateScale(_localScale) *
                       Matrix4x4.CreateFromQuaternion(_localRotation) *
                       Matrix4x4.CreateTranslation(_localPosition);

        // Мировая матрица
        if (_entity.Parent != null)
        {
            _worldMatrix = _localMatrix * _entity.Parent.Transform.WorldMatrix;
        }
        else
        {
            _worldMatrix = _localMatrix;
        }

        _isDirty = false;
    }

    private void SetDirty()
    {
        _isDirty = true;

        // Отмечаем детей как dirty
        foreach (var child in _entity.Children)
        {
            child.Transform.SetDirty();
        }
    }

    #endregion

    #region Direction Vectors

    /// <summary>
    /// Вектор "вперёд" (как EntityZ в Blitz3D)
    /// </summary>
    public Vector3 Forward => Vector3.Transform(-Vector3.UnitZ, WorldRotation);

    /// <summary>
    /// Вектор "вправо" (как EntityX в Blitz3D)
    /// </summary>
    public Vector3 Right => Vector3.Transform(Vector3.UnitX, WorldRotation);

    /// <summary>
    /// Вектор "вверх" (как EntityY в Blitz3D)
    /// </summary>
    public Vector3 Up => Vector3.Transform(Vector3.UnitY, WorldRotation);

    #endregion

    #region Movement Methods

    /// <summary>
    /// Перемещение в локальном пространстве
    /// </summary>
    public void Translate(Vector3 translation, bool worldSpace = false)
    {
        if (worldSpace)
        {
            WorldPosition += translation;
        }
        else
        {
            // Перемещение в локальном направлении
            LocalPosition += Vector3.Transform(translation, _localRotation);
        }
    }

    /// <summary>
    /// Вращение вокруг осей (TurnEntity в Blitz3D)
    /// </summary>
    public void Rotate(float pitchDegrees, float yawDegrees, float rollDegrees)
    {
        var rotation = Quaternion.CreateFromYawPitchRoll(
            yawDegrees * MathF.PI / 180f,
            pitchDegrees * MathF.PI / 180f,
            rollDegrees * MathF.PI / 180f
        );

        LocalRotation = _localRotation * rotation;
    }

    /// <summary>
    /// Направить на точку (PointEntity в Blitz3D)
    /// </summary>
    public void LookAt(Vector3 target, Vector3? up = null)
    {
        var direction = Vector3.Normalize(target - WorldPosition);
        var upVector = up ?? Vector3.UnitY;

        // Создаём матрицу вращения
        var lookMatrix = Matrix4x4.CreateLookAt(Vector3.Zero, direction, upVector);
        Matrix4x4.Invert(lookMatrix, out var inverted);

        WorldRotation = Quaternion.CreateFromRotationMatrix(inverted);
    }

    #endregion

    #region Utility

    /// <summary>
    /// Конвертация Quaternion в Euler углы (градусы)
    /// </summary>
    private static Vector3 QuaternionToEuler(Quaternion q)
    {
        // Извлекаем Euler углы из кватерниона
        float sinr_cosp = 2 * (q.W * q.X + q.Y * q.Z);
        float cosr_cosp = 1 - 2 * (q.X * q.X + q.Y * q.Y);
        float roll = MathF.Atan2(sinr_cosp, cosr_cosp);

        float sinp = 2 * (q.W * q.Y - q.Z * q.X);
        float pitch = MathF.Abs(sinp) >= 1
            ? MathF.CopySign(MathF.PI / 2, sinp)
            : MathF.Asin(sinp);

        float siny_cosp = 2 * (q.W * q.Z + q.X * q.Y);
        float cosy_cosp = 1 - 2 * (q.Y * q.Y + q.Z * q.Z);
        float yaw = MathF.Atan2(siny_cosp, cosy_cosp);

        // Конвертируем в градусы
        return new Vector3(
            pitch * 180f / MathF.PI,
            yaw * 180f / MathF.PI,
            roll * 180f / MathF.PI
        );
    }

    /// <summary>
    /// Конвертация Euler углов (градусы) в Quaternion
    /// </summary>
    private static Quaternion EulerToQuaternion(Vector3 euler)
    {
        return Quaternion.CreateFromYawPitchRoll(
            euler.Y * MathF.PI / 180f, // Yaw
            euler.X * MathF.PI / 180f, // Pitch
            euler.Z * MathF.PI / 180f  // Roll
        );
    }

    #endregion
}
