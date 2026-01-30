/*
 * Source: Engine Core
 * Type: BlitzEntity
 * Purpose: Blitz3D entity wrapper with coordinate system conversion
 *
 * Coordinate Systems:
 * - Blitz3D: Left-handed, Y-up, +Z forward
 * - Raylib:  Right-handed, Y-up, -Z forward
 * - Conversion: Negate Z axis
 */

using System;
using System.Numerics;
using Raylib_cs;

namespace SCPContainmentBreach.Core.Engine;

public class BlitzEntity
{
    public Model Model;
    public Texture2D Texture;

    // Position in world space (already converted to Raylib coords)
    public Vector3 Position;

    // Rotation stored as Euler angles in DEGREES (Blitz3D convention)
    // Pitch = X rotation, Yaw = Y rotation, Roll = Z rotation
    public float Pitch;
    public float Yaw;
    public float Roll;

    // Scale
    public Vector3 Scale = Vector3.One;

    // Visibility
    public bool Visible = true;

    // Parent entity for hierarchy
    public BlitzEntity? Parent;

    // Entity handle (Blitz3D compatibility)
    public int Handle;

    private static int _nextHandle = 1;

    public BlitzEntity()
    {
        Handle = _nextHandle++;
    }

    /// <summary>
    /// Degrees to radians
    /// </summary>
    private static float Deg2Rad(float degrees) => degrees * MathF.PI / 180f;

    /// <summary>
    /// Radians to degrees
    /// </summary>
    private static float Rad2Deg(float radians) => radians * 180f / MathF.PI;

    /// <summary>
    /// Build rotation matrix from Euler angles (Pitch/Yaw/Roll in degrees)
    /// Blitz3D order: Yaw (Y) -> Pitch (X) -> Roll (Z)
    /// </summary>
    public Matrix4x4 GetRotationMatrix()
    {
        float p = Deg2Rad(Pitch);
        float y = Deg2Rad(Yaw);
        float r = Deg2Rad(Roll);

        // Blitz3D rotation order: Y * X * Z
        Matrix4x4 rotY = Matrix4x4.CreateRotationY(y);
        Matrix4x4 rotX = Matrix4x4.CreateRotationX(p);
        Matrix4x4 rotZ = Matrix4x4.CreateRotationZ(r);

        return rotZ * rotX * rotY;
    }

    /// <summary>
    /// Get quaternion from current Euler angles
    /// </summary>
    public Quaternion GetRotationQuaternion()
    {
        float p = Deg2Rad(Pitch);
        float y = Deg2Rad(Yaw);
        float r = Deg2Rad(Roll);

        // YXZ order for Blitz3D compatibility
        Quaternion qY = Quaternion.CreateFromAxisAngle(Vector3.UnitY, y);
        Quaternion qX = Quaternion.CreateFromAxisAngle(Vector3.UnitX, p);
        Quaternion qZ = Quaternion.CreateFromAxisAngle(Vector3.UnitZ, r);

        return qY * qX * qZ;
    }

    /// <summary>
    /// Get forward vector (local Z axis after rotation)
    /// In Raylib coords: -Z is forward
    /// </summary>
    public Vector3 GetForwardVector()
    {
        Matrix4x4 rot = GetRotationMatrix();
        // Forward is -Z in right-handed
        return new Vector3(-rot.M31, -rot.M32, -rot.M33);
    }

    /// <summary>
    /// Get right vector (local X axis after rotation)
    /// </summary>
    public Vector3 GetRightVector()
    {
        Matrix4x4 rot = GetRotationMatrix();
        return new Vector3(rot.M11, rot.M12, rot.M13);
    }

    /// <summary>
    /// Get up vector (local Y axis after rotation)
    /// </summary>
    public Vector3 GetUpVector()
    {
        Matrix4x4 rot = GetRotationMatrix();
        return new Vector3(rot.M21, rot.M22, rot.M23);
    }

    /// <summary>
    /// Get full transformation matrix
    /// </summary>
    public Matrix4x4 GetTransformMatrix()
    {
        Matrix4x4 scale = Matrix4x4.CreateScale(Scale);
        Matrix4x4 rotation = GetRotationMatrix();
        Matrix4x4 translation = Matrix4x4.CreateTranslation(Position);

        Matrix4x4 transform = scale * rotation * translation;

        if (Parent != null)
        {
            transform = transform * Parent.GetTransformMatrix();
        }

        return transform;
    }

    // ========================================================================
    // BLITZ3D COMMAND IMPLEMENTATIONS
    // ========================================================================

    /// <summary>
    /// MoveEntity - Move in LOCAL space relative to entity's rotation
    /// Blitz3D: MoveEntity entity, x#, y#, z#
    ///
    /// In Blitz3D, +Z is forward. We convert to Raylib where -Z is forward.
    /// The input (x,y,z) is in Blitz3D convention.
    /// </summary>
    public static void MoveEntity(BlitzEntity e, float x, float y, float z)
    {
        // Convert Blitz3D Z to Raylib Z (negate)
        float raylibZ = -z;

        // Build rotation matrix from entity's current orientation
        Matrix4x4 rot = e.GetRotationMatrix();

        // Transform local movement vector by rotation
        // Local X = right, Local Y = up, Local Z = forward (in Blitz3D terms)
        Vector3 localMove = new Vector3(x, y, raylibZ);
        Vector3 worldMove = Vector3.Transform(localMove, rot);

        // Add to position
        e.Position += worldMove;
    }

    /// <summary>
    /// TranslateEntity - Move in GLOBAL or PARENT space
    /// Blitz3D: TranslateEntity entity, x#, y#, z#, [global]
    /// </summary>
    public static void TranslateEntity(BlitzEntity e, float x, float y, float z, bool global = false)
    {
        // Convert Blitz3D Z to Raylib Z
        float raylibZ = -z;

        if (global || e.Parent == null)
        {
            // Global movement
            e.Position.X += x;
            e.Position.Y += y;
            e.Position.Z += raylibZ;
        }
        else
        {
            // Parent space movement
            Matrix4x4 parentRot = e.Parent.GetRotationMatrix();
            Vector3 localMove = new Vector3(x, y, raylibZ);
            Vector3 worldMove = Vector3.Transform(localMove, parentRot);
            e.Position += worldMove;
        }
    }

    /// <summary>
    /// PositionEntity - Set GLOBAL position
    /// Blitz3D: PositionEntity entity, x#, y#, z#, [global]
    /// </summary>
    public static void PositionEntity(BlitzEntity e, float x, float y, float z, bool global = true)
    {
        // Convert Blitz3D Z to Raylib Z
        float raylibZ = -z;

        if (global || e.Parent == null)
        {
            e.Position = new Vector3(x, y, raylibZ);
        }
        else
        {
            // Position relative to parent
            Matrix4x4 parentTransform = e.Parent.GetTransformMatrix();
            if (Matrix4x4.Invert(parentTransform, out Matrix4x4 invParent))
            {
                Vector3 worldPos = new Vector3(x, y, raylibZ);
                e.Position = Vector3.Transform(worldPos, invParent);
            }
        }
    }

    /// <summary>
    /// TurnEntity - Rotate locally (add to current rotation)
    /// Blitz3D: TurnEntity entity, pitch#, yaw#, roll#, [global]
    /// </summary>
    public static void TurnEntity(BlitzEntity e, float pitch, float yaw, float roll, bool global = false)
    {
        if (global)
        {
            // Global rotation: rotate around world axes
            // This requires quaternion math to combine properly

            Quaternion currentRot = e.GetRotationQuaternion();

            // Create rotation quaternions for each axis (world space)
            Quaternion rotP = Quaternion.CreateFromAxisAngle(Vector3.UnitX, Deg2Rad(pitch));
            Quaternion rotY = Quaternion.CreateFromAxisAngle(Vector3.UnitY, Deg2Rad(yaw));
            Quaternion rotR = Quaternion.CreateFromAxisAngle(Vector3.UnitZ, Deg2Rad(roll));

            // Apply global rotations: world rotation * current
            Quaternion newRot = rotR * rotP * rotY * currentRot;

            // Convert back to Euler angles
            Vector3 euler = QuaternionToEuler(newRot);
            e.Pitch = euler.X;
            e.Yaw = euler.Y;
            e.Roll = euler.Z;
        }
        else
        {
            // Local rotation: simply add to Euler angles
            e.Pitch += pitch;
            e.Yaw += yaw;
            e.Roll += roll;

            // Normalize angles to -180..180 range
            e.Pitch = NormalizeAngle(e.Pitch);
            e.Yaw = NormalizeAngle(e.Yaw);
            e.Roll = NormalizeAngle(e.Roll);
        }
    }

    /// <summary>
    /// RotateEntity - Set GLOBAL rotation
    /// Blitz3D: RotateEntity entity, pitch#, yaw#, roll#, [global]
    /// </summary>
    public static void RotateEntity(BlitzEntity e, float pitch, float yaw, float roll, bool global = true)
    {
        if (global || e.Parent == null)
        {
            e.Pitch = pitch;
            e.Yaw = yaw;
            e.Roll = roll;
        }
        else
        {
            // Rotation relative to parent requires inverse parent rotation
            Quaternion parentRot = e.Parent.GetRotationQuaternion();
            Quaternion targetRot = Quaternion.CreateFromYawPitchRoll(
                Deg2Rad(yaw), Deg2Rad(pitch), Deg2Rad(roll));

            Quaternion localRot = Quaternion.Inverse(parentRot) * targetRot;
            Vector3 euler = QuaternionToEuler(localRot);

            e.Pitch = euler.X;
            e.Yaw = euler.Y;
            e.Roll = euler.Z;
        }
    }

    /// <summary>
    /// ScaleEntity - Set scale
    /// Blitz3D: ScaleEntity entity, x#, y#, z#, [global]
    /// </summary>
    public static void ScaleEntity(BlitzEntity e, float x, float y, float z, bool global = true)
    {
        e.Scale = new Vector3(x, y, z);
    }

    /// <summary>
    /// PointEntity - Point entity at target position
    /// Blitz3D: PointEntity entity, x#, y#, z#, [roll#]
    /// </summary>
    public static void PointEntity(BlitzEntity e, float x, float y, float z, float roll = 0)
    {
        // Convert target to Raylib coords
        float raylibZ = -z;
        Vector3 target = new Vector3(x, y, raylibZ);
        Vector3 direction = target - e.Position;

        if (direction.LengthSquared() < 0.0001f)
            return;

        direction = Vector3.Normalize(direction);

        // Calculate yaw (Y rotation) - horizontal angle
        e.Yaw = Rad2Deg(MathF.Atan2(direction.X, -direction.Z));

        // Calculate pitch (X rotation) - vertical angle
        float horizontalDist = MathF.Sqrt(direction.X * direction.X + direction.Z * direction.Z);
        e.Pitch = Rad2Deg(MathF.Atan2(direction.Y, horizontalDist));

        e.Roll = roll;
    }

    /// <summary>
    /// AlignToVector - Align entity axis to vector
    /// Blitz3D: AlignToVector entity, vx#, vy#, vz#, axis, [rate#]
    /// axis: 1=X, 2=Y, 3=Z
    /// </summary>
    public static void AlignToVector(BlitzEntity e, float vx, float vy, float vz, int axis, float rate = 1.0f)
    {
        // Convert vector to Raylib coords
        Vector3 targetVec = Vector3.Normalize(new Vector3(vx, vy, -vz));

        Vector3 currentAxis;
        switch (axis)
        {
            case 1: currentAxis = e.GetRightVector(); break;
            case 2: currentAxis = e.GetUpVector(); break;
            case 3: currentAxis = e.GetForwardVector(); break;
            default: return;
        }

        // Slerp between current and target
        if (rate < 1.0f)
        {
            currentAxis = Vector3.Lerp(currentAxis, targetVec, rate);
            currentAxis = Vector3.Normalize(currentAxis);
        }
        else
        {
            currentAxis = targetVec;
        }

        // Reconstruct rotation from aligned axis
        // This is complex - for now use PointEntity for Z axis
        if (axis == 3)
        {
            float yaw = Rad2Deg(MathF.Atan2(currentAxis.X, -currentAxis.Z));
            float pitch = Rad2Deg(MathF.Asin(currentAxis.Y));

            if (rate < 1.0f)
            {
                e.Yaw = LerpAngle(e.Yaw, yaw, rate);
                e.Pitch = LerpAngle(e.Pitch, pitch, rate);
            }
            else
            {
                e.Yaw = yaw;
                e.Pitch = pitch;
            }
        }
    }

    // ========================================================================
    // BLITZ3D GETTER COMMANDS
    // ========================================================================

    /// <summary>
    /// EntityX - Get X position
    /// Blitz3D: EntityX(entity, [global])
    /// </summary>
    public static float EntityX(BlitzEntity e, bool global = false)
    {
        if (global || e.Parent == null)
            return e.Position.X;

        Vector3 worldPos = Vector3.Transform(e.Position, e.Parent.GetTransformMatrix());
        return worldPos.X;
    }

    /// <summary>
    /// EntityY - Get Y position
    /// </summary>
    public static float EntityY(BlitzEntity e, bool global = false)
    {
        if (global || e.Parent == null)
            return e.Position.Y;

        Vector3 worldPos = Vector3.Transform(e.Position, e.Parent.GetTransformMatrix());
        return worldPos.Y;
    }

    /// <summary>
    /// EntityZ - Get Z position (returns Blitz3D Z, not Raylib Z)
    /// </summary>
    public static float EntityZ(BlitzEntity e, bool global = false)
    {
        float z;
        if (global || e.Parent == null)
            z = e.Position.Z;
        else
        {
            Vector3 worldPos = Vector3.Transform(e.Position, e.Parent.GetTransformMatrix());
            z = worldPos.Z;
        }

        // Convert back to Blitz3D coords
        return -z;
    }

    /// <summary>
    /// EntityPitch - Get pitch rotation
    /// </summary>
    public static float EntityPitch(BlitzEntity e, bool global = false)
    {
        if (global && e.Parent != null)
        {
            Quaternion parentRot = e.Parent.GetRotationQuaternion();
            Quaternion localRot = e.GetRotationQuaternion();
            Quaternion worldRot = parentRot * localRot;
            return QuaternionToEuler(worldRot).X;
        }
        return e.Pitch;
    }

    /// <summary>
    /// EntityYaw - Get yaw rotation
    /// </summary>
    public static float EntityYaw(BlitzEntity e, bool global = false)
    {
        if (global && e.Parent != null)
        {
            Quaternion parentRot = e.Parent.GetRotationQuaternion();
            Quaternion localRot = e.GetRotationQuaternion();
            Quaternion worldRot = parentRot * localRot;
            return QuaternionToEuler(worldRot).Y;
        }
        return e.Yaw;
    }

    /// <summary>
    /// EntityRoll - Get roll rotation
    /// </summary>
    public static float EntityRoll(BlitzEntity e, bool global = false)
    {
        if (global && e.Parent != null)
        {
            Quaternion parentRot = e.Parent.GetRotationQuaternion();
            Quaternion localRot = e.GetRotationQuaternion();
            Quaternion worldRot = parentRot * localRot;
            return QuaternionToEuler(worldRot).Z;
        }
        return e.Roll;
    }

    /// <summary>
    /// EntityDistance - Distance between two entities
    /// </summary>
    public static float EntityDistance(BlitzEntity e1, BlitzEntity e2)
    {
        return Vector3.Distance(e1.Position, e2.Position);
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /// <summary>
    /// Convert quaternion to Euler angles (degrees)
    /// Returns (Pitch, Yaw, Roll)
    /// </summary>
    private static Vector3 QuaternionToEuler(Quaternion q)
    {
        Vector3 euler;

        // Roll (Z)
        float sinr_cosp = 2 * (q.W * q.Z + q.X * q.Y);
        float cosr_cosp = 1 - 2 * (q.Y * q.Y + q.Z * q.Z);
        euler.Z = Rad2Deg(MathF.Atan2(sinr_cosp, cosr_cosp));

        // Pitch (X)
        float sinp = 2 * (q.W * q.X - q.Z * q.Y);
        if (MathF.Abs(sinp) >= 1)
            euler.X = Rad2Deg(MathF.CopySign(MathF.PI / 2, sinp));
        else
            euler.X = Rad2Deg(MathF.Asin(sinp));

        // Yaw (Y)
        float siny_cosp = 2 * (q.W * q.Y + q.Z * q.X);
        float cosy_cosp = 1 - 2 * (q.X * q.X + q.Y * q.Y);
        euler.Y = Rad2Deg(MathF.Atan2(siny_cosp, cosy_cosp));

        return euler;
    }

    /// <summary>
    /// Normalize angle to -180..180 range
    /// </summary>
    private static float NormalizeAngle(float angle)
    {
        while (angle > 180f) angle -= 360f;
        while (angle < -180f) angle += 360f;
        return angle;
    }

    /// <summary>
    /// Lerp between angles handling wrap-around
    /// </summary>
    private static float LerpAngle(float from, float to, float t)
    {
        float diff = NormalizeAngle(to - from);
        return from + diff * t;
    }

    private static float Deg2Rad(float d) => d * MathF.PI / 180f;
    private static float Rad2Deg(float r) => r * 180f / MathF.PI;

    // ========================================================================
    // RENDERING
    // ========================================================================

    /// <summary>
    /// Draw the entity model
    /// </summary>
    public void Draw()
    {
        if (!Visible)
            return;

        if (Model.MeshCount == 0)
            return;

        // Build transform for Raylib
        Matrix4x4 transform = GetTransformMatrix();

        // Raylib uses row-major, System.Numerics is row-major, compatible
        Raylib.DrawModel(Model, Position, 1.0f, Color.White);
    }

    /// <summary>
    /// Draw with full transform (rotation/scale)
    /// </summary>
    public void DrawEx()
    {
        if (!Visible)
            return;

        if (Model.MeshCount == 0)
            return;

        // Raylib DrawModelEx uses axis-angle rotation
        Vector3 rotationAxis = new Vector3(0, 1, 0);
        float rotationAngle = Yaw;

        Raylib.DrawModelEx(Model, Position, rotationAxis, rotationAngle, Scale, Color.White);
    }
}
