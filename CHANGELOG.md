# Changelog

## 8.0.1

Correct cuboidUV front-face coordinates so both sides of a split textured door retain the same texture-X positions. This restores the centre-seam handles on the eight large 3x3 door variants. Cached geometry and per-frame vertex counts are unchanged.

## 8.0.0 - Minecraft 1.21.1 / NeoForge

Add the native support required by MalisisDoors 9: reversible timelines, bounded resource caches, immutable packed meshes and OBJ decoding, native baked-model extraction, tapered mesh emission and renderer-owned scratch poses.

Index carried geometry in loaded chunk buckets for dynamic collision and nearest-hit selection. Keep native cell traversal local, preserve intervening-wall occlusion, reindex changing coverage and clear indexes on world unload. Single-bucket queries reuse their bucket rather than constructing a temporary candidate set.

Keep optional callback/allocation metrics disabled during ordinary gameplay. This native support library does not preserve binary or source compatibility with the full legacy Core API.
