/*
 * Source: Blitz3D Collision System
 * Type: Sliding Collision Response (Type 2)
 *
 * Blitz3D uses ellipsoid-based collision with three response types:
 * 1 = Stop - entity stops at collision point
 * 2 = Slide - entity slides along surface (used for player)
 * 3 = Slide + Stop - slides but stops at second collision
 */

using System;
using System.Collections.Generic;
using System.Numerics;
using Raylib_cs;

namespace SCPContainmentBreach.Core.Physics;

public static class CollisionSystem
{
    private static readonly List<BoundingBox> StaticColliders = new();
    private static readonly List<CollisionMesh> MeshColliders = new();

    public const float SkinWidth = 0.001f;
    public const float MaxSlideIterations = 3;
    public const float GroundCheckDistance = 0.35f;
    public const float StepHeight = 0.2f;

    public struct CollisionMesh
    {
        public Model Model;
        public Matrix4x4 Transform;
        public bool Enabled;
    }

    public struct CollisionResult
    {
        public bool Hit;
        public Vector3 Point;
        public Vector3 Normal;
        public float Distance;
    }

    public static void ClearColliders()
    {
        StaticColliders.Clear();
        MeshColliders.Clear();
    }

    public static void AddBoxCollider(BoundingBox box)
    {
        StaticColliders.Add(box);
    }

    public static void AddMeshCollider(Model model, Matrix4x4 transform)
    {
        MeshColliders.Add(new CollisionMesh
        {
            Model = model,
            Transform = transform,
            Enabled = true
        });
    }

    public static Vector3 SlideMove(Vector3 position, Vector3 velocity, float radius)
    {
        if (velocity.LengthSquared() < 0.00001f)
            return position;

        Vector3 finalPos = position;
        Vector3 remainingVel = velocity;

        for (int iteration = 0; iteration < MaxSlideIterations; iteration++)
        {
            if (remainingVel.LengthSquared() < 0.00001f)
                break;

            CollisionResult result = CastSphere(finalPos, remainingVel, radius);

            if (!result.Hit)
            {
                finalPos += remainingVel;
                break;
            }

            float moveDistance = MathF.Max(result.Distance - SkinWidth, 0);
            Vector3 moveDir = Vector3.Normalize(remainingVel);
            finalPos += moveDir * moveDistance;

            float remainingDistance = remainingVel.Length() - moveDistance;
            if (remainingDistance <= SkinWidth)
                break;

            Vector3 slideVel = CalculateSlideVector(remainingVel, result.Normal, remainingDistance);
            remainingVel = slideVel;
        }

        return finalPos;
    }

    private static Vector3 CalculateSlideVector(Vector3 velocity, Vector3 normal, float remainingDistance)
    {
        float dot = Vector3.Dot(velocity, normal);

        if (dot >= 0)
            return velocity;

        Vector3 normalComponent = normal * dot;
        Vector3 tangentComponent = velocity - normalComponent;

        if (tangentComponent.LengthSquared() < 0.00001f)
            return Vector3.Zero;

        Vector3 slideDir = Vector3.Normalize(tangentComponent);
        return slideDir * remainingDistance;
    }

    public static CollisionResult CastSphere(Vector3 origin, Vector3 direction, float radius)
    {
        CollisionResult closest = new CollisionResult
        {
            Hit = false,
            Distance = float.MaxValue
        };

        float maxDist = direction.Length();
        if (maxDist < 0.00001f)
            return closest;

        Vector3 dir = direction / maxDist;

        foreach (var box in StaticColliders)
        {
            CollisionResult result = SphereBoxSweep(origin, dir, maxDist, radius, box);
            if (result.Hit && result.Distance < closest.Distance)
            {
                closest = result;
            }
        }

        foreach (var mesh in MeshColliders)
        {
            if (!mesh.Enabled)
                continue;

            CollisionResult result = SphereMeshSweep(origin, dir, maxDist, radius, mesh);
            if (result.Hit && result.Distance < closest.Distance)
            {
                closest = result;
            }
        }

        return closest;
    }

    private static CollisionResult SphereBoxSweep(Vector3 origin, Vector3 dir, float maxDist, float radius, BoundingBox box)
    {
        CollisionResult result = new CollisionResult { Hit = false };

        BoundingBox expanded = new BoundingBox(
            new Vector3(box.Min.X - radius, box.Min.Y - radius, box.Min.Z - radius),
            new Vector3(box.Max.X + radius, box.Max.Y + radius, box.Max.Z + radius)
        );

        Ray ray = new Ray(origin, dir);
        RayCollision collision = Raylib.GetRayCollisionBox(ray, expanded);

        if (!collision.Hit)
            return result;

        if (collision.Distance > maxDist)
            return result;

        if (collision.Distance < 0)
        {
            result.Hit = true;
            result.Distance = 0;
            result.Point = origin;
            result.Normal = GetBoxPushoutNormal(origin, box);
            return result;
        }

        result.Hit = true;
        result.Distance = collision.Distance;
        result.Point = collision.Point;
        result.Normal = collision.Normal;

        return result;
    }

    private static Vector3 GetBoxPushoutNormal(Vector3 point, BoundingBox box)
    {
        Vector3 center = (box.Min + box.Max) * 0.5f;
        Vector3 halfSize = (box.Max - box.Min) * 0.5f;
        Vector3 localPoint = point - center;

        float absX = MathF.Abs(localPoint.X) / halfSize.X;
        float absY = MathF.Abs(localPoint.Y) / halfSize.Y;
        float absZ = MathF.Abs(localPoint.Z) / halfSize.Z;

        if (absX >= absY && absX >= absZ)
            return new Vector3(MathF.Sign(localPoint.X), 0, 0);
        else if (absY >= absX && absY >= absZ)
            return new Vector3(0, MathF.Sign(localPoint.Y), 0);
        else
            return new Vector3(0, 0, MathF.Sign(localPoint.Z));
    }

    private static unsafe CollisionResult SphereMeshSweep(Vector3 origin, Vector3 dir, float maxDist, float radius, CollisionMesh meshData)
    {
        CollisionResult result = new CollisionResult { Hit = false, Distance = float.MaxValue };

        Ray ray = new Ray(origin, dir);

        for (int meshIndex = 0; meshIndex < meshData.Model.MeshCount; meshIndex++)
        {
            Mesh mesh = meshData.Model.Meshes[meshIndex];
            RayCollision collision = Raylib.GetRayCollisionMesh(ray, mesh, meshData.Transform);

            if (collision.Hit)
            {
                float adjustedDist = collision.Distance - radius;

                if (adjustedDist < 0) adjustedDist = 0;
                if (adjustedDist > maxDist) continue;

                if (adjustedDist < result.Distance)
                {
                    result.Hit = true;
                    result.Distance = adjustedDist;
                    result.Point = collision.Point;
                    result.Normal = collision.Normal;
                }
            }
        }

        return result;
    }

    public static bool CheckGroundCollision(Vector3 position, float height, out float groundY)
    {
        groundY = 0;

        Vector3 rayOrigin = position + new Vector3(0, height * 0.5f, 0);
        Vector3 rayDir = new Vector3(0, -1, 0);
        float rayLength = height + GroundCheckDistance;

        Ray ray = new Ray(rayOrigin, rayDir);

        float closestDist = float.MaxValue;
        bool hitGround = false;

        foreach (var box in StaticColliders)
        {
            RayCollision collision = Raylib.GetRayCollisionBox(ray, box);
            if (collision.Hit && collision.Distance < closestDist && collision.Distance <= rayLength)
            {
                closestDist = collision.Distance;
                groundY = collision.Point.Y;
                hitGround = true;
            }
        }

        foreach (var mesh in MeshColliders)
        {
            if (!mesh.Enabled)
                continue;

            for (int i = 0; i < mesh.Model.MeshCount; i++)
            {
                RayCollision collision = Raylib.GetRayCollisionMesh(ray, mesh.Model.Meshes[i], mesh.Transform);
                if (collision.Hit && collision.Distance < closestDist && collision.Distance <= rayLength)
                {
                    closestDist = collision.Distance;
                    groundY = collision.Point.Y;
                    hitGround = true;
                }
            }
        }

        if (position.Y <= 0.1f)
        {
            groundY = 0;
            hitGround = true;
        }

        return hitGround;
    }

    public static bool CheckCeiling(Vector3 position, float height, out float ceilingY)
    {
        ceilingY = float.MaxValue;

        Vector3 rayOrigin = position + new Vector3(0, height, 0);
        Vector3 rayDir = new Vector3(0, 1, 0);
        float rayLength = 0.5f;

        Ray ray = new Ray(rayOrigin, rayDir);

        foreach (var box in StaticColliders)
        {
            RayCollision collision = Raylib.GetRayCollisionBox(ray, box);
            if (collision.Hit && collision.Distance < rayLength)
            {
                ceilingY = MathF.Min(ceilingY, collision.Point.Y);
                return true;
            }
        }

        return false;
    }

    public static CollisionResult Raycast(Vector3 origin, Vector3 direction, float maxDistance)
    {
        CollisionResult result = new CollisionResult
        {
            Hit = false,
            Distance = float.MaxValue
        };

        Vector3 dir = Vector3.Normalize(direction);
        Ray ray = new Ray(origin, dir);

        foreach (var box in StaticColliders)
        {
            RayCollision collision = Raylib.GetRayCollisionBox(ray, box);
            if (collision.Hit && collision.Distance < result.Distance && collision.Distance <= maxDistance)
            {
                result.Hit = true;
                result.Distance = collision.Distance;
                result.Point = collision.Point;
                result.Normal = collision.Normal;
            }
        }

        foreach (var mesh in MeshColliders)
        {
            if (!mesh.Enabled)
                continue;

            for (int i = 0; i < mesh.Model.MeshCount; i++)
            {
                RayCollision collision = Raylib.GetRayCollisionMesh(ray, mesh.Model.Meshes[i], mesh.Transform);
                if (collision.Hit && collision.Distance < result.Distance && collision.Distance <= maxDistance)
                {
                    result.Hit = true;
                    result.Distance = collision.Distance;
                    result.Point = collision.Point;
                    result.Normal = collision.Normal;
                }
            }
        }

        return result;
    }

    public static bool PointInBox(Vector3 point, BoundingBox box)
    {
        return point.X >= box.Min.X && point.X <= box.Max.X &&
               point.Y >= box.Min.Y && point.Y <= box.Max.Y &&
               point.Z >= box.Min.Z && point.Z <= box.Max.Z;
    }

    public static Vector3 ClosestPointOnBox(Vector3 point, BoundingBox box)
    {
        return new Vector3(
            MathF.Max(box.Min.X, MathF.Min(point.X, box.Max.X)),
            MathF.Max(box.Min.Y, MathF.Min(point.Y, box.Max.Y)),
            MathF.Max(box.Min.Z, MathF.Min(point.Z, box.Max.Z))
        );
    }

    public static int GetColliderCount()
    {
        return StaticColliders.Count + MeshColliders.Count;
    }
}
