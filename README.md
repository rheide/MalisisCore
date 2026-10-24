# MalisisCore 8 - Minecraft 1.21.1

Native support library for MalisisDoors 9 on NeoForge 1.21.1. It supplies reversible animation timelines, bounded resource caches, immutable textured/OBJ meshes, reusable render poses, and spatially indexed collision and selection hooks for geometry carried outside a block cell.

Collision and ray queries use loaded chunk buckets. Dynamic bounds can be reindexed; cell-local outlines keep native voxel traversal from selecting geometry through intervening walls. World unload clears its index. Client metrics are disabled unless the isolated harness explicitly enables them.

This is the Core functionality required by the native Doors implementation. It is not binary/source compatible with the legacy MalisisCore API and does not make unrelated 1.12.2 Malisis mods work on 1.21.1.

Build from the sibling Doors worktree using Java 21: `../modern/gradlew -p ../modern build --no-daemon`. Both mods produce separate JARs. Original upstream history and MIT licensing are preserved in this repository.
