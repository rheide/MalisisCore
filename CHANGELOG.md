# Changelog

## 8.0.0 - Minecraft 1.21.1 / NeoForge

Add the native support required by MalisisDoors 9: reversible timelines, bounded resource caches, immutable packed meshes and OBJ decoding, native baked-model extraction, tapered mesh emission and renderer-owned scratch poses.

Index carried geometry in loaded chunk buckets for dynamic collision and nearest-hit selection. Keep native cell traversal local, preserve intervening-wall occlusion, reindex changing coverage and clear indexes on world unload. Single-bucket queries reuse their bucket rather than constructing a temporary candidate set.

Keep optional callback/allocation metrics disabled during ordinary gameplay. This native support library does not preserve binary or source compatibility with the full legacy Core API.
