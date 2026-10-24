# Changelog

## 7.0.0 - 2026-10-24

- Replace linked-list render parameter indexing with contiguous storage.
- Reuse independent per-renderer parameter scratch objects during face drawing, preserving source defaults, null overrides and custom-subclass fallbacks.
- Write canonical vertex records into reusable storage; preserve allocating paths for extended formats and custom vertex implementations.
- Replace component lookup streams and tile lookup capturing lambdas with direct, ordered lookups that retain live list semantics.
- Update the Java 8 / Forge 1.12.2 build and repair the bundled chunk-collision mixin shadow names for the production runtime.
- Validation: 460,000 independent parameter/vertex parity comparisons; isolated real-client rendering and dedicated-server startup/shutdown. Hardware GPU and third-party shader acceptance remain unmeasured.
