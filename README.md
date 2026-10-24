# MalisisCore-compat 8 - Minecraft 1.21.1

Native support library for MalisisDoors 9 on NeoForge 1.21.1. It supplies reversible animation timelines, bounded resource caches, immutable textured/OBJ meshes, reusable render poses, and spatially indexed collision and selection hooks for geometry carried outside a block cell.

Collision and ray queries use loaded chunk buckets. Dynamic bounds can be reindexed; cell-local outlines keep native voxel traversal from selecting geometry through intervening walls. World unload clears its index. Client metrics are disabled unless the isolated harness explicitly enables them.

This is the Core functionality required by the native Doors implementation. It is not binary/source compatible with the legacy MalisisCore API and does not make unrelated 1.12.2 Malisis mods work on 1.21.1.

Build from the sibling Doors worktree using Java 21: `../modern/gradlew -p ../modern build --no-daemon`. Both mods produce separate JARs. Original upstream history and MIT licensing are preserved in this repository.

The public name is MalisisCore-compat and the artifact is `malisiscore-compat-1.21.1-8.0.1.jar`. The internal ID remains `malisiscore`. Doors and Core remain separate repositories at https://github.com/rheide/MalisisDoors and https://github.com/rheide/MalisisCore, with separate legacy performance, rename and native port history. Doors 9.1.0 supplies the original-world data converter; see its upgrade guide.
